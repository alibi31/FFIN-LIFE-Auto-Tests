package life.superapp.api.model.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegistrationRequest(
        @JsonProperty("mandatory_pension_contributions") String mandatoryPensionContributions,
        @JsonProperty("voluntary_pension_contributions") String voluntaryPensionContributions,
        @JsonProperty("own_expenses") String ownExpenses,
        @JsonProperty("guaranteed_period_years") String guaranteedPeriodYears,
        @JsonProperty("mandatory_professional_pension_contributions") MandatoryProfessionalPensionContributions oppv,
        @JsonProperty("iban") String iban,
        @JsonProperty("email") String email,
        @JsonProperty("disability") Disability disability,
        @JsonProperty("beneficiary") Beneficiary beneficiary,
        @JsonProperty("previous_life_insurance_contract") PreviousContract previousContract,
        @JsonProperty("doc") Doc doc
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MandatoryProfessionalPensionContributions (
            @JsonProperty("amount") int amount,
            @JsonProperty("months") int months
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Disability(
            @JsonProperty("group") String group,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Beneficiary(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("gender") String gender,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("doc") Doc doc
    ) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Doc(
            @JsonProperty("number") String number,
            @JsonProperty("type") String type,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("issue") String issue
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PreviousContract(
            @JsonProperty("policy_number") String policyNumber,
            @JsonProperty("company") String company,
            @JsonProperty("redemption") String redemption,
            @JsonProperty("start_date") String startDate
    ) {}
}
