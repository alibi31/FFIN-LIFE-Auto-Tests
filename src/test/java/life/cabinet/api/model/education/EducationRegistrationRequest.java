package life.cabinet.api.model.education;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder

public record EducationRegistrationRequest(
        @JsonProperty("period") Integer period,
        @JsonProperty("term") Integer term,
        @JsonProperty("premium") Integer premium,
        @JsonProperty("notification_method") Integer notification_method,
        @JsonProperty("parent") Parent parent,
        @JsonProperty("child") Child child,
        @JsonProperty("options") Options options
) {
    @Builder
    public record Parent(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("doc") Document doc
    ){}

    @Builder
    public record Child(
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("gender") Integer gender,
            @JsonProperty("phone") String phone,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("doc") Document doc
    ){}

    @Builder
    public record Document(
            @JsonProperty("number") String number,
            @JsonProperty("type") Integer type,
            @JsonProperty("authority") String authority,
            @JsonProperty("date") String date
    ) {}

    @Builder
    public record Options(
            @JsonProperty("deathDueToAccident") Boolean deathDueToAccident,
            @JsonProperty("disabilityDueToAccident") Boolean disabilityDueToAccident,
            @JsonProperty("bodilyInjury") Boolean bodilyInjury,
            @JsonProperty("criticalIllness") Boolean criticalIllness,
            @JsonProperty("temporaryIncapacityForWork") Boolean temporaryIncapacityForWork,
            @JsonProperty("hospitalizationAsResultOfAccident") Boolean hospitalizationAsResultOfAccident,
            @JsonProperty("childBodilyInjury") Boolean childBodilyInjury,
            @JsonProperty("childCriticalIllness") Boolean childCriticalIllness
    ){}
}
