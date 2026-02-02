package life.cabinet.test.api;

import io.qameta.allure.Owner;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("cabinet-api")
@Owner("QA")
public class GetCardsTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("GET /api/v1/cards/get - 200")
    void shouldReturnDocuments() {
        apiClient.getWithAuth(apiCabinetUrl, "api/v1/cards/get")
                .statusCode(200);
    }
}