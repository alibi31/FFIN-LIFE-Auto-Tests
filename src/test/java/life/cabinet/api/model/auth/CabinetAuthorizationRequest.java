package life.cabinet.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CabinetAuthorizationRequest {

    @JsonProperty("uin")
    private String uin;

    @JsonProperty("password")
    private String password;
}