package life.core.http;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.auth.CabinetAuthorizationRequest;
import life.utils.config.EnvConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

public class ApiClient {

    /** Время жизни токена на стенде — 1 час; обновляем с запасом, чтобы не получить 401 на границе */
    private static final Duration TOKEN_TTL = Duration.ofHours(1);
    private static final Duration SAFETY_MARGIN = Duration.ofMinutes(2);

    /** Кэш токенов по пользователю (uin). Общий для всех экземпляров ApiClient и всех потоков. */
    private static final Map<String, TokenData> TOKENS = new ConcurrentHashMap<>();

    private record TokenData(String token, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // ---------- Авторизация ----------

    /** Токен пользователя по умолчанию (учётка берётся из конфигурации, не из кода) */
    public String getAuthToken() {
        return getAuthToken(defaultUin(), defaultPassword());
    }

    @Step("Получить Bearer токен для пользователя {uin}")
    public String getAuthToken(String uin,
                               @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        // compute блокирует только этот uin: за токеном одного пользователя идёт ровно один поток,
        // остальные ждут и получают уже закэшированный токен
        return TOKENS.compute(uin, (key, current) ->
                current != null && !current.isExpired()
                        ? current
                        : new TokenData(fetchFreshToken(uin, password),
                        Instant.now().plus(TOKEN_TTL).minus(SAFETY_MARGIN))
        ).token();
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

        String body = response.asString();
        String token = (body == null || body.isBlank()) ? null : response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Логин пользователя " + uin + " вернул 200, но без токена. Тело ответа: '" + body + "'");
        }
        return token;
    }

    /** Сбросить токен пользователя по умолчанию и получить новый */
    public void refreshToken() {
        refreshToken(defaultUin(), defaultPassword());
    }

    public void refreshToken(String uin, String password) {
        TOKENS.remove(uin);
        getAuthToken(uin, password);
    }

    public boolean isTokenValid() {
        TokenData current = TOKENS.get(defaultUin());
        return current != null && !current.isExpired();
    }

    private static String defaultUin() {
        return EnvConfig.cfg().cabinetUin();
    }

    private static String defaultPassword() {
        return EnvConfig.cfg().cabinetPassword();
    }

    private Header bearer() {
        return new Header("Authorization", "Bearer " + getAuthToken());
    }

    // ---------- Запросы с авторизацией ----------

    @Step("Отправить GET запрос с авторизацией")
    public ValidatableResponse getWithAuth(String baseUri, String uriPath) {
        return get(baseUri, uriPath, bearer());
    }

    @Step("Отправить GET запрос с авторизацией и параметрами")
    public ValidatableResponse getWithAuth(String baseUri, String uriPath, Map<String, String> params) {
        return given()
                .spec(RequestSpec.json(baseUri))
                .header(bearer())
                .params(params)
                .when()
                .get(uriPath)
                .then();
    }

    public ValidatableResponse postWithAuth(String baseUri, String uriPath, Object body) {
        return post(baseUri, uriPath, body, bearer());
    }

    /** POST с одной повторной попыткой при 401 (токен протух на стороне сервера) */
    public ValidatableResponse postWithAutoRefresh(String baseUri, String uriPath, Object body) {
        ValidatableResponse response = postWithAuth(baseUri, uriPath, body);
        if (response.extract().statusCode() != 401) {
            return response;
        }
        refreshToken();
        return postWithAuth(baseUri, uriPath, body);
    }

    // ---------- Базовые запросы ----------

    public ValidatableResponse get(String baseUri, String uriPath) {
        return given().spec(RequestSpec.json(baseUri))
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse get(String baseUri, String uriPath, Header header) {
        return given().spec(RequestSpec.json(baseUri))
                .header(header)
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .when().post(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body, Header header) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .header(header)
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