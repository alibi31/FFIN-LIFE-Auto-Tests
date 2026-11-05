package life.saqtapro.test.api.rbac;

import io.qameta.allure.Feature;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses;
import life.saqtapro.auth.TestUser;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import life.saqtapro.steps.SaqtaSteps;
import life.saqtapro.stubs.GovStubs;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static life.saqtapro.auth.TestUser.AGENT_A;
import static life.saqtapro.auth.TestUser.AGENT_B;
import static life.saqtapro.auth.TestUser.COMPLIANCE_OFFICER;
import static life.saqtapro.auth.TestUser.MANAGER_ALMATY;
import static life.saqtapro.auth.TestUser.MANAGER_ASTANA;
import static life.saqtapro.auth.TestUser.UNDERWRITER;
import static life.saqtapro.test.api.rbac.RbacMatrixTest.Access.ALLOWED;
import static life.saqtapro.test.api.rbac.RbacMatrixTest.Access.DENIED;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Матрица прав «пользователь × действие» (раздел 2 стратегии). Договор всегда принадлежит AGENT_A
 * из филиала Алматы, поэтому одна таблица покрывает и роли, и IDOR, и межфилиальную изоляцию.
 * Строки матрицы — единственное место, которое меняется при изменении требований.
 */
@SaqtaApiTest
@Tag("rbac")
@Feature("RBAC")
class RbacMatrixTest {

    enum Access {
        ALLOWED(anyOf(is(200), is(201))),
        /** 404 допустим: сервер может скрывать сам факт существования чужого договора */
        DENIED(anyOf(is(403), is(404)));

        final Matcher<Integer> status;

        Access(Matcher<Integer> status) {
            this.status = status;
        }
    }

    enum Precondition { DRAFT, UNDER_REVIEW }

    enum Action {
        VIEW_POLICY(Precondition.DRAFT, false,
                (api, id) -> api.policies().get(id)),
        EDIT_DRAFT(Precondition.DRAFT, true,
                (api, id) -> api.policies().patch(id, Map.of("termYears", 7))),
        SUBMIT_TO_UNDERWRITING(Precondition.DRAFT, true,
                (api, id) -> api.policies().submitToUnderwriting(id)),
        VIEW_UNDERWRITING_QUEUE(Precondition.UNDER_REVIEW, false,
                (api, id) -> api.underwriting().queue()),
        APPROVE(Precondition.UNDER_REVIEW, true,
                (api, id) -> api.underwriting().decide(id, Requests.Decision.approve(SaqtaSteps.versionOf(SaqtaApi.as(AGENT_A), id))));

        final Precondition precondition;
        final boolean changesState;
        final BiFunction<SaqtaApi, String, ValidatableResponse> call;

        Action(Precondition precondition, boolean changesState, BiFunction<SaqtaApi, String, ValidatableResponse> call) {
            this.precondition = precondition;
            this.changesState = changesState;
            this.call = call;
        }
    }

    static Stream<Arguments> matrix() {
        return Stream.of(
                // просмотр договора
                Arguments.of(AGENT_A, Action.VIEW_POLICY, ALLOWED),
                Arguments.of(AGENT_B, Action.VIEW_POLICY, DENIED),             // IDOR: чужой агент
                Arguments.of(MANAGER_ALMATY, Action.VIEW_POLICY, ALLOWED),
                Arguments.of(MANAGER_ASTANA, Action.VIEW_POLICY, DENIED),      // другой филиал
                Arguments.of(UNDERWRITER, Action.VIEW_POLICY, DENIED),         // черновик ещё не передан
                // редактирование черновика
                Arguments.of(AGENT_A, Action.EDIT_DRAFT, ALLOWED),
                Arguments.of(AGENT_B, Action.EDIT_DRAFT, DENIED),
                Arguments.of(MANAGER_ALMATY, Action.EDIT_DRAFT, ALLOWED),
                Arguments.of(MANAGER_ASTANA, Action.EDIT_DRAFT, DENIED),
                Arguments.of(UNDERWRITER, Action.EDIT_DRAFT, DENIED),
                // передача на андеррайтинг
                Arguments.of(AGENT_A, Action.SUBMIT_TO_UNDERWRITING, ALLOWED),
                Arguments.of(AGENT_B, Action.SUBMIT_TO_UNDERWRITING, DENIED),
                Arguments.of(MANAGER_ALMATY, Action.SUBMIT_TO_UNDERWRITING, ALLOWED),
                Arguments.of(UNDERWRITER, Action.SUBMIT_TO_UNDERWRITING, DENIED),
                // очередь андеррайтинга: менеджер — только чтение
                Arguments.of(AGENT_A, Action.VIEW_UNDERWRITING_QUEUE, DENIED),
                Arguments.of(MANAGER_ALMATY, Action.VIEW_UNDERWRITING_QUEUE, ALLOWED),
                Arguments.of(UNDERWRITER, Action.VIEW_UNDERWRITING_QUEUE, ALLOWED),
                // решение по договору — только андеррайтер
                Arguments.of(AGENT_A, Action.APPROVE, DENIED),
                Arguments.of(MANAGER_ALMATY, Action.APPROVE, DENIED),
                Arguments.of(COMPLIANCE_OFFICER, Action.APPROVE, DENIED),
                Arguments.of(UNDERWRITER, Action.APPROVE, ALLOWED)
        );
    }

    @ParameterizedTest(name = "{0} → {1}: {2}")
    @MethodSource("matrix")
    @DisplayName("RBAC: доступ к действиям над договором агента A")
    void accessMatrix(TestUser user, Action action, Access expected,
                      @As(AGENT_A) SaqtaApi owner, GovStubs stubs) {

        String policyId = action.precondition == Precondition.UNDER_REVIEW
                ? SaqtaSteps.policyUnderReview(owner, stubs).id()
                : SaqtaSteps.draftPolicy(owner,
                Requests.PolicyDraft.aboveAgentLimit(SaqtaSteps.verifiedClient(owner, stubs).clientId()));
        Statuses.Policy before = SaqtaSteps.statusOf(owner, policyId);

        action.call.apply(SaqtaApi.as(user), policyId).statusCode(expected.status);

        // Отказ должен быть настоящим: ответ 403 при изменённом статусе — тоже дефект
        if (expected == DENIED && action.changesState) {
            assertEquals(before, SaqtaSteps.statusOf(owner, policyId),
                    "Статус договора изменился, хотя " + user + " получил отказ");
        }
    }
}
