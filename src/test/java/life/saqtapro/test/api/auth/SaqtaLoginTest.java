package life.saqtapro.test.api.auth;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import life.saqtapro.api.AuthApi;
import life.saqtapro.auth.TestUser;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@SaqtaApiTest
@Tag("smoke")
@Feature("Авторизация")
@Owner("QA")
class SaqtaLoginTest {

    private final AuthApi authApi = new AuthApi();

    @Test
    @DisplayName("POST /api/login — сотрудник получает токен и профиль")
    void employeeGetsToken() {
        TestUser user = TestUser.QA_EMPLOYEE;

        authApi.login(user.login(), user.password())
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                // токен Laravel Sanctum: "<id>|<секрет>"
                .body("data.token", matchesPattern("^\\d+\\|[A-Za-z0-9]{40,}$"))
                .body("data.id", notNullValue())
                .body("data.email", equalToIgnoringCase(user.login()))
                .body("data.branch.name_ru", notNullValue());
    }

    @Test
    @DisplayName("POST /api/login — неверный пароль не выдаёт токен")
    void wrongPasswordIsRejected() {
        authApi.login(TestUser.QA_EMPLOYEE.login(), "wrong-password-autotest")
                .body("status", not(true))
                .body("data.token", nullValue());
    }

    @ParameterizedTest(name = "mail=«{0}», password=«{1}»")
    @CsvSource(value = {
            "'', ''",
            "'', any-password",
            "not-an-email, any-password",
            "nobody.autotest@ffin.life, any-password"
    })
    @DisplayName("POST /api/login — некорректные учётные данные не выдают токен")
    void invalidCredentialsAreRejected(String mail, String password) {
        authApi.login(mail, password)
                .body("status", not(true))
                .body("data.token", nullValue());
    }
}
