package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.education.EducationRegistrationRequest;
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
public class EducationRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("POST /api/v2/products/freedom-education/register")
    void shouldBeStatus200FromEducationRegistrationTest(){
        // Данные документов родителя
        EducationRegistrationRequest.Document parentDocument = EducationRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .date("2017-09-15")
                .build();
        // Данные документов ребёнка
        EducationRegistrationRequest.Document childDocument = EducationRegistrationRequest.Document.builder()
                .number("042101243")
                .type(13310363)
                .authority("МВД РК")
                .date("2017-09-15")
                .build();
        // Данные родителя
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
        // Данные ребёнка
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
        EducationRegistrationRequest request = EducationRegistrationRequest.builder()
                .period(4)
                .term(3)
                .premium(15000)
                .notificationMethod(2)
                .parent(parent)
                .child(child)
                .options(options)
                .build();
        // Получаем токен авторизации и отправляем запрос на нужный эндпоинт
        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-education/register",
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
