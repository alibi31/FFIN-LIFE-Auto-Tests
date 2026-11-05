package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.Requests;

import java.util.function.Supplier;

public final class ClientApi {

    private final Supplier<RequestSpecification> spec;

    ClientApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    @Step("Создать карточку клиента по результату гос. верификации {request.verificationId}")
    public ValidatableResponse create(Requests.CreateClient request) {
        return spec.get().body(request).when().post(SaqtaEndpoints.CLIENTS).then();
    }

    @Step("Получить карточку клиента {clientId}")
    public ValidatableResponse get(String clientId) {
        return spec.get().pathParam("clientId", clientId).when().get(SaqtaEndpoints.CLIENT).then();
    }

    @Step("Получить результат комплаенс-проверки клиента {clientId}")
    public ValidatableResponse compliance(String clientId) {
        return spec.get().pathParam("clientId", clientId).when().get(SaqtaEndpoints.CLIENT_COMPLIANCE).then();
    }

    @Step("Перезапустить комплаенс-проверку клиента {clientId}")
    public ValidatableResponse recheckCompliance(String clientId) {
        return spec.get().pathParam("clientId", clientId).when().post(SaqtaEndpoints.CLIENT_COMPLIANCE).then();
    }
}
