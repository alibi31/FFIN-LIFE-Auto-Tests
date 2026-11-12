package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.function.Supplier;

/** Профиль текущего сотрудника SaqtaPro: GET /api/me — кто владелец токена */
public final class ProfileApi {

    private final Supplier<RequestSpecification> spec;

    ProfileApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    @Step("Получить профиль текущего сотрудника (GET /api/me)")
    public ValidatableResponse me() {
        return spec.get().when().get(SaqtaEndpoints.ME).then();
    }
}
