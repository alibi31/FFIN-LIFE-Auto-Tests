package life.cabinet.test.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.cabinet.api.model.education.EducationRegistrationRequest;
import life.core.http.ApiClient;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.http.ContentType.JSON;

import static org.hamcrest.Matchers.*;

public class EducationRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @CancelPolicy
    @DisplayName("POST /api/v2/products/freedom-education/register")
    void shouldBeStatus200FromEducationRegistrationTest(){
        EducationRegistrationRequest.Document document = EducationRegistrationRequest.Document.builder()
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
                .doc(document)
                .build();

        EducationRegistrationRequest.Child child = EducationRegistrationRequest.Child.builder()
                .uin("150513501862")
                .fullName("John Doe")
                .gender(1)
                .birthDate("2015-05-13")
                .phone("+7 (777) 777-77-77")
                .email("test@test.com")
                .address("Улица Гагарина 117")
                .doc(document)
                .build();

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

        EducationRegistrationRequest request = EducationRegistrationRequest.builder()
                .period(4)
                .term(3)
                .premium(15000)
                .notification_method(2)
                .parent(parent)
                .child(child)
                .options(options)
                .build();

        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-education/register",
                request
        );

        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data")
                .toString().replace("OK:", "").trim();

        // Задержка в 10 секунд, чтобы аннулировать договор корректно.
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Задержка была прервана", e);
        }

        PoliciesRegistry.register(policyNumber);
    }
}
