package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.Requests;

import java.util.function.Supplier;

public final class UnderwritingApi {

    private final Supplier<RequestSpecification> spec;

    UnderwritingApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    @Step("Получить очередь андеррайтинга")
    public ValidatableResponse queue() {
        return spec.get().when().get(SaqtaEndpoints.UW_QUEUE).then();
    }

    @Step("Принять решение по договору {policyId}: {decision.decision}")
    public ValidatableResponse decide(String policyId, Requests.Decision decision) {
        return spec.get().pathParam("policyId", policyId).body(decision)
                .when().post(SaqtaEndpoints.UW_DECISION).then();
    }
}
