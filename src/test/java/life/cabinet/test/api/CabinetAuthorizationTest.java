package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.auth.CabinetAuthorizationRequest;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

@Tag("cabinet-api")
@Owner("QA")
public class CabinetAuthorizationTest {

    private static final String BASE_URL = EnvConfig.cfg().apiCabinetUrl();
    private static final String AUTH_ENDPOINT = "/api/v1/auth/login";

    private final ApiClient apiClient = new ApiClient();

    @Test
    @DisplayName("Успешная авторизация с валидными данными")
    void successfulAuthorization() {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin("010131500057")
                .password("alibi2001")
                .build();

        ValidatableResponse response = apiClient.post(BASE_URL, AUTH_ENDPOINT, request)
                .statusCode(200)
                .contentType(JSON)
                .body("token", notNullValue())
                .body("token", not(emptyString()));
    }

    @Test
    @DisplayName("Неуспешная авторизация с неверным паролем")
    void authorizationWithInvalidPassword() {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin("010131500057")
                .password("wrongPassword")
                .build();

        apiClient.post(BASE_URL, AUTH_ENDPOINT, request)
                .statusCode(401)
                .contentType(JSON)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Неуспешная авторизация с несуществующим UIN")
    void authorizationWithNonExistentUin() {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin("999999999999")
                .password("alibi2001")
                .build();

        apiClient.post(BASE_URL, AUTH_ENDPOINT, request)
                .statusCode(400)
                .contentType(JSON);
    }

    @Test
    @DisplayName("Неуспешная авторизация без пароля")
    void authorizationWithoutPassword() {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin("010131500057")
                .password(null)
                .build();

        apiClient.post(BASE_URL, AUTH_ENDPOINT, request)
                .statusCode(422)
                .contentType(JSON);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",           // пустой
            "1",          // 1 цифра
            "12345678901", // 11 цифр
            "1234567890123" // 13 цифр
    })
    @DisplayName("Неуспешная авторизация с ИИН не из 12 цифр")
    void authorizationWithVariousInvalidUinLengths(String invalidUin) {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin(invalidUin)
                .password("alibi2001")
                .build();

        apiClient.post(BASE_URL, AUTH_ENDPOINT, request)
                .statusCode(422)
                .contentType(JSON);
    }

}
