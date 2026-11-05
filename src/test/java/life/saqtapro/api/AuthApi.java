package life.saqtapro.api;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.model.LoginRequest;

import static io.restassured.RestAssured.given;

/** Авторизация сотрудника в SaqtaPro: POST /api/login {mail, password} → data.token */
public final class AuthApi {

    @Step("Войти в SaqtaPro под {mail}")
    public ValidatableResponse login(String mail,
                                     @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        return given()
                .spec(SaqtaRequestSpec.anonymousJson())
                .body(new LoginRequest(mail, password))
                .when()
                .post(SaqtaEndpoints.LOGIN)
                .then();
    }
}
