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