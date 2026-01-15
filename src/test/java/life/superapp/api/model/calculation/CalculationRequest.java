package life.superapp.api.model.calculation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalculationRequest(
        @JsonProperty("mandatory_pension_contributions") String mandatoryPensionContributions,
        @JsonProperty("voluntary_pension_contributions") String voluntaryPensionContributions,
        @JsonProperty("own_expenses") String ownExpenses,
        @JsonProperty("guaranteed_period_years") String guaranteedPeriodYears,
        @JsonProperty("mandatory_professional_pension_contributions") MandatoryProfessionalPensionContributions oppv,
        @JsonProperty("disability") String disability,
        @JsonProperty("other_life_insurance_company_redemption") String otherInsuranceRedemption
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MandatoryProfessionalPensionContributions (
            @JsonProperty("amount") String amount,
            @JsonProperty("months") String months
    ) {}
}
