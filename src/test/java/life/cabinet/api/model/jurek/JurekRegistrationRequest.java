package life.cabinet.api.model.jurek;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

// Все данные которые передаются в теле запроса
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record JurekRegistrationRequest(
        @JsonProperty("insurer") Insurer insurer,
        @JsonProperty("insured") Insured insured,
        @JsonProperty("beneficiary") Beneficiary beneficiary,
        @JsonProperty("notification_method") Integer notificationMethod,
        @JsonProperty("payment_periodicity") Integer paymentPeriodicity,
        @JsonProperty("guaranteed_period") String guaranteedPeriod,
        @JsonProperty("agent_full_name") String agentFullName,
        @JsonProperty("incoming_amount") Integer incomingAmount,
        @JsonProperty("premium_calculation") Boolean premiumCalculation,
        @JsonProperty("bank_id") String bankId,
        @JsonProperty("iban") String iban,
        @JsonProperty("usd") Boolean usd,
        @JsonProperty("period_annuity_payments_years") Integer periodAnnuityPaymentsYears
) {

    @Builder
    public record Insurer(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Insured(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Beneficiary(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Document(
            @JsonProperty("type") Integer type,
            @JsonProperty("number") String number,
            @JsonProperty("authority") String authority,
            @JsonProperty("date") String date
    ) {}
}