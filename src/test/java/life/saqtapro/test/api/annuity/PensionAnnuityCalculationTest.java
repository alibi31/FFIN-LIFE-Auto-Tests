package life.saqtapro.test.api.annuity;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import life.saqtapro.actuarial.PensionAnnuityCalculator;
import life.saqtapro.actuarial.PensionAnnuityCalculator.Payment;
import life.saqtapro.actuarial.PensionAnnuityCalculator.Quote;
import life.saqtapro.api.PensionAnnuityApi;
import life.saqtapro.api.model.annuity.PensionAnnuityCalculationRequest;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.data.PensionAnnuityFixtures;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SaqtaApiTest
@Tag("pension-annuity")
@Feature("Пенсионный аннуитет")
@Story("Калькуляция")
@Owner("QA")
class PensionAnnuityCalculationTest {

    private static final String SCHEMA = "schema/saqtapro/pension-annuity-calculation.json";
    private static final String CALC = "data.calculation.data.";

    /** Лимит выплаты дивидендов — из dividend_limit_message на dev; в калькуляторе актуариев его нет */
    private static final long DIVIDEND_LIMIT = 900_000;

    private final PensionAnnuityApi api = new PensionAnnuityApi(SaqtaAuth.tokenOf(TestUser.QA_EMPLOYEE));

    @Test
    @DisplayName("POST /api/v1/contracts/pension-annuity/calculations — успешный расчёт совпадает с калькулятором актуариев")
    @Description("ОПВ + ДПВ + ОППВ (60 мес.) + собственные средства + средства из дивидендов + выкупная из другой КСЖ, "
            + "без инвалидности, ГП 10 лет. Премия, ежемесячная выплата, минимальная премия и весь график выплат "
            + "сверяются с эталонным расчётом по формулам xlsx (PensionAnnuityCalculator)")
    void successfulCalculationMatchesActuarialModel() {
        PensionAnnuityCalculationRequest request = PensionAnnuityFixtures.successfulCalculation();
        LocalDate startDate = LocalDate.parse(request.contractStartDate());

        Quote expected = Allure.step("Эталонный расчёт по формулам калькулятора актуариев", () -> {
            Quote quote = PensionAnnuityCalculator.calculate(request);
            Allure.addAttachment("Эталонный расчёт", "text/plain", quote.describe(), ".txt");
            return quote;
        });
        assertTrue(expected.immediate(startDate), "Фикстура рассчитана на немедленный аннуитет");

        JsonPath response = api.calculate(request)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body(matchesJsonSchemaInClasspath(SCHEMA))
                .body("data.calculation.status", is(true))
                .body("data.payment_schedule_pdf_url", startsWith("https://"))
                .body("data.payment_schedule_pdf_url_kz", startsWith("https://"))
                .extract().jsonPath();

        Allure.step("Входные данные вернулись без изменений", () -> assertAll(
                () -> assertEquals(PensionAnnuityFixtures.INSURED_IIN, response.getString("data.insured.uin"), "insured.uin"),
                () -> assertEquals(PensionAnnuityFixtures.INSURED_GENDER, response.getString("data.insured.gender"), "insured.gender"),
                () -> assertEquals(PensionAnnuityFixtures.INSURED_BIRTH_DATE, response.getString("data.insured.birth_date"), "insured.birth_date"),
                () -> assertEquals(request.contractStartDate(), response.getString("data.contract_start_date"), "contract_start_date"),
                () -> assertEquals(PensionAnnuityFixtures.today().toString(), response.getString("data.calculation_date"), "calculation_date"),
                () -> assertEquals(request.mandatoryPensionContributions(), response.getLong("data.mandatory_pension_contributions"), "ОПВ"),
                () -> assertEquals(request.voluntaryPensionContributions(), response.getLong("data.voluntary_pension_contributions"), "ДПВ"),
                () -> assertEquals(request.mandatoryProfessionalPensionContributions().amount(),
                        response.getLong("data.mandatory_professional_pension_contributions.amount"), "ОППВ"),
                () -> assertEquals(request.mandatoryProfessionalPensionContributions().months(),
                        response.getInt("data.mandatory_professional_pension_contributions.months"), "ОППВ, месяцев"),
                () -> assertEquals(request.ownExpenses(), response.getLong("data.own_expenses"), "собственные средства"),
                () -> assertEquals(request.ownFundsFromDividends(), response.getLong("data.own_funds_from_dividends"), "средства из дивидендов"),
                () -> assertEquals(request.otherLifeInsuranceCompanyRedemption(),
                        response.getLong("data.other_life_insurance_company_redemption"), "выкупная из другой КСЖ"),
                () -> assertEquals(request.guaranteedPeriodYears(), response.getInt("data.guaranteed_period_years"), "ГП"),
                () -> assertEquals(request.disability(), response.getString("data.disability"), "disability")));

        Allure.step("Суммы совпадают с эталоном", () -> assertAll(
                () -> assertEquals(expected.premium(), response.getLong(CALC + "insurance_premium"),
                        "insurance_premium = сумма всех источников средств"),
                () -> assertEquals(expected.monthlyPayment(), response.getLong(CALC + "annuity_payment"),
                        "annuity_payment = премия / аннуитетный фактор брутто / 12 (AG22)"),
                () -> assertEquals(expected.minimumPremium(), response.getLong(CALC + "minimum_premium_sum"),
                        "minimum_premium_sum = фактор × 70% ПМ × 12 (AG21)"),
                () -> assertTrue(response.getBoolean(CALC + "premium_is_enough"), "premium_is_enough"),
                () -> assertEquals(0L, response.getLong(CALC + "missing_premium_sum"), "missing_premium_sum"),
                () -> assertTrue(response.getBoolean(CALC + "is_immediate_annuity"), "is_immediate_annuity")));

        Allure.step("Дивиденды: в пределах лимита, оба поля согласованы", () -> {
            long dividends = response.getLong(CALC + "insurance_dividends");
            assertAll(
                    () -> assertEquals(dividends, response.getLong(CALC + "insurance_dividends_NEW.kzt"), "insurance_dividends_NEW.kzt"),
                    () -> assertEquals(dividends, response.getLong(CALC + "payments_schedule.dividends.kzt"), "payments_schedule.dividends.kzt"),
                    () -> assertTrue(dividends > 0 && dividends <= DIVIDEND_LIMIT,
                            "дивиденды должны быть в (0; " + DIVIDEND_LIMIT + "], а пришло " + dividends));
        });

        Allure.step("График выплат совпадает с эталоном", () -> {
            assertAll(
                    () -> assertEquals(expected.paymentStartDate().toString(), response.getString(CALC + "payments_schedule.start_date"), "start_date"),
                    () -> assertEquals(expected.ageAtPaymentStart(), response.getInt(CALC + "payments_schedule.start_age"), "start_age"),
                    () -> assertTrue(response.getBoolean(CALC + "payments_schedule.is_immediate_annuity"), "payments_schedule.is_immediate_annuity"));
            assertScheduleEquals(expected.schedule(), actualSchedule(response));
        });
    }

