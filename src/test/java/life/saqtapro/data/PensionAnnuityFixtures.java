package life.saqtapro.data;

import life.saqtapro.api.model.annuity.PensionAnnuityCalculationRequest;
import life.saqtapro.api.model.annuity.PensionAnnuityCalculationRequest.ProfessionalContributions;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest.Document;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest.Insured;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Данные для расчёта и договора пенсионного аннуитета на dev-стенде.
 *
 * Застрахованный не генерируется случайно: расчёт и договор делаются для тестовой персоны стенда
 * (та же, что в superapp PensionAnnuityTest), а в договоре insured.uin должен совпадать с расчётом.
 */
public final class PensionAnnuityFixtures {

    private PensionAnnuityFixtures() {}

    /** Тестовая персона dev-стенда; один и тот же ИИН в расчёте и в договоре */
    public static final String INSURED_IIN = "700511402493";
    public static final String INSURED_FULL_NAME = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА";
    public static final String INSURED_BIRTH_DATE = "1970-05-11";
    public static final String INSURED_GENDER = "FEMALE";
    public static final String INSURED_PHONE = "+77014514050";
    public static final String INSURED_IBAN = "KZ46551B829795347KZT";
    public static final String BANK_BIC = "KSNVKZKA";

    // Справочники SaqtaPro (значения с dev-стенда)
    public static final long JUREK_PROGRAM_ID = 4896707691L; // Freedom JUREK+: выплата дивидендов после поступления премии
    public static final int BANK_ID = 16;
    public static final int SIGNATORY_ID = 1;
    public static final int RESPONSIBLE_PERSON_ID = 1255;
    public static final int DOCUMENT_TYPE_ID_CARD = 2;

    /** Дата начала договора — сегодня по Алматы: прогон около полуночи UTC не должен давать «вчера» */
    public static LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Almaty"));
    }

    /**
     * Успешный расчёт: все источники средств, ОППВ 60 месяцев (немедленный аннуитет с 50 лет),
     * без инвалидности, гарантированный срок 10 лет, дивиденды 7%.
     * Совокупная премия 145 100 000 ₸ — с запасом выше минимальной (~9,8 млн ₸).
     */
    public static PensionAnnuityCalculationRequest successfulCalculation() {
        return PensionAnnuityCalculationRequest.builder()
                .insured(PensionAnnuityCalculationRequest.Insured.builder()
                        .uin(INSURED_IIN)
                        .fullName("")
                        .gender(INSURED_GENDER)
                        .birthDate(INSURED_BIRTH_DATE)
                        .phoneNumber(INSURED_PHONE)
                        .build())
                .contractStartDate(today().toString())
                .mandatoryPensionContributions(50_000_000)
                .voluntaryPensionContributions(40_000_000)
                .mandatoryProfessionalPensionContributions(ProfessionalContributions.builder()
                        .amount(30_000_000)
                        .months(60)
                        .build())
                .ownExpenses(5_000_000)
                .ownFundsFromDividends(100_000)
                .otherLifeInsuranceCompanyRedemption(20_000_000)
                .otherLifeInsuranceCompanyMppcMonths(0)
                .hasOtherLifeInsuranceCompanyMppc(false)
                .guaranteedPeriodYears(10)
                .disability("NONE")
                .dividendPercent(7)
                .immediateAnnuitySuperapp(false)
                .build();
    }

    /** Договор с электронным подписанием, без инвалидности, без ЕТН-счёта — по свежему расчёту */
    public static PensionAnnuityContractRequest electronicContract(long calculationId) {
        return PensionAnnuityContractRequest.builder()
                .calculationId(calculationId)
                .signingType("electronic")
                .jurekProgramId(JUREK_PROGRAM_ID)
                .signatoryId(SIGNATORY_ID)
                .responsiblePersonId(RESPONSIBLE_PERSON_ID)
                .regionSignatoryId(null)
                .insured(Insured.builder()
                        .uin(INSURED_IIN)
                        .fullName(INSURED_FULL_NAME)
                        .address("АЛМАТЫ, АУЭЗОВСКИЙ, УЛИЦА Жандосова, д. 182, кв. 14")
                        .email("test@test.com")
                        .iban(INSURED_IBAN)
                        .etnIban("")
                        .bankId(BANK_ID)
                        .bankBic(BANK_BIC)
                        .disability("NONE")
                        .usaTaxResident(false)
                        .foreignPublicOfficial(false)
                        .relativeOfForeignPublicOfficial(false)
                        .document(Document.builder()
                                .number("045833075")
                                .authority("МИНИСТЕРСТВО ВНУТРЕННИХ ДЕЛ РК")
                                .startDate("2020-02-13")
                                .expiredDate("2030-02-12")
                                .type(DOCUMENT_TYPE_ID_CARD)
                                .build())
                        .build())
                .build();
    }
}
