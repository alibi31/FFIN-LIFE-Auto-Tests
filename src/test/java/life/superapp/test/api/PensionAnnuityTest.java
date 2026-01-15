package life.superapp.test.api;

import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.restassured.http.Header;
import life.core.http.ApiClient;
import life.superapp.api.model.calculation.CalculationRequest;
import life.superapp.api.model.auth.OttGatewayRequest;
import life.superapp.api.model.registration.RegistrationRequest;
import life.superapp.jupiter.annotation.AccessToken;
import life.superapp.jupiter.annotation.Auth;
import life.superapp.jupiter.annotation.OneTimeToken;
import life.utils.config.EnvConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Tag("PensionAnnuity")
@Owner("QA")
public class PensionAnnuityTest {

    private final ApiClient apiClient = new ApiClient();

    private final String baseUrl = EnvConfig.cfg().apiSuperAppUrl();

    @Test
    @DisplayName("POST /api/auth/gateway")
    @Description("[Авторизация] Получение одноразового токена")
    void getOneTimeTokenTest() {

        OttGatewayRequest request = new OttGatewayRequest(
                "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА",
                "700511402493",
                "+77772145973",
                "1970-05-11",
                "F",
                "drr@mail.ru",
                List.of(new OttGatewayRequest.Document(
                                "054041413",
                                "04",
                                "МВД РК",
                                "2023-08-09"
                        )
                )
        );

        Header header = new Header("X-Gateway-Secret", "life_test");

        apiClient.post(baseUrl, "auth/gateway", request, header)
                .statusCode(200)
                .body("data.size()", greaterThan(0))
                .body("data", not(empty()));
    }


    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("POST /api/auth/user")
    @Description("[Кросс-Авторизация] Получение access token")
    void getAccessTokenTest(@OneTimeToken String token){
        apiClient.post(baseUrl,
                "auth/user",  """
                {"one_time_token": "%s"}
                """.formatted(token))
                .statusCode(200)
                .body("data.access_token", allOf(not(emptyString())));

    }

    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("GET /api/auth/me")
    @Description("Получение данных пользователя, проверка статуса, и что data.uin " +
            "и data.full_name совпадают c iin/fullName из аннотации")
    void getUserInfoTest(@AccessToken String accessToken, TestInfo testInfo) {
        Auth auth = testInfo.getTestMethod()
                .orElseThrow()
                .getAnnotation(Auth.class);

        String expectedIin = auth.iin();
        String expectedFullName = auth.fullName();

        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.get(baseUrl, "auth/me", header)
                .statusCode(200)
                .body("status", is(true))
                .body("data.uin", equalTo(expectedIin))
                .body("data.full_name", equalTo(expectedFullName))
                .body("data.id", notNullValue())
                .body("data.docs", notNullValue());
    }

    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("GET /api/auth/me/bank-accounts")
    @Description("Получение банковские данные: " +
            "Проверка статуса, что data.cardNumber не пустые и соответствие json схемы")
    void getBankAccountsTest(@AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.get(baseUrl, "auth/me/bank-accounts", header)
                .statusCode(200)
                .body("data.cardNumber", notNullValue())
                .body(matchesJsonSchemaInClasspath("schema/superApp/bank-accounts.json"));
    }

    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("GET /api/auth/me/disability")
    @Description("Получение информации об инвалидности: " +
            "Проверка статуса, что для пользователя 'group' = 0 и 'code' = 'ok' и соответствие json схемы ")
    void getDisabilityTest(@AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.get(baseUrl, "auth/me/disability", header)
                .statusCode(200)
                .body("data.code", equalTo("ok"))
                .body("data.group", equalTo(0))
                .body(matchesJsonSchemaInClasspath("schema/superApp/disability.json"));
    }

    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("GET /api/default")
    void getDefaultTest(@AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.get(baseUrl, "default", header)
                .statusCode(200)
                .body("data", notNullValue())
                .body(matchesJsonSchemaInClasspath("schema/superApp/default.json"));
    }

