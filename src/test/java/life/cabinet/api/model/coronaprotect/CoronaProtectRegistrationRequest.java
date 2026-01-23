package life.cabinet.api.model.coronaprotect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

// Все данные которые передаются в теле запроса
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record CoronaProtectRegistrationRequest(
        @JsonProperty("premium") Integer premium,
        @JsonProperty("notification_method") Integer notificationMethod,
        @JsonProperty("options") Options options,
        @JsonProperty("insurer") Insurer insurer,
        @JsonProperty("insured") Insured insured,
        @JsonProperty("recipient") Recipient recipient
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
            @JsonProperty("isNotKzResident") Boolean isNotKzResident,
            @JsonProperty("doc") Document doc
    ){}

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
    ){}

    @Builder
    public record Recipient(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uin") String uin,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("address") String address,
            @JsonProperty("email") String email,
            @JsonProperty("doc") Document doc
    ){}

    @Builder
    public record Document(
            @JsonProperty("number") String number,
            @JsonProperty("type") Integer type,
            @JsonProperty("authority") String authority,
            @JsonProperty("active") Boolean active,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate,
            @JsonProperty("isDisabled") Boolean isDisabled,
            @JsonProperty("date") String date
    ) {}

    @Builder
    public record Options(
            @JsonProperty("invalid") Boolean invalid,
            @JsonProperty("nervous") Boolean nervous,
            @JsonProperty("resperatory") Boolean resperatory,
            @JsonProperty("cardiovascular") Boolean cardiovascular
    ){}
}
