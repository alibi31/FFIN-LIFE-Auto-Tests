package life.saqtapro.test.api.auth;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Param;
import io.qameta.allure.Story;
import io.qameta.allure.model.Parameter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.AuthApi;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static life.saqtapro.auth.TestUser.QA_EMPLOYEE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * Профиль текущего сотрудника: GET /api/me с заголовком Authorization: Bearer &lt;токен&gt;.
 *
 * Контракт (dev-lins-refactor):
 *  - действующий токен                      → 200, status=true, профиль владельца токена (без token);
 *  - нет токена / некорректный / чужой      → 401, status=false, message="Вы не авторизованы."
 *
 * Тесты только входят успешно, неверных паролей здесь нет — учётку QA_EMPLOYEE они не блокируют.
 */
@SaqtaApiTest
@Tag("smoke")
@Tag("saqtapro-auth")
@Feature("Авторизация")
@Owner("QA")
class SaqtaMeTest {

    private static final String UNAUTHORIZED = "Вы не авторизованы.";

    /** Поля профиля, которые /api/me обязан вернуть такими же, как при входе */
    private static final String[] PROFILE_FIELDS = {
            "id", "name", "email", "department", "department_slug", "position",
            "branch.id", "branch.name_ru", "can_access_admin"
    };

    // ---------- Действующий токен ----------

    @Test
    @Story("Профиль сотрудника")
    @DisplayName("ME-01: действующий токен → 200, профиль соответствует схеме и владельцу токена")
    void validTokenReturnsProfile(@As(QA_EMPLOYEE) SaqtaApi employee) {
        employee.profile().me()
                .statusCode(200)
                .contentType(JSON)
                .body(matchesJsonSchemaInClasspath("schema/saqtapro/me.json"))
                .body("status", is(true))
                .body("data.email", equalToIgnoringCase(QA_EMPLOYEE.login()));
    }

    @Test
    @Story("Профиль сотрудника")
    @DisplayName("ME-02: /api/me возвращает тот же профиль, что и вход, и не возвращает токен")
    void profileMatchesLoginAndHidesToken() {
        JsonPath login = new AuthApi().login(QA_EMPLOYEE.login(), QA_EMPLOYEE.password())
                .statusCode(200)
                .extract().jsonPath();

        ValidatableResponse me = SaqtaApi.withRawToken(login.getString("data.token")).profile().me()
                .statusCode(200)
                // токен отдаётся только при входе: в профиле его быть не должно
                .body("data", not(hasKey("token")));

        for (String field : PROFILE_FIELDS) {
            Object expected = login.get("data." + field);
            me.body("data." + field, equalTo(expected));
        }
    }

    // ---------- Нет токена или он некорректный: 401 ----------

    @Test
    @Story("Без авторизации")
    @DisplayName("ME-03: без заголовка Authorization → 401 «Вы не авторизованы.»")
    void noTokenIsUnauthorized() {
        assertUnauthorized(SaqtaApi.anonymous().profile().me());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("malformedHeaders")
    @Story("Без авторизации")
    @DisplayName("ME-04: некорректный заголовок Authorization → 401 «Вы не авторизованы.»")
    void malformedHeaderIsUnauthorized(@Param(name = "Кейс") String caseName,
                                       @Param(name = "Authorization", mode = Parameter.Mode.MASKED) String header) {
        assertUnauthorized(SaqtaApi.withAuthorizationHeader(header).profile().me());
    }

    static Stream<Arguments> malformedHeaders() {
        return Stream.of(
                Arguments.of("пустой Bearer", "Bearer "),
                Arguments.of("Bearer со случайной строкой", "Bearer not-a-token"),
                Arguments.of("несуществующий токен в формате Sanctum",
                        "Bearer 999999999|" + "a".repeat(40)),
                Arguments.of("Basic вместо Bearer", "Basic dGVzdDp0ZXN0")
        );
    }

    /** Подделки, собранные из настоящего токена QA_EMPLOYEE вида "<id>|<секрет>" */
    enum Tampering {
        WITHOUT_BEARER_PREFIX("действующий токен без префикса Bearer", false, t -> t),
        WRONG_SECRET("верный id токена, чужой секрет", true, t -> idOf(t) + "|" + "a".repeat(secretOf(t).length())),
        FOREIGN_ID("секрет действующего токена с чужим id", true, t -> (Long.parseLong(idOf(t)) - 1) + "|" + secretOf(t)),
        TRUNCATED("обрезанный токен", true, t -> t.substring(0, t.length() - 1));

        private final String title;
        private final boolean withBearer;
        private final UnaryOperator<String> tamper;

        Tampering(String title, boolean withBearer, UnaryOperator<String> tamper) {
            this.title = title;
            this.withBearer = withBearer;
            this.tamper = tamper;
        }

        String authorizationHeader(String realToken) {
            String credentials = tamper.apply(realToken);
            return withBearer ? "Bearer " + credentials : credentials;
        }

        private static String idOf(String token) {
            return token.substring(0, token.indexOf('|'));
        }

        private static String secretOf(String token) {
            return token.substring(token.indexOf('|') + 1);
        }

        @Override
        public String toString() {
            return title;
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Tampering.class)
    @Story("Без авторизации")
    @DisplayName("ME-05: подделанный или чужой токен → 401 «Вы не авторизованы.»")
    void tamperedTokenIsUnauthorized(@Param(name = "Подделка") Tampering tampering) {
        String realToken = SaqtaAuth.tokenOf(QA_EMPLOYEE);

        assertUnauthorized(SaqtaApi.withAuthorizationHeader(tampering.authorizationHeader(realToken))
                .profile().me());
    }

    // ---------- helpers ----------

    private static void assertUnauthorized(ValidatableResponse response) {
        response.statusCode(401)
                .contentType(JSON)
                .body("status", is(false))
                .body("message", equalTo(UNAUTHORIZED))
                .body("$", not(hasKey("data")));
    }
}
