package life.saqtapro.data;

import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest.Document;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest.Insured;
import life.saqtapro.config.Saqta;

/**
 * Данные для договора пенсионного аннуитета на dev-стенде.
 *
 * Застрахованный не генерируется случайно: договор оформляется поверх расчёта (calculation_id),
 * а расчёт сделан для конкретного ИИН. Поэтому здесь — тестовая персона стенда, для которой
 * сделан расчёт (та же, что в superapp PensionAnnuityTest), и справочные id SaqtaPro.
 */
public final class PensionAnnuityFixtures {

    private PensionAnnuityFixtures() {}

    /** Тестовая персона dev-стенда; должна совпадать с insured.uin в расчёте */
    public static final String INSURED_IIN = "700511402493";
    public static final String INSURED_FULL_NAME = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА";
    public static final String INSURED_IBAN = "KZ46551B829795347KZT";
    public static final String BANK_BIC = "KSNVKZKA";

    // Справочники SaqtaPro (значения с dev-стенда)
    public static final long JUREK_PROGRAM_ID = 4896707691L; // Freedom JUREK+: выплата дивидендов после поступления премии
    public static final int BANK_ID = 16;
    public static final int SIGNATORY_ID = 1;
    public static final int RESPONSIBLE_PERSON_ID = 1255;
    public static final int DOCUMENT_TYPE_ID_CARD = 2;

    /** Договор с электронным подписанием, без инвалидности, без ЕТН-счёта */
    public static PensionAnnuityContractRequest electronicContract() {
        return PensionAnnuityContractRequest.builder()
                .calculationId(Saqta.cfg().annuityCalculationId())
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
