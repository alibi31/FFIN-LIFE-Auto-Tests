package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.first.FirstRegistrationRequest;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.http.ContentType.JSON;

import static org.hamcrest.Matchers.*;

@Tag("cabinet-api")
@Owner("QA")
public class FirstRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("POST /api/v2/products/freedom-first/register")
    void shouldBeStatus200FromFirstRegistrationTest(){
        // Данные документов застрахованного
        FirstRegistrationRequest.Document insuredDocument = FirstRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .date("2017-09-15")
                .build();
        // Данные документов выгодоприобретателя
        FirstRegistrationRequest.Document beneficiaryDocument = FirstRegistrationRequest.Document.builder()
                .number("111111111")
                .type(13310363)
                .authority("МВД РК")
                .date("2025-02-20")
                .build();
        // Данные застрахованного
        FirstRegistrationRequest.Insured insured = FirstRegistrationRequest.Insured.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .birthDate("2001-01-31")
                .phone("+7 (747) 304-51-32")
                .email("alibi6390@gmail.com")
                .address("Улица Абиш Кекилбайулы 264")
                .isNotKzResident(false)
                .doc(insuredDocument)
                .build();
        // Данные выгодоприобретателя
        FirstRegistrationRequest.Beneficiary beneficiary = FirstRegistrationRequest.Beneficiary.builder()
                .uin("040308500629")
                .fullName("Зайнахунов Медат Шухратулы")
                .gender(1)
                .birthDate("2004-03-08")
                .phone("+7 (777) 777-77-77")
                .email("test@test.com")
                .address("Каблукова 129")
                .isNotKzResident(false)
                .doc(beneficiaryDocument)
                .build();
        // Передаем все нужные данные для отправки запроса
        FirstRegistrationRequest request = FirstRegistrationRequest.builder()
                .insurancePremium(250)
                .insurancePremiumKzt(126763)
                .notificationMethod(2)
                .icSum(250)
                .term(5)
                .agentFullName("")
                .insured(insured)
                .beneficiary(beneficiary)
                .build();
        // Получаем токен авторизации и отправляем запрос на нужный эндпоинт
        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-first/register",
                request
        );
        // Получем ответ после отправки запроса, проверяем ответ на корректность
        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data")
                .toString().replace("OK:", "").trim();
        ContractCleanupTest.collectPolicyNumbers(List.of(policyNumber));
    }
}
