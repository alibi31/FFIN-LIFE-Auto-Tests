//package life.superapp.api.model.cancel;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public record CancelPolicyResponse(
//        @JsonProperty("\uD83E\uDDF5") String
//) {
//}

package life.superapp.api.model.cancel;

public class CancelPolicyResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CancelPolicyResponse() {
        this.success = false;
        this.message = "";
    }

    public CancelPolicyResponse(boolean success) {
        this.success = success;
    }

    public CancelPolicyResponse(String message) {
        this.message = message;
    }
}