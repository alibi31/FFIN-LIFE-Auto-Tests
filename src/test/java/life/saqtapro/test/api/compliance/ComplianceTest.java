package life.saqtapro.test.api.compliance;

import io.qameta.allure.Feature;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses.Compliance;
import life.saqtapro.api.model.Statuses.Policy;
import life.saqtapro.api.model.Statuses.WatchList;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import life.saqtapro.steps.SaqtaSteps;
import life.saqtapro.steps.SaqtaSteps.VerifiedClient;
import life.saqtapro.stubs.EgovStub;
import life.saqtapro.stubs.GovStubs;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static life.saqtapro.auth.TestUser.AGENT_A;
import static life.saqtapro.auth.TestUser.MANAGER_ALMATY;
import static life.saqtapro.auth.TestUser.UNDERWRITER;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Комплаенс-контроль (раздел 3 стратегии). Гос. верификация во всех тестах проходит —
 * отличается только ответ комплаенс-провайдера, чтобы тест падал по одной причине.
 *
 * Допущение: при совпадении SaqtaPro создаёт карточку клиента со статусом BLOCKED (201),
 * а оформление договора отклоняет с 422. Если backend отвечает 422 уже при создании клиента —
 * поправить blockedClient().
 */
@SaqtaApiTest
@Tag("compliance")
@Feature("Комплаенс-контроль")
class ComplianceTest {

    @Test
    @DisplayName("CMP-01: легитимный клиент проходит проверку и может оформить договор")
    void cleanClientCanGetPolicy(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        VerifiedClient client = SaqtaSteps.verifiedClient(agent, stubs);

        agent.clients().compliance(client.clientId())
                .statusCode(200)
                .body("status", equalTo(Compliance.CLEAR.name()));
        SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.withinAgentLimit(client.clientId()));
    }

    @ParameterizedTest(name = "CMP-02: совпадение в списке {0} блокирует оформление")
    @EnumSource(WatchList.class)
    @DisplayName("CMP-02: клиент из черного списка")
    void listedClientIsBlocked(WatchList list, @As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        String clientId = blockedClient(agent, stubs, list);

        agent.policies().create(Requests.PolicyDraft.withinAgentLimit(clientId))
                .statusCode(422)
                .body("code", equalTo("CLIENT_COMPLIANCE_BLOCKED"));
    }

    @Test
    @DisplayName("CMP-03: частичное совпадение → ручной досмотр, на андеррайтинг не передаётся")
    void possibleMatchGoesToManualReview(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = govPassesFor(stubs, person);
        stubs.compliance().possibleMatch(person);

        VerifiedClient client = SaqtaSteps.createClient(agent, person, egov);
        agent.clients().compliance(client.clientId())
                .body("status", equalTo(Compliance.MANUAL_REVIEW.name()));

        String policyId = SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(client.clientId()));
        agent.policies().submitToUnderwriting(policyId).statusCode(422);
        assertEquals(Policy.DRAFT, SaqtaSteps.statusOf(agent, policyId));
    }

    @Test
    @DisplayName("CMP-04: клиент попал в список после создания черновика — передача блокируется")
    void listedAfterDraftIsBlockedOnSubmit(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        VerifiedClient client = SaqtaSteps.verifiedClient(agent, stubs);
        String policyId = SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(client.clientId()));
        int checksBefore = stubs.compliance().callsFor(client.person());

        stubs.compliance().listed(client.person(), WatchList.TERRORISTS_EXTREMISTS);

        agent.policies().submitToUnderwriting(policyId).statusCode(422);
        assertTrue(stubs.compliance().callsFor(client.person()) > checksBefore,
                "Перед передачей на андеррайтинг SaqtaPro должен повторно проверить клиента по спискам");
        assertEquals(Policy.BLOCKED, SaqtaSteps.statusOf(agent, policyId));
    }

    @Test
    @DisplayName("CMP-06: провайдер комплаенса недоступен → fail-closed, клиент не CLEAR")
    void providerOutageFailsClosed(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = govPassesFor(stubs, person);
        stubs.compliance().unavailable(person);

        VerifiedClient client = SaqtaSteps.createClient(agent, person, egov);

        agent.clients().compliance(client.clientId())
                .body("status", anyOf(is(Compliance.PENDING.name()), is(Compliance.BLOCKED.name())));
        String policyId = SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(client.clientId()));
        agent.policies().submitToUnderwriting(policyId).statusCode(422);
    }

    @Test
    @DisplayName("CMP-07: андеррайтер не может одобрить договор заблокированного клиента")
    void underwriterCannotApproveBlocked(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter,
                                         GovStubs stubs) {
        SaqtaSteps.PolicyInReview policy = SaqtaSteps.policyUnderReview(agent, stubs);
        String policyId = policy.id();
        stubs.compliance().listed(policy.client().person(), WatchList.SANCTIONS);

        underwriter.underwriting()
                .decide(policyId, Requests.Decision.approve(SaqtaSteps.versionOf(underwriter, policyId)))
                .statusCode(anyOf(is(409), is(422)));
        assertEquals(Policy.UNDER_REVIEW, SaqtaSteps.statusOf(underwriter, policyId));
    }

    @Test
    @DisplayName("CMP-08: агент видит только факт блокировки, менеджер — причину")
    void blockReasonVisibilityByRole(@As(AGENT_A) SaqtaApi agent, @As(MANAGER_ALMATY) SaqtaApi manager,
                                     GovStubs stubs) {
        String clientId = blockedClient(agent, stubs, WatchList.TERRORISTS_EXTREMISTS);

        agent.clients().compliance(clientId)
                .statusCode(200)
                .body("status", equalTo(Compliance.BLOCKED.name()))
                .body("matchedLists", nullValue());

        manager.clients().compliance(clientId)
                .statusCode(200)
                .body("matchedLists", hasItem(WatchList.TERRORISTS_EXTREMISTS.name()));
    }

    // ---------- helpers ----------

    private static EgovStub.EgovPerson govPassesFor(GovStubs stubs, TestPerson person) {
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person);
        stubs.consent().approved(person);
        return stubs.egov().personData(person);
    }

    private static String blockedClient(SaqtaApi agent, GovStubs stubs, WatchList list) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = govPassesFor(stubs, person);
        stubs.compliance().listed(person, list);

        String clientId = SaqtaSteps.createClient(agent, person, egov).clientId();
        agent.clients().get(clientId).body("complianceStatus", equalTo(Compliance.BLOCKED.name()));
        return clientId;
    }
}
