package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.jurek.JurekRegistrationRequest;
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
public class JurekRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("POST /api/v2/products/freedom-jurek/register")
    void shouldBeStatus200FromJurekRegistrationTest(){
        // Данные документов страхователя
        JurekRegistrationRequest.Document insurerDocument = JurekRegistrationRequest.Document.builder()
                .type(13310363)
                .number("042101243")
                .authority("МВД РК")
                .date("2017-09-15")
                .build();
        // Данные документов застрахованного
        JurekRegistrationRequest.Document insuredDocument = JurekRegistrationRequest.Document.builder()
                .type(13310363)
                .number("042101243")
                .authority("МВД РК")
                .date("2017-09-15")
                .build();
        // Данные документов выгодоприобретателя
        JurekRegistrationRequest.Document beneficiaryDocument = JurekRegistrationRequest.Document.builder()
                .type(13310363)
                .number("111111111")
                .authority("МВД РК")
                .date("2025-02-20")
                .build();
        // Данные страхователя
        JurekRegistrationRequest.Insurer insurer = JurekRegistrationRequest.Insurer.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .phone("+7 (747) 304-51-32")
                .birthDate("2001-01-31")
                .address("Улица Абиш Кекилбайулы 264")
                .email("alibi6390@gmail.com")
                .doc(insuredDocument)
                .build();
        // Данные застрахованного
        JurekRegistrationRequest.Insured insured = JurekRegistrationRequest.Insured.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .phone("+7 (747) 304-51-32")
                .birthDate("2001-01-31")
                .address("Улица Абиш Кекилбайулы 264")
                .email("alibi6390@gmail.com")
                .doc(insuredDocument)
                .build();
        // Данные выгодоприобретателя
        JurekRegistrationRequest.Beneficiary beneficiary = JurekRegistrationRequest.Beneficiary.builder()
                .uin("040308500629")
                .fullName("Зайнахунов Медат Шухратулы")
                .gender(1)
                .phone("+7 (777) 777-77-77")
                .birthDate("2004-03-08")
                .address("Каблукова 129")
                .email("test@test.com")
                .doc(beneficiaryDocument)
                .build();
        // Передаем все нужные данные для отправки запроса
        JurekRegistrationRequest request = JurekRegistrationRequest.builder()
                .insurer(insurer)
                .insured(insured)
                .beneficiary(beneficiary)
                .notificationMethod(2)
                .paymentPeriodicity(3)
                .guaranteedPeriod("2")
                .incomingAmount(100000)
                .premiumCalculation(false)
                .bankId("407036346")
                .iban("KZ84551A600000218365")
                .usd(true)
                .periodAnnuityPaymentsYears(5)
                .build();
        // Получаем токен авторизации и отправляем запрос на нужный эндпоинт
        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-jurek/register",
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
