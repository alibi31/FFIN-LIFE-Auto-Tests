package life.saqtapro.test.api.rbac;

import io.qameta.allure.Feature;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses;
import life.saqtapro.data.KzIban;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import life.saqtapro.steps.SaqtaSteps;
import life.saqtapro.steps.SaqtaSteps.VerifiedClient;
import life.saqtapro.stubs.GovStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static life.saqtapro.auth.TestUser.AGENT_A;
import static life.saqtapro.auth.TestUser.AGENT_B;
import static life.saqtapro.auth.TestUser.MANAGER_ALMATY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SaqtaApiTest
@Tag("rbac")
@Feature("RBAC")
class RbacNegativeTest {

    @Test
    @DisplayName("Агент не может повысить статус договора через PATCH (mass assignment)")
    void agentCannotEscalateStatusViaPatch(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        VerifiedClient client = SaqtaSteps.verifiedClient(agent, stubs);
        String policyId = SaqtaSteps.draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(client.clientId()));

        agent.policies()
                .patch(policyId, Map.of("status", "APPROVED", "underwriterId", "self"))
                .statusCode(anyOf(is(200), is(400)));   // поле либо игнорируется, либо запрос отклоняется

        assertEquals(Statuses.Policy.DRAFT, SaqtaSteps.statusOf(agent, policyId));
    }

    @Test
    @DisplayName("Поиск не раскрывает договоры другого агента, включая счётчик total")
    void searchDoesNotLeakForeignPolicies(@As(AGENT_A) SaqtaApi agentA, @As(AGENT_B) SaqtaApi agentB,
                                          GovStubs stubs) {
        VerifiedClient client = SaqtaSteps.verifiedClient(agentA, stubs);
        SaqtaSteps.draftPolicy(agentA, Requests.PolicyDraft.withinAgentLimit(client.clientId()));

        agentB.policies().search(client.person().iin())
                .statusCode(200)
                .body("items", empty())
                .body("total", equalTo(0));
    }

    @Test
    @DisplayName("GOV-15: внештатный агент видит IBAN только маской и не видит родственные связи")
    void agentSeesMaskedSensitiveData(@As(AGENT_A) SaqtaApi agent, @As(MANAGER_ALMATY) SaqtaApi manager,
                                      GovStubs stubs) {
        VerifiedClient client = SaqtaSteps.verifiedClient(agent, stubs);
        String fullIban = client.egov().iban();
        String relativeIin = client.egov().relative().iin();

        ValidatableResponse asAgent = agent.clients().get(client.clientId()).statusCode(200);
        String agentBody = asAgent.extract().asString();
        asAgent.body("iban", equalTo(KzIban.masked(fullIban)));
        assertThat("полный IBAN в ответе агенту", agentBody, not(containsString(fullIban)));
        assertThat("ИИН родственника в ответе агенту", agentBody, not(containsString(relativeIin)));

        // Требование для менеджера подтвердить у владельца продукта; здесь — полный доступ
        manager.clients().get(client.clientId())
                .statusCode(200)
                .body("iban", equalTo(fullIban))
                .body("relatives[0].iin", equalTo(relativeIin));
    }

    static Stream<Arguments> protectedCalls() {
        Function<SaqtaApi, ValidatableResponse> list = api -> api.policies().search("");
        Function<SaqtaApi, ValidatableResponse> queue = api -> api.underwriting().queue();
        Function<SaqtaApi, ValidatableResponse> verify = api -> api.gov()
                .start(new Requests.StartGovVerification("900101300123", "+77001234567"));
        return Stream.of(
                Arguments.of("GET /policies", list),
                Arguments.of("GET /underwriting/queue", queue),
                Arguments.of("POST /gov/verifications", verify));
    }

    @ParameterizedTest(name = "{0} без токена → 401")
    @MethodSource("protectedCalls")
    @DisplayName("Защищённые эндпоинты без токена")
    void noTokenIsRejected(String name, Function<SaqtaApi, ValidatableResponse> call) {
        call.apply(SaqtaApi.anonymous()).statusCode(401);
    }

    @ParameterizedTest(name = "{0} с поддельным токеном → 401")
    @MethodSource("protectedCalls")
    @DisplayName("Защищённые эндпоинты с поддельным JWT")
    void forgedTokenIsRejected(String name, Function<SaqtaApi, ValidatableResponse> call) {
        String forged = "eyJhbGciOiJub25lIn0.eyJyb2xlIjoiVU5ERVJXUklURVIifQ."; // alg=none, role=UNDERWRITER
        call.apply(SaqtaApi.withRawToken(forged)).statusCode(401);
    }
}
