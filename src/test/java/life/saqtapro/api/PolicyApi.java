package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.Requests;

import java.util.Map;
import java.util.function.Supplier;

public final class PolicyApi {

    private final Supplier<RequestSpecification> spec;

    PolicyApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    @Step("Создать черновик договора")
    public ValidatableResponse create(Requests.PolicyDraft draft) {
        return spec.get().body(draft).when().post(SaqtaEndpoints.POLICIES).then();
    }

    @Step("Получить договор {policyId}")
    public ValidatableResponse get(String policyId) {
        return spec.get().pathParam("policyId", policyId).when().get(SaqtaEndpoints.POLICY).then();
    }

    @Step("Изменить поля договора {policyId}")
    public ValidatableResponse patch(String policyId, Map<String, ?> fields) {
        return spec.get().pathParam("policyId", policyId).body(fields)
                .when().patch(SaqtaEndpoints.POLICY).then();
    }

    @Step("Поиск договоров: {query}")
    public ValidatableResponse search(String query) {
        return spec.get().queryParam("search", query).when().get(SaqtaEndpoints.POLICIES).then();
    }

    @Step("Передать договор {policyId} на андеррайтинг")
    public ValidatableResponse submitToUnderwriting(String policyId) {
        return spec.get().pathParam("policyId", policyId).when().post(SaqtaEndpoints.POLICY_SUBMIT).then();
    }

    @Step("Выпустить полис по договору {policyId}")
    public ValidatableResponse issue(String policyId) {
        return spec.get().pathParam("policyId", policyId).when().post(SaqtaEndpoints.POLICY_ISSUE).then();
    }

    @Step("Получить журнал переходов договора {policyId}")
    public ValidatableResponse history(String policyId) {
        return spec.get().pathParam("policyId", policyId).when().get(SaqtaEndpoints.POLICY_HISTORY).then();
    }
}
