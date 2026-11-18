package life.saqtapro.api.model.annuity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Тело POST /api/v1/contracts/pension-annuity/calculations.
 * Структура снята с реального запроса SaqtaPro (dev-saqta). Все суммы — в тенге, целые.
 */
@Builder(toBuilder = true)
public record PensionAnnuityCalculationRequest(
        @JsonProperty("insured") Insured insured,
        /** ОПВ */
        @JsonProperty("mandatory_pension_contributions") long mandatoryPensionContributions,
        /** ДПВ */
        @JsonProperty("voluntary_pension_contributions") long voluntaryPensionContributions,
        /** Собственные средства */
        @JsonProperty("own_expenses") long ownExpenses,
        /** Собственные средства из дивидендов */
        @JsonProperty("own_funds_from_dividends") long ownFundsFromDividends,
        /** yyyy-MM-dd */
        @JsonProperty("contract_start_date") String contractStartDate,
        /** Гарантированный срок выплат, лет (0..30) */
        @JsonProperty("guaranteed_period_years") int guaranteedPeriodYears,
        /** ОППВ: сумма и количество месяцев уплаты */
        @JsonProperty("mandatory_professional_pension_contributions") ProfessionalContributions mandatoryProfessionalPensionContributions,
        /** NONE — без инвалидности */
        @JsonProperty("disability") String disability,
        /** Выкупная сумма из другой КСЖ */
        @JsonProperty("other_life_insurance_company_redemption") long otherLifeInsuranceCompanyRedemption,
        // "monthes" — так в API, не опечатка в тесте
        @JsonProperty("other_life_insurance_company_mppc_monthes") int otherLifeInsuranceCompanyMppcMonths,
        /** Выкупная из другой КСЖ пришла как ОППВ */
        @JsonProperty("has_other_life_insurance_company_mppc") boolean hasOtherLifeInsuranceCompanyMppc,
        @JsonProperty("is_immediate_annuity_superapp") boolean immediateAnnuitySuperapp,
        /** Процент дивидендов, целое число: 7 = 7% */
        @JsonProperty("dividend_percent") int dividendPercent
) {

    @Builder(toBuilder = true)
    public record Insured(
            @JsonProperty("uin") String uin,
            /** Фронтенд SaqtaPro отправляет пустую строку — ФИО backend берёт сам */
            @JsonProperty("full_name") String fullName,
            /** MALE / FEMALE */
            @JsonProperty("gender") String gender,
            /** yyyy-MM-dd */
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("phone_number") String phoneNumber
    ) {}

    @Builder(toBuilder = true)
    public record ProfessionalContributions(
            @JsonProperty("amount") long amount,
            @JsonProperty("months") int months
    ) {}

    /** Совокупная страховая премия — все источники средств вместе */
    public long totalPremium() {
        long oppv = mandatoryProfessionalPensionContributions == null ? 0 : mandatoryProfessionalPensionContributions.amount();
        return mandatoryPensionContributions + voluntaryPensionContributions + oppv
                + ownExpenses + ownFundsFromDividends + otherLifeInsuranceCompanyRedemption;
    }
}
