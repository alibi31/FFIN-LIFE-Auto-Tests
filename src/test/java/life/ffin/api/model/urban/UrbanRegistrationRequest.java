package life.ffin.api.model.urban;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record UrbanRegistrationRequest(
        @JsonProperty("insuredType") String insuredType,
        @JsonProperty("insuranceSum") Integer insuranceSum,
        @JsonProperty("term") Integer term,
        @JsonProperty("period") Integer period,
        @JsonProperty("insurancePremium") Integer insurancePremium,
        @JsonProperty("confirmRepresentative") Boolean confirmRepresentative,
        @JsonProperty("insurerIin") String insurerIin,
        @JsonProperty("insurerFullName") String insurerFullName,
        @JsonProperty("insurerPhone") String insurerPhone,
        @JsonProperty("insurerAddress") String insurerAddress,
        @JsonProperty("insurerEmail") String insurerEmail,
        @JsonProperty("insurerDocType") String insurerDocType,
        @JsonProperty("insurerDocNumber") String insurerDocNumber,
        @JsonProperty("insurerDocIssueDate") String insurerDocIssueDate,
        @JsonProperty("insurerDocEndDate") String insurerDocEndDate,
        @JsonProperty("insurerDocIssuer") String insurerDocIssuer,
        @JsonProperty("notificationMethod") Integer notificationMethod,
        @JsonProperty("agreement") Boolean agreement,
        @JsonProperty("insurerSex") Integer insurerSex,
        @JsonProperty("insurerBirthDate") String insurerBirthDate,
        @JsonProperty("insuredIin") String insuredIin,
        @JsonProperty("insuredFullName") String insuredFullName,
        @JsonProperty("insuredSex") Integer insuredSex,
        @JsonProperty("insuredBirthDate") String insuredBirthDate,
        @JsonProperty("insuredPhone") String insuredPhone,
        @JsonProperty("insuredAddress") String insuredAddress,
        @JsonProperty("insuredEmail") String insuredEmail,
        @JsonProperty("insuredDocType") String insuredDocType,
        @JsonProperty("insuredDocNumber") String insuredDocNumber,
        @JsonProperty("insuredDocIssueDate") String insuredDocIssueDate,
        @JsonProperty("insuredDocEndDate") String insuredDocEndDate,
        @JsonProperty("insuredDocIssuer") String insuredDocIssuer,
        @JsonProperty("policyType") String policyType,
        @JsonProperty("resource") String resource
) {
}
