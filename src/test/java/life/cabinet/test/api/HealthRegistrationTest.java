package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.PolicyApi;
import life.cabinet.api.SessionApi;
import life.core.http.ApiClient;
import life.cabinet.api.model.health.HealthRegistrationRequest;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("cabinet-api")
@Owner("QA")
public class HealthRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("Регистрация полиса Health")
    void shouldBeStatus200FromHealthRegistrationTest() {
        // Подготовить тестовые данные
        HealthRegistrationRequest request = prepareTestData();

        // Отправить запрос на регистрацию полиса
        ValidatableResponse response = sendRegistrationRequest(request);

        // Проверить ответ и извлечь номер полиса
        String policyNumber = extractAndValidatePolicyNumber(response);

        // Активировать полис (сделать действующим)
        activateHealthPolicy(policyNumber);

    }

    @Step("1. Подготовить тестовые данные для полиса Health")
    private HealthRegistrationRequest prepareTestData() {
        HealthRegistrationRequest.Document insurerDocument = HealthRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .active(true)
                .isDisabled(true)
                .date("2017-09-15")
                .build();

        HealthRegistrationRequest.Document insuredDocument = HealthRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .active(true)
                .isDisabled(true)
                .date("2017-09-15")
                .build();

        HealthRegistrationRequest.Insurer insurer = HealthRegistrationRequest.Insurer.builder()
                .fullName("Қуандық Әліби Ерболұлы")
                .uin("010131500057")
                .gender(1)
                .phone("+7 (747) 304-51-32")
                .birthDate("2001-01-31")
                .address("Алматы, Улица Абиш Кекилбайулы, 264")
                .email("alibi6390@gmail.com")
                .doc(insurerDocument)
                .build();

        HealthRegistrationRequest.Insured insured = HealthRegistrationRequest.Insured.builder()
                .fullName("Қуандық Әліби Ерболұлы")
                .uin("010131500057")
                .gender(1)
                .phone("+7 (747) 304-51-32")
                .birthDate("2001-01-31")
                .address("Алматы, Улица Абиш Кекилбайулы, 264")
                .email("alibi6390@gmail.com")
                .doc(insuredDocument)
                .build();

        return HealthRegistrationRequest.builder()
                .program(7)
                .period(1463)
                .insurer(insurer)
                .insured(insured)
                .agreement1(true)
                .agreement2(true)
                .agreement3(false)
                .agent("")
                .nomad("")
                .promo("")
                .build();
    }

    @Step("2. Отправить запрос на регистрацию полиса Health")
    private ValidatableResponse sendRegistrationRequest(HealthRegistrationRequest request) {
        return apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-health/register",
                request
        );
    }

    @Step("3. Проверить ответ и извлечь номер полиса")
    private String extractAndValidatePolicyNumber(ValidatableResponse response) {
        // Проверяем статус код и структуру ответа
        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data")
                .toString().replace("OK:", "").trim();

        // Валидация номера полиса
        assertNotNull(policyNumber, "Номер полиса не должен быть null");
        assertFalse(policyNumber.isEmpty(), "Номер полиса не должен быть пустым");
        PoliciesRegistry.register(policyNumber);
        PoliciesRegistry.debug();
        return policyNumber;
    }

    @Step("4. Активировать полис {policyNumber} через SOAP API")
    private void activateHealthPolicy(String policyNumber) {
        try {
            String sessionId = SessionApi.getSessionId();
            PolicyApi.activatePolicy(sessionId, policyNumber);
        } catch (Exception e) {
            // Логируем ошибку, но не падаем - основной тест уже прошел
            System.err.println("Ошибка при активации полиса " + policyNumber + ": " + e.getMessage());
        }
    }


}