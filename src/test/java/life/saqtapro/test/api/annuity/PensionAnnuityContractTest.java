package life.saqtapro.test.api.annuity;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import life.saqtapro.api.PensionAnnuityApi;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.data.PensionAnnuityFixtures;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static life.saqtapro.data.PensionAnnuityFixtures.BANK_BIC;
import static life.saqtapro.data.PensionAnnuityFixtures.INSURED_FULL_NAME;
import static life.saqtapro.data.PensionAnnuityFixtures.INSURED_IBAN;
import static life.saqtapro.data.PensionAnnuityFixtures.INSURED_IIN;
import static life.saqtapro.data.PensionAnnuityFixtures.JUREK_PROGRAM_ID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SaqtaApiTest
@Tag("pension-annuity")
@Feature("Пенсионный аннуитет")
@Owner("QA")
class PensionAnnuityContractTest {

    private static final String SCHEMA = "schema/saqtapro/pension-annuity-contract.json";

    /** Подписанные ссылки MinIO живут 10 минут и пересоздаются на каждый запрос — их не сравниваем */
    private static final String[] VOLATILE_CALCULATION_FIELDS = {"payment_schedule_pdf_url", "payment_schedule_pdf_url_kz"};

    private final PensionAnnuityApi api = new PensionAnnuityApi(SaqtaAuth.tokenOf(TestUser.QA_EMPLOYEE));

    @Test
    @DisplayName("POST /api/v2/contracts/pension-annuity/contract — регистрация договора с электронным подписанием")
    @Description("Договор создаётся по готовому расчёту, ответ соответствует JSON-схеме, данные застрахованного, "
            + "расчёта и реквизиты попадают в договор; GET по id возвращает тот же договор")
    void registerContract() {
        PensionAnnuityContractRequest request = PensionAnnuityFixtures.electronicContract();
        int calculationId = request.calculationId().intValue();
        // Дата заключения считается по времени Алматы: прогон около полуночи UTC не должен падать
        String today = LocalDate.now(ZoneId.of("Asia/Almaty")).toString();

        JsonPath created = api.createContract(request)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body(matchesJsonSchemaInClasspath(SCHEMA))
                // договор
                .body("data.id", notNullValue())
                .body("data.contract_number", matchesPattern("^SQ\\d{8}[A-Z]{2}$"))
                .body("data.product_name", equalTo("Пенсионный аннуитет"))
                .body("data.sales_channel", equalTo("saqta"))
                .body("data.conclusion_date", equalTo(today))
                .body("data.signing_type.value", equalTo("electronic"))
                .body("data.jurek_program_id", equalTo(JUREK_PROGRAM_ID))
                // страхователь = застрахованный, выгодоприобретателей нет
                .body("data.insured.uin", equalTo(INSURED_IIN))
                .body("data.insured.full_name", equalTo(INSURED_FULL_NAME))
                .body("data.policyholder.bin", equalTo(INSURED_IIN))
                .body("data.beneficiaries", empty())
                // договор привязан к нашему расчёту, и расчёт успешный
                .body("data.calculation.calculation_id", equalTo(calculationId))
                .body("data.calculation.insured.uin", equalTo(INSURED_IIN))
                .body("data.calculation.calculation.status", is(true))
                .body("data.calculation.calculation.data.premium_is_enough", is(true))
                // что ушло в учётную систему
                .body("data.registration_payload.calculation_id", equalTo(calculationId))
                .body("data.registration_payload.insured_uin", equalTo(INSURED_IIN))
                .body("data.registration_payload.insured_iban", equalTo(INSURED_IBAN))
                .body("data.registration_payload.bank_bic", equalTo(BANK_BIC))
                .body("data.registration_payload.insured_document_number",
                        equalTo(request.insured().document().number()))
                .body("data.registration_payload.signing_type", equalTo("electronic"))
                .body("data.registration_payload.login_reg", equalTo("saqtapro"))
                .extract().jsonPath();

        long contractId = created.getLong("data.id");

        JsonPath fetched = api.getContract(contractId)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body(matchesJsonSchemaInClasspath(SCHEMA))
                .body("data.id", equalTo((int) contractId))
                .body("data.contract_number", equalTo(created.getString("data.contract_number")))
                .extract().jsonPath();

        assertEquals(stable(created), stable(fetched),
                "GET по id должен вернуть тот же договор, что и ответ на создание");
    }

    /** data без подписанных ссылок на PDF — остальное между POST и GET должно совпасть полностью */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> stable(JsonPath response) {
        Map<String, Object> data = response.getMap("data");
        Map<String, Object> calculation = (Map<String, Object>) data.get("calculation");
        if (calculation != null) {
            for (String field : VOLATILE_CALCULATION_FIELDS) {
                calculation.remove(field);
            }
        }
        return data;
    }
}
