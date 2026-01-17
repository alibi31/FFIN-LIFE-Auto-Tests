package life.core.http;

import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.auth.CabinetAuthorizationRequest;
import life.utils.config.EnvConfig;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;

public class ApiClient {
    private static final AtomicReference<TokenData> cachedToken = new AtomicReference<>();

    private static class TokenData {
        final String token;
        final Instant expiresAt;

        TokenData(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // Получение токена с кэшированием
    public synchronized String getAuthToken() {
        return getAuthToken("010131500057", "alibi2001");
    }

    public synchronized String getAuthToken(String uin, String password) {
        TokenData current = cachedToken.get();

        // Если токен есть и не истек - возвращаем
        if (current != null && !current.isExpired()) {
            return current.token;
        }

        // Получаем новый токен
        String newToken = fetchFreshToken(uin, password);
        // Токен действителен 10 минут (600 секунд)
        Instant expiresAt = Instant.now().plusSeconds(600);

        cachedToken.set(new TokenData(newToken, expiresAt));
        return newToken;
    }

    private String fetchFreshToken(String uin, String password) {
        CabinetAuthorizationRequest request = CabinetAuthorizationRequest.builder()
                .uin(uin)
                .password(password)
                .build();

        Response response = given()
                .spec(RequestSpec.json(EnvConfig.cfg().apiCabinetUrl()))
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getString("token");
    }

    // Метод для принудительного обновления токена
    public void refreshToken() {
        cachedToken.set(null);
        getAuthToken();
    }

    // Метод для проверки валидности токена
    public boolean isTokenValid() {
        TokenData current = cachedToken.get();
        return current != null && !current.isExpired();
    }

    // Новый метод с автоматической авторизацией
    public ValidatableResponse postWithAuth(String baseUri, String uriPath, Object body) {
        String token = getAuthToken();

        return given()
                .spec(RequestSpec.json(baseUri))
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(uriPath)
                .then();
    }

    // Метод с автоматическим обновлением токена при 401
    public ValidatableResponse postWithAutoRefresh(String baseUri, String uriPath, Object body) {
        String token = getAuthToken();

        ValidatableResponse response = given()
                .spec(RequestSpec.json(baseUri))
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(uriPath)
                .then();

        // Если получили 401 - обновляем токен и повторяем запрос
        try {
            response.statusCode(401);
            // 401 означает невалидный токен
            refreshToken();
            token = getAuthToken();

            return given()
                    .spec(RequestSpec.json(baseUri))
                    .header("Authorization", "Bearer " + token)
                    .body(body)
                    .when()
                    .post(uriPath)
                    .then();
        } catch (AssertionError e) {
            // Если не 401 - просто возвращаем результат
            return response;
        }
    }

    public ValidatableResponse get(String baseUri, String uriPath) {
        return given().spec(RequestSpec.json(baseUri))
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse get(String baseUri, String uriPath, Header headers) {
        return given().spec(RequestSpec.json(baseUri))
                .header(headers)
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .when().post(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body, Header headers) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .header(headers)
                .when().post(uriPath)
                .then();
    }

    public ValidatableResponse put(String baseUri, String uriPath, Object body) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .when().put(uriPath)
                .then();
    }

    public ValidatableResponse delete(String baseUri, String uriPath) {
        return given().spec(RequestSpec.json(baseUri))
                .when().delete(uriPath)
                .then();
    }
}
