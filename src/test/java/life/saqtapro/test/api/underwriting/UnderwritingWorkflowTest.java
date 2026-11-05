package life.saqtapro.test.api.underwriting;

import io.qameta.allure.Feature;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses.Policy;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import life.saqtapro.steps.SaqtaSteps;
import life.saqtapro.stubs.GovStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static life.saqtapro.auth.TestUser.AGENT_A;
import static life.saqtapro.auth.TestUser.UNDERWRITER;
import static life.saqtapro.auth.TestUser.UNDERWRITER_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Вкладка андеррайтера как конечный автомат (раздел 5 стратегии):
 * разрешённые переходы — позитивные тесты, запрещённые — 409 и неизменный статус.
 */
@SaqtaApiTest
@Tag("underwriting")
@Feature("Андеррайтинг")
class UnderwritingWorkflowTest {

    @Test
    @DisplayName("UW-01: договор выше лимита агента попадает в очередь андеррайтера и блокируется для агента")
    void submittedPolicyAppearsInQueue(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter,
                                       GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        underwriter.underwriting().queue()
                .statusCode(200)
                .body("items.id", hasItem(policyId));
        agent.policies().patch(policyId, Map.of("termYears", 3))
                .statusCode(anyOf(is(403), is(409)));
    }

    @Test
    @DisplayName("UW-01: повторная передача на андеррайтинг → 409")
    void doubleSubmitIsRejected(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        agent.policies().submitToUnderwriting(policyId).statusCode(409);
    }

    @Test
    @DisplayName("UW-04: одобрение → APPROVED, решение в журнале, агент может выпустить полис")
    void approveAndIssue(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.approve(SaqtaSteps.versionOf(underwriter, policyId)))
                .statusCode(200)
                .body("status", equalTo(Policy.APPROVED.name()));

        agent.policies().history(policyId)
                .statusCode(200)
                .body("items.toStatus", hasItem(Policy.APPROVED.name()))
                .body("items.find { it.toStatus == 'APPROVED' }.actorRole", equalTo("UNDERWRITER"));

        agent.policies().issue(policyId)
                .statusCode(200)
                .body("status", equalTo(Policy.ISSUED.name()));
    }

    @Test
    @DisplayName("UW-06: отклонение без причины → 400, статус не меняется")
    void rejectWithoutReasonIsInvalid(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter,
                                      GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.reject(SaqtaSteps.versionOf(underwriter, policyId), ""))
                .statusCode(400);
        assertEquals(Policy.UNDER_REVIEW, SaqtaSteps.statusOf(underwriter, policyId));
    }

    @Test
    @DisplayName("UW-07: отклонение с причиной → REJECTED, причина видна агенту, выпуск невозможен")
    void rejectWithReason(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();
        String reason = "Профессия повышенного риска без медицинского обследования";

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.reject(SaqtaSteps.versionOf(underwriter, policyId), reason))
                .statusCode(200);

        agent.policies().get(policyId)
                .body("status", equalTo(Policy.REJECTED.name()))
                .body("underwriting.comment", equalTo(reason));
        agent.policies().issue(policyId).statusCode(409);
    }

    @Test
    @DisplayName("UW-08: запрос доп. информации → агент дополняет → снова UNDER_REVIEW")
    void requestInfoLoop(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.requestInfo(SaqtaSteps.versionOf(underwriter, policyId),
                        "Приложите медицинскую анкету"))
                .statusCode(200)
                .body("status", equalTo(Policy.INFO_REQUESTED.name()));

        agent.policies().patch(policyId, Map.of("medicalQuestionnaireAttached", true)).statusCode(200);
        agent.policies().submitToUnderwriting(policyId)
                .statusCode(200)
                .body("status", equalTo(Policy.UNDER_REVIEW.name()));
    }

    @Test
    @DisplayName("UW-09: повторное решение по уже решённому договору → 409")
    void secondDecisionIsRejected(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter,
                                  GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();
        underwriter.underwriting()
                .decide(policyId, Requests.Decision.approve(SaqtaSteps.versionOf(underwriter, policyId)))
                .statusCode(200);

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.reject(SaqtaSteps.versionOf(underwriter, policyId), "передумал"))
                .statusCode(409);
        assertEquals(Policy.APPROVED, SaqtaSteps.statusOf(underwriter, policyId));
    }

    @Test
    @DisplayName("UW-10: два андеррайтера решают одновременно — принимается одно решение")
    void concurrentDecisionsUseOptimisticLock(@As(AGENT_A) SaqtaApi agent,
                                              @As(UNDERWRITER) SaqtaApi first,
                                              @As(UNDERWRITER_2) SaqtaApi second,
                                              GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();
        long version = SaqtaSteps.versionOf(first, policyId);

        CompletableFuture<ValidatableResponse> approve = CompletableFuture.supplyAsync(() ->
                first.underwriting().decide(policyId, Requests.Decision.approve(version)));
        CompletableFuture<ValidatableResponse> reject = CompletableFuture.supplyAsync(() ->
                second.underwriting().decide(policyId, Requests.Decision.reject(version, "Высокий риск")));

        List<Integer> codes = List.of(approve.join().extract().statusCode(), reject.join().extract().statusCode());
        assertThat("ровно одно решение принято, второе отклонено", codes,
                containsInAnyOrder(200, 409));
    }

    @Test
    @DisplayName("Запрещённый переход: черновик нельзя одобрить в обход очереди")
    void draftCannotBeApproved(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter,
                               GovStubs stubs) {
        String clientId = SaqtaSteps.verifiedClient(agent, stubs).clientId();
        String policyId = SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(clientId));

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.approve(SaqtaSteps.versionOf(agent, policyId)))
                .statusCode(anyOf(is(403), is(404), is(409)));
        assertEquals(Policy.DRAFT, SaqtaSteps.statusOf(agent, policyId));
    }
}
