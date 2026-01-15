package life.superapp.api.service;

import javax.annotation.Nonnull;

public interface AuthService {
    @Nonnull
    String getOtt(String iin, String fullName) throws Exception;

    @Nonnull
    String getAccessToken(String oneTimeToken) throws Exception;
}
