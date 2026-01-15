package life.superapp.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthUserRequest(
        @JsonProperty("one_time_token") String oneTimeToken
)
{ }