    @Test
    @DisplayName("POST /api/pension-annuity/calculation")
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @Description("Калькуляция: Все значения 0 и инвалидности нет")
    void calculationNullTest(@AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        CalculationRequest request = new CalculationRequest(
                "0",
                "0",
                "0",
                "0",
                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                "ok",
                "0"
        );

        apiClient.post(baseUrl, "pension-annuity/calculation", request, header)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(false))
                .body("data", notNullValue());

    }

    @Test
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("POST /api/pension-annuity/validation")
    void validationTest(@AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.post(baseUrl, "pension-annuity/validation", "{}", header)
                .statusCode(200)
                .body("status", is(true))
                .body("data", notNullValue())
                .body("data.age.valid", is(true))
                .body("data.doc.valid", is(true))
                .body(matchesJsonSchemaInClasspath("schema/superApp/validation.json"));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("calculationPositiveCases")
    @DisplayName("POST /api/pension-annuity/calculation")
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    void calculationTest(String caseName, CalculationRequest request, @AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.post(baseUrl, "pension-annuity/calculation", request, header)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body("data", notNullValue())
                .body(matchesJsonSchemaInClasspath("schema/superApp/calculation.json"));

    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("calculationNegativeCases")
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("POST /api/pension-annuity/calculation: невалидные входные данные")
    void calculationNegativeTest(String caseName, CalculationRequest request, int expectedStatus, @AccessToken String accessToken) {
        Header auth = new Header("X-Auth-Token", accessToken);

        apiClient.post(baseUrl, "pension-annuity/calculation", request, auth)
                .statusCode(expectedStatus)
                .contentType(JSON)
                .body("status", anyOf(is(false), nullValue()))
                .body("message", notNullValue())
                .body(matchesJsonSchemaInClasspath("schema/superApp/calculation-error.json"));
    }

    @Disabled
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("registrationPositiveCases")
    @Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
    @DisplayName("POST /api/pension-annuity/registration")
    void registrationTest(String caseName, RegistrationRequest request, @AccessToken String accessToken) {
        Header header = new Header("X-Auth-Token", accessToken);

        apiClient.post(baseUrl, "pension-annuity/registration", request, header)
                .statusCode(201)
                .contentType(JSON)
                .body("status", is(true))
                .body("data", notNullValue())
                .body(matchesJsonSchemaInClasspath("schema/superApp/registration.json"));

    }


    @NotNull
    static Stream<Object[]> registrationPositiveCases() {
        return Stream.of(
                new Object[] {
                        "ОПВ = 1 000 000 000, Нет инвалидности",
                        new RegistrationRequest(
                                "1000000000",
                                null,
                                null,
                                null,
                                null,
                                "OP8LBCW8N8P1U5CO",
                                "test@test.con",
                                new RegistrationRequest.Disability(
                                        "0",
                                        "2025-11-12",
                                        null
                                ),
                                null,
                                null,
                                new RegistrationRequest.Doc(
                                        "823091",
                                        "passport",
                                        "2025-10-02",
                                        "МВД РК"
                                )
                        )
                },
                new Object[] {
                        "ОПВ = 1 000 000 000, Гарантированный период = 10, Собственные средства = 1000, Нет инвалидности",
                        new RegistrationRequest(
                                "1000000000",
                                null,
                                "1000",
                                "10",
                                null,
                                "OP8LBCW8N8P1U5CO",
                                "test@test.con",
                                null,
                                new RegistrationRequest.Beneficiary(
                                        "040910203992",
                                        "Тест Тестович",
                                        "+77117117111",
                                        "m",
                                        "2004-09-10",
                                        "test@test.com",
                                        "Тест, 123",
                                        new RegistrationRequest.Doc(
                                                "823091",
                                                "passport",
                                                "2025-10-02",
                                                "МВД РК"
                                        )
                                ),
                                null,
                                new RegistrationRequest.Doc(
                                        "823091",
                                        "passport",
                                        "2025-10-02",
                                        "МВД РК"
                                )
                        )
                },
                new Object[] {
                        "ОПВ = 1 000 000 000, ДПВ = 1 000 000, Собственные средства = 1000, Нет инвалидности",
                        new RegistrationRequest(
                                "1000000000",
                                "1000000",
                                "1000",
                                null,
                                null,
                                "OP8LBCW8N8P1U5CO",
                                "test@test.con",
                                new RegistrationRequest.Disability(
                                        "0",
                                        "2025-11-12",
                                        null
                                ),
                                null,
                                null,
                                new RegistrationRequest.Doc(
                                        "823091",
                                        "passport",
                                        "2025-10-02",
                                        "МВД РК"
                                )
                        )
                },
                new Object[] {
                        "ОПВ = 1 000 000 000, Собственные средства = 1000, Нет инвалидности",
                        new RegistrationRequest(
                                "1000000000",
                                null,
                                "1000",
                                null,
                                null,
                                "OP8LBCW8N8P1U5CO",
                                "test@test.con",
                                new RegistrationRequest.Disability(
                                        "0",
                                        "2025-11-12",
                                        null
                                ),
                                null,
                                null,
                                new RegistrationRequest.Doc(
                                        "823091",
                                        "passport",
                                        "2025-10-02",
                                        "МВД РК"
                                )
                        )
                },
                new Object[] {
                        "ОПВ = 1 000 000 000, Средства из другой КСЖ = 1000, Нет инвалидности",
                        new RegistrationRequest(
                                "1000000000",
                                null,
                                null,
                                null,
                                null,
                                "OP8LBCW8N8P1U5CO",
                                "test@test.con",
                                new RegistrationRequest.Disability(
                                        "0",
                                        "2025-11-12",
                                        null
                                ),
                                null,
                                new RegistrationRequest.PreviousContract(
                                        "OHS387200",
                                        "nomad-life",
                                        "1000000",
                                        "2025-10-20"
                                ),
                                new RegistrationRequest.Doc(
                                        "823091",
                                        "passport",
                                        "2025-10-02",
                                        "МВД РК"
                                )
                        )
                }
        );
    }

    @NotNull
    static Stream<Object[]> calculationPositiveCases() {
        return Stream.of(
                new Object[]{
                        "ОПВ = 1 000 000 000, Нет инвалидности",
                        new CalculationRequest(
                                "1000000000", "0", "0", "0",
                                null,
                                "ok",
                                "0"
                        )
                },
                new Object[]{
                        "ОПВ = 1 000 000 000, ДПВ = 100 000, Собсвтенные средства = 10 000, Средства КСЖ = 100 000,  Нет инвалидонсти",
                        new CalculationRequest(
                                "1000000000",
                                "10000",
                                "5000",
                                "0",
                                null,
                                "ok",
                                "100000"
                        )
                },
                new Object[]{
                        "ОПВ = 1 000 000 000, ДПВ = 100 000, Собсвтенные средства = 10 000, Средства КСЖ = 100 000,  Инвалидность 1 группы",
                        new CalculationRequest(
                                "1000000000",
                                "10000",
                                "5000",
                                "0",
                                null,
                                "disability_degree_1",
                                "100000"
                        )
                },
                new Object[]{
                        "ОПВ = 1 000 000 000, ДПВ = 100 000, Собсвтенные средства = 10 000, Средства КСЖ = 100 000,  Инвалидность 2 группы",
                        new CalculationRequest(
                                "1000000000",
                                "10000",
                                "5000",
                                "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "disability_degree_2",
                                "100000"
                        )
                },
                new Object[]{
                        "ОПВ = 1 000 000 000, ДПВ = 100 000, Собсвтенные средства = 10 000, Средства КСЖ = 100 000,  Инвалидность 3 группы",
                        new CalculationRequest(
                                "1000000000",
                                "10000",
                                "5000",
                                "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "disability_degree_3",
                                "100000"
                        )
                },
                new Object[]{
                        "ОПВ = 1 000 000 000, Гарантированный срок = 12, Нет инвалидности",
                        new CalculationRequest(
                                "1000000000",
                                "0",
                                "0",
                                "12",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "disability_degree_3",
                                "0"
                        )
                }
        );
    }

    @NotNull
    static Stream<Object[]> calculationNegativeCases() {
        return Stream.of(
                new Object[]{
                        "Отрицательные суммы (mandatory_pension_contributions < 0)",
                        new CalculationRequest(
                                "-1", "0", "0", "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "ok",
                                "0"
                        ),
                        422
                },
                new Object[]{
                        "Отрицательные суммы (voluntary_pension_contributions < 0)",
                        new CalculationRequest(
                                "10000000", "-1", "0", "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "ok",
                                "0"
                        ),
                        422
                },
                new Object[]{
                        "Отрицательные суммы (own_expenses < 0)",
                        new CalculationRequest(
                                "10000000", "0", "-1", "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "ok",
                                "0"
                        ),
                        422
                },
                new Object[]{
                        "Отрицательный гарантированный период (guaranteed_period_years < 0)",
                        new CalculationRequest(
                                "10000000", "0", "0", "-1",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "ok",
                                "0"
                        ),
                        422
                },
                new Object[]{
                        "Отрицательные суммы (other_life_insurance_company_redemption < 0)",
                        new CalculationRequest(
                                "10000000", "0", "0", "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "ok",
                                "-1"
                        ),
                        422
                },
                new Object[]{
                        "Некорректное значение disability",
                        new CalculationRequest(
                                "0", "0", "0", "0",
                                new CalculationRequest.MandatoryProfessionalPensionContributions("0", "0"),
                                "unknown",
                                "0"
                        ),
                        422
                }

        );
    }

}
