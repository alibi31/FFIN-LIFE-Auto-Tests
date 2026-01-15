package life.superapp.api.service;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface CancelPolicyService {

    void cancelPolicy(String policyNumber, String reason) throws IOException;
}
