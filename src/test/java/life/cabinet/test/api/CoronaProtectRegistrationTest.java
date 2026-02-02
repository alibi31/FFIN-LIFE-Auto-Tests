package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.coronaprotect.CoronaProtectRegistrationRequest;
import life.core.http.ApiClient;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.http.ContentType.JSON;

import static org.hamcrest.Matchers.*;

@Tag("cabinet-api")
@Owner("QA")
public class CoronaProtectRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("POST /api/v2/products/corona-protect/register")
    void shouldBeStatus200FromCoronaProtectRegistrationTest(){
        // Данные документов страхователя
        CoronaProtectRegistrationRequest.Document insurerDocument = CoronaProtectRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .active(true)
                .startDate("2017-09-15")
                .endDate("2027-09-14")
                .isDisabled(true)
                .date("2017-09-15")
                .build();
        // Данные документов застрахованного
        CoronaProtectRegistrationRequest.Document insuredDocument = CoronaProtectRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .active(true)
                .startDate("2017-09-15")
                .endDate("2027-09-14")
                .isDisabled(true)
                .date("2017-09-15")
                .build();
        // Данные документов выгодоприобретателя
        CoronaProtectRegistrationRequest.Document recipientDocument = CoronaProtectRegistrationRequest.Document.builder()
                .type(13310363)
                .number("042101243")
                .authority("МВД РК")
                .startDate("2017-09-15")
                .endDate("")
                .build();
        // Данные страхователя
        CoronaProtectRegistrationRequest.Insurer insurer = CoronaProtectRegistrationRequest.Insurer.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .birthDate("2001-01-31")
                .phone("+7 (747) 304-51-32")
                .email("alibi6390@gmail.com")
                .address("Улица Абиш Кекилбайулы 264")
                .doc(insurerDocument)
                .build();
        // Данные застрахованного
        CoronaProtectRegistrationRequest.Insured insured = CoronaProtectRegistrationRequest.Insured.builder()
                .uin("010131500057")
                .fullName("Қуандық Әліби Ерболұлы")
                .gender(1)
                .birthDate("2001-01-31")
                .phone("+7 (747) 304-51-32")
                .email("alibi6390@gmail.com")
                .address("Улица Абиш Кекилбайулы 264")
                .doc(insuredDocument)
                .build();
        // Данные выгодоприобретателя
        CoronaProtectRegistrationRequest.Recipient recipient = CoronaProtectRegistrationRequest.Recipient.builder()
                .uin("040308500629")
                .fullName("John Doe")
                .gender(1)
                .birthDate("2015-05-13")
                .phone("+7 (777) 777-77-77")
                .email("test@test.com")
                .address("Улица Гагарина 117")
                .doc(recipientDocument)
                .build();
        // Дополнительные опции страхования
        CoronaProtectRegistrationRequest.Options options = CoronaProtectRegistrationRequest.Options.builder()
                .invalid(false)
                .nervous(false)
                .resperatory(false)
                .cardiovascular(false)
                .build();
        // Передаем все нужные данные для отправки запроса
        CoronaProtectRegistrationRequest request = CoronaProtectRegistrationRequest.builder()
                .premium(4900)
                .notificationMethod(2)
                .options(options)
                .insurer(insurer)
                .insured(insured)
                .recipient(recipient)
                .build();
        // Получаем токен авторизации и отправляем запрос на нужный эндпоинт
        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/corona-protect/register",
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
    }
}
