package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.cabinet.api.model.health.HealthRegistrationRequest;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.http.ContentType.JSON;

import static org.hamcrest.Matchers.*;

@Tag("cabinet-api")
@Owner("QA")
public class HealthRegistrationTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @CancelPolicy
    @DisplayName("POST /api/v2/products/freedom-health/register")
    void shouldBeStatus200FromHealthRegistrationTest() {

        HealthRegistrationRequest.Document document = HealthRegistrationRequest.Document.builder()
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
                .doc(document)
                .build();

        HealthRegistrationRequest.Insured insured = HealthRegistrationRequest.Insured.builder()
                .fullName("Қуандық Әліби Ерболұлы")
                .uin("010131500057")
                .gender(1)
                .phone("+7 (747) 304-51-32")
                .birthDate("2001-01-31")
                .address("Алматы, Улица Абиш Кекилбайулы, 264")
                .email("alibi6390@gmail.com")
                .doc(document)
                .build();

        HealthRegistrationRequest request = HealthRegistrationRequest.builder()
                .program(5)
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

        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/freedom-health/register",
                request
        );

        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data")
                .toString().replace("OK:", "").trim();

        PoliciesRegistry.register(policyNumber);
    }

}
