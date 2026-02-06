package life.ffin.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.core.jupiter.annotation.CancelPolicy;
import life.ffin.api.model.urban.UrbanRegistrationRequest;
import life.utils.PoliciesRegistry;
import life.utils.TestDataStorage;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;

@Tag("ffin-api")
@Owner("QA")
public class UrbanRegistrationTest {

    private final ApiClient apiClient = new ApiClient();
    private final String apiFfinUrl = EnvConfig.cfg().apiFfinUrl();

    @Test
//    @CancelPolicy
    @DisplayName("POST /api/v1/products/freedom-urban/registration")
    void shouldBeStatus200FromRegistrationTest() {

        UrbanRegistrationRequest request = UrbanRegistrationRequest.builder()
                .insuredType("self")
                .insuranceSum(1000000)
                .term(1)
                .period(2)
                .insurancePremium(6000)
                .confirmRepresentative(true)
                .insurerIin("010131500057")
                .insurerFullName("ҚУАНДЫҚ ӘЛІБИ ЕРБОЛҰЛЫ")
                .insurerPhone("+7 (747) 304-51-32")
                .insurerAddress("АЛМАТЫ, УЛИЦА Абиш Кекилбайулы, 264")
                .insurerEmail("alibi6390@gmail.com")
                .insurerDocType("13310363")
                .insurerDocNumber("042101243")
                .insurerDocIssueDate("2017-09-15")
                .insurerDocEndDate("2027-09-14")
                .insurerDocIssuer("МВД РК")
                .notificationMethod(2)
                .agreement(true)
                .insurerSex(1)
                .insurerBirthDate("2001-01-31")
                .insuredIin("010131500057")
                .insuredFullName("ҚУАНДЫҚ ӘЛІБИ ЕРБОЛҰЛЫ")
                .insuredSex(1)
                .insuredBirthDate("2001-01-31")
                .insuredPhone("+7 (747) 304-51-32")
                .insuredAddress("АЛМАТЫ, УЛИЦА Абиш Кекилбайулы, 264")
                .insuredEmail("alibi6390@gmail.com")
                .insuredDocType("13310363")
                .insuredDocNumber("042101243")
                .insuredDocIssueDate("2017-09-15")
                .insuredDocEndDate("2027-09-14")
                .insuredDocIssuer("МВД РК")
                .policyType("URS")
                .resource("test")
                .build();

        ValidatableResponse response = apiClient.post(
                apiFfinUrl,
                "v1/products/freedom-urban/registration",
                request
        );

        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("data.policy_number")
                .toString().replace("OK:", "").trim();

        PoliciesRegistry.register(policyNumber);

        TestDataStorage.save("urban_policy_number", policyNumber);
    }
}
