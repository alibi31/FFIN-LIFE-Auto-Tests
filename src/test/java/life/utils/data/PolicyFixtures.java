package life.utils.data;

import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.ffin.api.model.urban.UrbanRegistrationRequest;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;

/** Регистрирует Urban-полис "на месте", без зависимости от порядка запуска других тестов. */
public final class PolicyFixtures {

    private PolicyFixtures() {}

    public static String registerUrbanPolicy() {
        ApiClient apiClient = new ApiClient();
        TestPerson p = TestPersonFactory.adult();

        UrbanRegistrationRequest request = UrbanRegistrationRequest.builder()
                .insuredType("self").insuranceSum(1000000).term(1).period(2)
                .insurancePremium(6000).confirmRepresentative(true)
                .insurerIin(p.iin()).insurerFullName(p.fullName())
                .insurerPhone(p.phone()).insurerAddress(p.address()).insurerEmail(p.email())
                .insurerDocType(String.valueOf(p.document().type()))
                .insurerDocNumber(p.document().number())
                .insurerDocIssueDate(p.document().issueDateStr())
                .insurerDocEndDate(p.birthDate().plusYears(90).toString())
                .insurerDocIssuer(p.document().authority())
                .notificationMethod(2).agreement(true)
                .insurerSex(p.genderCode()).insurerBirthDate(p.birthDateStr())
                .insuredIin(p.iin()).insuredFullName(p.fullName())
                .insuredSex(p.genderCode()).insuredBirthDate(p.birthDateStr())
                .insuredPhone(p.phone()).insuredAddress(p.address()).insuredEmail(p.email())
                .insuredDocType(String.valueOf(p.document().type()))
                .insuredDocNumber(p.document().number())
                .insuredDocIssueDate(p.document().issueDateStr())
                .insuredDocEndDate(p.birthDate().plusYears(90).toString())
                .insuredDocIssuer(p.document().authority())
                .policyType("URS").resource("test")
                .build();

        ValidatableResponse response = apiClient.post(
                EnvConfig.cfg().apiFfinUrl(), "v1/products/freedom-urban/registration", request);

        String policyNumber = response
                .statusCode(200).contentType(JSON).body("status", is(true))
                .extract().path("data.policy_number")
                .toString().replace("OK:", "").trim();

        PoliciesRegistry.register(policyNumber); // авто-cleanup через CancelPolicyExtension/Listener
        return policyNumber;
    }
}