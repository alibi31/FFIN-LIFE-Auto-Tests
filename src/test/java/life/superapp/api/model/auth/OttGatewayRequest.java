package life.superapp.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OttGatewayRequest(
        @JsonProperty("fullName") String fullName,
        @JsonProperty("iin") String iin,
        @JsonProperty("phone") String phone,
        @JsonProperty("birth_date") String birthDate,   // "YYYY-MM-DD"
        @JsonProperty("gender") String gender,          // "M"/"F"
        @JsonProperty("email") String email,
        @JsonProperty("documents") List<Document> documents
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Document(
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("document_type") String documentType,
            @JsonProperty("document_issue_authority") String documentIssueAuthority,
            @JsonProperty("document_issue_date") String documentIssueDate // "YYYY-MM-DD"
    ) {}
}