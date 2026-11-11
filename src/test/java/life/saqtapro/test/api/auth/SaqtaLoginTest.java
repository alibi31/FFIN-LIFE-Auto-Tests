package life.saqtapro.test.api.auth;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Param;
import io.qameta.allure.Story;
import io.qameta.allure.model.Parameter;
import life.saqtapro.api.AuthApi;
import life.saqtapro.auth.TestUser;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * Авторизация сотрудника в SaqtaPro: POST /api/login {mail, password}.
 *
 * Контракт (dev-lins-refactor):
 *  - верные данные          → 200, status=true, data.token "<id>|<секрет>" + профиль сотрудника;
 *  - неверный логин/пароль  → 422, status=false, errors=["Неверный логин или пароль"];
 *  - пустой логин/пароль    → 500, status=false, message="Что-то пошло не так. Попробуйте позже."
 */
@SaqtaApiTest
@Tag("smoke")
@Tag("saqtapro-auth")
@Feature("Авторизация")
@Owner("QA")
class SaqtaLoginTest {

    private static final String INVALID_CREDENTIALS = "Неверный логин или пароль";
    private static final String SERVER_ERROR = "Что-то пошло не так. Попробуйте позже.";

    private static final String WRONG_PASSWORD = "wrong-password-autotest";
    private static final String UNKNOWN_MAIL = "nobody.autotest@ffin.life";

    private final AuthApi authApi = new AuthApi();

    // ---------- Успешный вход ----------

    @Test
    @Story("Успешный вход")
    @DisplayName("AUTH-01: верные логин и пароль → 200, токен и профиль сотрудника")
    void validCredentialsReturnTokenAndProfile() {
        TestUser user = TestUser.QA_EMPLOYEE;

        authApi.login(user.login(), user.password())
                .statusCode(200)
                .contentType(JSON)
                .body(matchesJsonSchemaInClasspath("schema/saqtapro/login.json"))
                .body("status", is(true))
                // токен Laravel Sanctum: "<id>|<секрет>"
                .body("data.token", matchesPattern("^\\d+\\|[A-Za-z0-9]{40,}$"))
                .body("data.email", equalToIgnoringCase(user.login()));
    }

    @Test
    @Story("Успешный вход")
    @DisplayName("AUTH-02: каждый вход выдаёт новый токен")
    void eachLoginIssuesNewToken() {
        TestUser user = TestUser.QA_EMPLOYEE;

        String first = tokenOf(user);
        String second = tokenOf(user);

        // сравниваем внутри Hamcrest, чтобы сами токены не печатались в отчёт целиком при успехе
        assertThat("Повторный вход вернул тот же токен", second, not(equalTo(first)));
    }

    // ---------- Неверный логин или пароль: 422 ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrongCredentials")
    @Story("Неверные учётные данные")
    @DisplayName("AUTH-03: неверный логин или пароль → 422 «Неверный логин или пароль», токен не выдан")
    @Description("Сообщение одинаковое для неверного пароля и несуществующей почты — "
            + "по ответу нельзя узнать, заведён ли сотрудник в системе")
    void wrongCredentialsAreRejected(@Param(name = "Кейс") String caseName,
                                     @Param(name = "mail") String mail,
                                     @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        authApi.login(mail, password)
                .statusCode(422)
                .contentType(JSON)
                .body("status", is(false))
                .body("message", nullValue())
                .body("errors", contains(INVALID_CREDENTIALS))
                .body("data", nullValue());
    }

    static Stream<Arguments> wrongCredentials() {
        TestUser user = TestUser.QA_EMPLOYEE;
        String password = user.password();
        Stream<Arguments> cases = Stream.of(
                Arguments.of("верная почта, неверный пароль", user.login(), WRONG_PASSWORD),
                Arguments.of("верная почта, пароль с пробелом в конце", user.login(), password + " "),
                Arguments.of("несуществующая почта, любой пароль", UNKNOWN_MAIL, WRONG_PASSWORD),
                Arguments.of("несуществующая почта, пароль реального сотрудника", UNKNOWN_MAIL, password)
        );
        // если в пароле нет букв, смена регистра ничего не меняет — такой кейс не добавляем
        String swapped = swapCase(password);
        return swapped.equals(password)
                ? cases
                : Stream.concat(cases, Stream.of(
                Arguments.of("верная почта, пароль в другом регистре", user.login(), swapped)));
    }

    // ---------- Пустой логин или пароль: 500 ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyFields")
    @Story("Пустые поля")
    @DisplayName("AUTH-04: пустой логин или пароль → 500 «Что-то пошло не так», токен не выдан")
    @Description("Фиксирует текущее поведение стенда. 500 на пустое поле — дефект backend: "
            + "ожидается 422 с ошибкой валидации. Когда backend исправят, тест упадёт — "
            + "тогда поменять ожидание на 422 и текст ошибки валидации")
    void emptyFieldsAreRejected(@Param(name = "Кейс") String caseName,
                                @Param(name = "mail") String mail,
                                @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        authApi.login(mail, password)
                .statusCode(500)
                .body("status", is(false))
                .body("message", equalTo(SERVER_ERROR))
                .body("data", nullValue());
    }

    static Stream<Arguments> emptyFields() {
        TestUser user = TestUser.QA_EMPLOYEE;
        return Stream.of(
                Arguments.of("пустая почта и пустой пароль", "", ""),
                Arguments.of("пустая почта, верный пароль", "", user.password()),
                Arguments.of("верная почта, пустой пароль", user.login(), "")
        );
    }

    // ---------- Безопасность ----------

    @ParameterizedTest(name = "mail=«{0}»")
    @MethodSource("injections")
    @Story("Безопасность")
    @DisplayName("AUTH-05: SQL/NoSQL-инъекция в почте не даёт войти")
    void injectionDoesNotAuthenticate(@Param(name = "mail") String mail) {
        authApi.login(mail, WRONG_PASSWORD)
                .statusCode(not(200))
                .body("status", is(false))
                .body("data.token", nullValue());
    }

    static Stream<String> injections() {
        return Stream.of(
                "' OR '1'='1",
                "' OR 1=1 --",
                TestUser.QA_EMPLOYEE.login() + "' --",
                "{\"$ne\": null}"
        );
    }

    // ---------- helpers ----------

    private String tokenOf(TestUser user) {
        return authApi.login(user.login(), user.password())
                .statusCode(200)
                .extract()
                .path("data.token");
    }

    private static String swapCase(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return sb.toString();
    }
}