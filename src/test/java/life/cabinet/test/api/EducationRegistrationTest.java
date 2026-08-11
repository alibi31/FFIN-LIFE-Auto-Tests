package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.PolicyApi;
import life.cabinet.api.SessionApi;
import life.core.http.ApiClient;
import life.cabinet.api.model.education.EducationRegistrationRequest;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("cabinet-api")
@Owner("QA")
public class EducationRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("POST /api/v2/products/freedom-education/register")
    void shouldBeStatus200FromEducationRegistrationTest() {

        EducationRegistrationRequest request = prepareTestData();

        ValidatableResponse response = sendRegistrationRequest(request);

        String policyNumber = extractAndValidatePolicyNumber(response);

        activateEducationPolicy(policyNumber);
    }

    @Step("1. Подготовить тестовые данные для полиса Education")
    private EducationRegistrationRequest prepareTestData() {
        EducationRegistrationRequest.Document parentDocument = EducationRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .date("2017-09-15")
                .build();

        EducationRegistrationRequest.Document childDocument = EducationRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .date("2017-09-15")
                .build();

        EducationRegistrationRequest.Parent parent = EducationRegistrationRequest.Parent.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .birthDate("2001-01-31")
                .phone("+7 (747) 304-51-32")
                .email("alibi6390@gmail.com")
                .address("Улица Абиш Кекилбайулы 264")
                .doc(parentDocument)
                .build();

        EducationRegistrationRequest.Child child = EducationRegistrationRequest.Child.builder()
                .uin("150513501862")
                .fullName("John Doe")
                .gender(1)
                .birthDate("2015-05-13")
                .phone("+7 (777) 777-77-77")
                .email("test@test.com")
                .address("Улица Гагарина 117")
                .doc(childDocument)
                .build();

        // Дополнительные опции страхования
        EducationRegistrationRequest.Options options = EducationRegistrationRequest.Options.builder()
                .deathDueToAccident(false)
                .disabilityDueToAccident(false)
                .bodilyInjury(false)
                .criticalIllness(false)
                .temporaryIncapacityForWork(false)
                .hospitalizationAsResultOfAccident(false)
                .childBodilyInjury(false)
                .childCriticalIllness(false)
                .build();
        // Передаем все нужные данные для отправки запроса
        return EducationRegistrationRequest.builder()
                .period(4)
                .term(3)
                .premium(15000)
                .notificationMethod(2)
                .parent(parent)
                .child(child)
                .options(options)
                .build();
    }

    @Step("2. Отправить запрос на регистрацию полиса Education")
    private ValidatableResponse sendRegistrationRequest(EducationRegistrationRequest request){
        return apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-education/register",
                request
        );
    }

    @Step("3. Проверить ответ и извлечь номер полиса")
    private String extractAndValidatePolicyNumber(ValidatableResponse response){
        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data")
                .toString().replace("OK:", "").trim();

        assertNotNull(policyNumber, "Номер полиса не должен быть null");
        assertFalse(policyNumber.isEmpty(), "Номер полиса не должен быть пустым");
        PoliciesRegistry.register(policyNumber);
        PoliciesRegistry.debug();

        try {
            TimeUnit.SECONDS.sleep(90);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Задержка была прервана", e);
        }

        return policyNumber;
    }

    @Step("4. Активировать полис {policyNumber} через SOAP API")
    private void activateEducationPolicy(String policyNumber){
        try {
            String sessionId = SessionApi.getSessionId();
            PolicyApi.activatePolicy(sessionId, policyNumber);
        } catch (Exception e) {
            // Логируем ошибку, но не падаем - основной тест уже прошел
            System.err.println("Ошибка при активации полиса " + policyNumber + ": " + e.getMessage());
        }
    }

}