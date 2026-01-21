package life.cabinet.api.model.first;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record FirstRegistrationRequest(
        @JsonProperty("insurance_premium") Integer insurancePremium,
        @JsonProperty("insurance_premium_kzt") Integer insurancePremiumKzt,
        @JsonProperty("notification_method") Integer notificationMethod,
        @JsonProperty("ic_sum") Integer icSum,
        @JsonProperty("term") Integer term,
        @JsonProperty("agent_full_name") String agentFullName,
        @JsonProperty("insured") Insured insured,
        @JsonProperty("beneficiary") Beneficiary beneficiary
) {

    @Builder
    public record Insured(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uin") String uin,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("isNotKzResident") Boolean isNotKzResident,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Beneficiary(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uin") String uin,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("isNotKzResident") Boolean isNotKzResident,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Document(
            @JsonProperty("number") String number,
            @JsonProperty("type") Integer type,
            @JsonProperty("authority") String authority,
            @JsonProperty("active") Boolean active,
            @JsonProperty("isDisabled") Boolean isDisabled,
            @JsonProperty("date") String date
    ) {}
}