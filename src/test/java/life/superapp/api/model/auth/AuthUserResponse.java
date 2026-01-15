package life.superapp.api.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthUserResponse(
        @JsonProperty("status") Boolean status,
        @JsonProperty("data") Data data
)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            @JsonProperty("access_token") String accessToken
    ) {}
}