    @Test
    @DisplayName("POST /api/v1/contracts/pension-annuity/calculations — каждый расчёт получает новый calculation_id")
    @Description("calculation_id одноразовый: договор оформляется по нему один раз. Повторный расчёт с теми же "
            + "данными должен выдать новый id, иначе второй договор по этим параметрам не создать")
    void eachCalculationGetsNewId() {
        PensionAnnuityCalculationRequest request = PensionAnnuityFixtures.successfulCalculation();

        long first = api.newCalculationId(request);
        long second = api.newCalculationId(request);

        assertNotEquals(first, second, "Два расчёта подряд вернули один и тот же calculation_id = " + first);
    }

    /** schedule[год].months[] → плоский список в порядке ответа */
    @SuppressWarnings("unchecked")
    private static List<Payment> actualSchedule(JsonPath response) {
        List<Map<String, Object>> years = response.getList(CALC + "payments_schedule.schedule");
        List<Payment> payments = new ArrayList<>();
        for (Map<String, Object> year : years) {
            for (Map<String, Object> month : (List<Map<String, Object>>) year.get("months")) {
                payments.add(new Payment(
                        LocalDate.parse((String) month.get("date")),
                        ((Number) month.get("amount")).longValue()));
            }
        }
        return payments;
    }

    /** В графике ~640 строк: вместо простыни из assertEquals показываем первое расхождение */
    private static void assertScheduleEquals(List<Payment> expected, List<Payment> actual) {
        int common = Math.min(expected.size(), actual.size());
        for (int k = 0; k < common; k++) {
            if (!expected.get(k).equals(actual.get(k))) {
                fail("График расходится с эталоном с выплаты №" + k
                        + ": ожидали " + expected.get(k) + ", пришло " + actual.get(k));
            }
        }
        assertEquals(expected.size(), actual.size(), "Количество выплат в графике (до 110 лет). Последняя ожидаемая: "
                + expected.get(expected.size() - 1) + ", последняя в ответе: "
                + (actual.isEmpty() ? "—" : actual.get(actual.size() - 1)));
    }
}
