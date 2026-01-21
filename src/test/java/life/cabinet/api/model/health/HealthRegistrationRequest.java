package life.cabinet.api.model.health;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

// Все данные которые передаются в теле запроса
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record HealthRegistrationRequest(
        @JsonProperty("program") Integer program,
        @JsonProperty("period") Integer period,
        @JsonProperty("insurer") Insurer insurer,
        @JsonProperty("insured") Insured insured,
        @JsonProperty("agreement_1") Boolean agreement1,
        @JsonProperty("agreement_2") Boolean agreement2,
        @JsonProperty("agreement_3") Boolean agreement3,
        @JsonProperty("agent") String agent,
        @JsonProperty("nomad") String nomad,
        @JsonProperty("promo") String promo
) {

    @Builder
    public record Insurer(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uin") String uin,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("doc") Document doc
    ) {}

    @Builder
    public record Insured(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uin") String uin,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
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