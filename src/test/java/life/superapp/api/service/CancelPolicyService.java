package life.superapp.api.service;

import life.superapp.api.model.cancel.CancelPolicyResponse;
import javax.annotation.Nonnull;
import java.io.IOException;

public interface CancelPolicyService {
    CancelPolicyResponse cancelPolicy(@Nonnull String policyNumber,
                                      @Nonnull String reason) throws IOException;

//    void cancelPolicy(String policyNumber, String reason) throws IOException;
}
