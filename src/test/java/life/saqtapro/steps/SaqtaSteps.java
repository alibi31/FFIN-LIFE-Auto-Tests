package life.saqtapro.steps;

import io.qameta.allure.Step;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses;
import life.saqtapro.stubs.EgovStub;
import life.saqtapro.stubs.GovStubs;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;

import static org.hamcrest.Matchers.equalTo;

/**
 * Составные шаги подготовки данных через API. Каждый тест строит своё состояние сам,
 * не опираясь на порядок запуска и на чужие договоры.
 */
public final class SaqtaSteps {

    private SaqtaSteps() {}

    public record VerifiedClient(TestPerson person, String clientId, String verificationId,
                                 EgovStub.EgovPerson egov) {}

    @Step("Подготовить клиента, прошедшего гос. верификацию и комплаенс")
    public static VerifiedClient verifiedClient(SaqtaApi api, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = stubs.happyPath(person);
        return createClient(api, person, egov);
    }

    /** Для сценариев, где стабы (например, комплаенс) уже настроены тестом */
    @Step("Пройти гос. верификацию и создать карточку клиента")
    public static VerifiedClient createClient(SaqtaApi api, TestPerson person, EgovStub.EgovPerson egov) {
        String verificationId = api.gov().startAndGetId(person.iin(), person.phone());
        api.gov().awaitTerminal(verificationId).body("status", equalTo(Statuses.Verification.COMPLETED.name()));

        String clientId = api.clients()
                .create(new Requests.CreateClient(person.iin(), person.phone(), verificationId))
                .statusCode(201)
                .extract().path("id");
        return new VerifiedClient(person, clientId, verificationId, egov);
    }

    @Step("Создать черновик договора")
    public static String draftPolicy(SaqtaApi api, Requests.PolicyDraft draft) {
        return api.policies().create(draft)
                .statusCode(201)
                .body("status", equalTo(Statuses.Policy.DRAFT.name()))
                .extract().path("id");
    }

    public record PolicyInReview(String id, VerifiedClient client) {}

    @Step("Подготовить договор в статусе UNDER_REVIEW")
    public static PolicyInReview policyUnderReview(SaqtaApi agent, GovStubs stubs) {
        VerifiedClient client = verifiedClient(agent, stubs);
        String policyId = draftPolicy(agent, Requests.PolicyDraft.aboveAgentLimit(client.clientId()));
        agent.policies().submitToUnderwriting(policyId)
                .statusCode(200)
                .body("status", equalTo(Statuses.Policy.UNDER_REVIEW.name()));
        return new PolicyInReview(policyId, client);
    }

    public static Statuses.Policy statusOf(SaqtaApi api, String policyId) {
        return Statuses.Policy.valueOf(api.policies().get(policyId).statusCode(200).extract().path("status"));
    }

    /** Версия для оптимистической блокировки при решении андеррайтера */
    public static long versionOf(SaqtaApi api, String policyId) {
        Number version = api.policies().get(policyId).statusCode(200).extract().path("version");
        return version.longValue();
    }
}
