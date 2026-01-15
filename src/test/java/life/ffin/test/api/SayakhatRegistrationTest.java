package life.ffin.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.ffin.api.model.saykhat.SayakhatRegistrationRequest;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.http.ContentType.JSON;

import static org.hamcrest.Matchers.*;


@Tag("ffin-api")
@Owner("QA")
public class SayakhatRegistrationTest {

    private final ApiClient apiClient = new ApiClient();
    private final String apiFfinUrl = EnvConfig.cfg().apiFfinUrl();


    @Test
    @CancelPolicy
    @DisplayName("POST /api/v1/products/freedom-travel/registration")
    void shouldBeStatus200FromRegistrationTest() {

        String dateStart = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateEnd = LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        SayakhatRegistrationRequest request = SayakhatRegistrationRequest.builder()
                .country1("374957920")
                .fioKir("КУАНДЫК АЛИБИ")
                .iin("010131500057")
                .iin2("010131500057")
                .sex("1")
                .sex2("1")
                .curRate("20000")
                .dateBirth("2001-01-31")
                .dateBirth2("2001-01-31")
                .dateStart(dateStart)
                .dateEnd(dateEnd)
                .address("Тест 123")
                .address2("Тест 123")
                .passportNum("N33823838")
                .passportNum2("N33823838")
                .passportGive("МВД РК")
                .passportGive2("МВД РК")
                .passportDate("2024-02-10")
                .passportDate2("2024-02-10")
                .passportDateEnd("2034-02-09")
                .passportDateEnd2("2034-02-09")
                .mobilePhone("+7 (777) 777-77-77")
                .mobilePhone2("+7 (777) 777-77-77")
                .phone("+7 (777) 777-77-77")
                .phone2("+7 (777) 777-77-77")
                .email("test@test.com")
                .email2("test@test.com")
                .product("travel")
                .rprogramm("1")
                .fullAmount("4304")
                .agreement("1")
                .fioLat("KUANDYK ALIBI")
                .fioLat2("KUANDYK ALIBI")
                .build();

        ValidatableResponse response = apiClient.post(
                apiFfinUrl,
                "v1/products/freedom-travel/registration",
                request
        );

        String policyNumber = response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("policy_number")
                .toString().replace("OK:", "").trim();

        PoliciesRegistry.register(policyNumber);



    }
}
