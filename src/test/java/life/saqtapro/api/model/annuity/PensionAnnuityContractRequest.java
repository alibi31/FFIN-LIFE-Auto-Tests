package life.saqtapro.api.model.annuity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Тело поля "data" в multipart-запросе POST /api/v2/contracts/pension-annuity/contract.
 * Структура снята с реального запроса SaqtaPro (dev-saqta).
 */
@Builder(toBuilder = true)
public record PensionAnnuityContractRequest(
        @JsonProperty("calculation_id") Long calculationId,
        @JsonProperty("insured") Insured insured,
        @JsonProperty("signing_type") String signingType,
        @JsonProperty("jurek_program_id") Long jurekProgramId,
        @JsonProperty("signatory_id") Integer signatoryId,
        @JsonProperty("responsible_person_id") Integer responsiblePersonId,
        @JsonProperty("region_signatory_id") Integer regionSignatoryId
) {

    @Builder(toBuilder = true)
    public record Insured(
            @JsonProperty("etn_iban") String etnIban,
            @JsonProperty("iban") String iban,
            @JsonProperty("bank_id") Integer bankId,
            @JsonProperty("address") String address,
            @JsonProperty("bank_bic") String bankBic,
            @JsonProperty("uin") String uin,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("disability") String disability,
            @JsonProperty("email") String email,
            @JsonProperty("document") Document document,
            @JsonProperty("is_usa_tr") boolean usaTaxResident,
            @JsonProperty("is_fpo") boolean foreignPublicOfficial,
            @JsonProperty("is_relative_fpo") boolean relativeOfForeignPublicOfficial
    ) {}

    @Builder(toBuilder = true)
    public record Document(
            @JsonProperty("number") String number,
            @JsonProperty("authority") String authority,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("expired_date") String expiredDate,
            @JsonProperty("type") Integer type
    ) {}
}
