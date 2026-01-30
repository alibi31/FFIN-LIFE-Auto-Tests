package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("cabinet-api")
@Owner("QA")
public class GetContractTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("GET /api/v2/contracts/{id} - 403 Несанкционированная попытка доступа")
    void shouldReturn403WhenUserHasNoAccess() {
        apiClient.getWithAuth(apiCabinetUrl, "api/v2/contracts/4840963119")
                .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/v2/contracts?status=active&without_ic=1 - действующие договора при отправке заявке о страховом случае")
    void shouldGetActiveContracts() {
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/contracts?status=active&without_ic=1"
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        // Проверяем статусы
        List<String> activeStatuses = Arrays.asList(
                "Принят",
                "Действует",
                "Рабочий",
                "Принято - полис создан",
                "Действует(Изменен)",
                "Действует(Ожидает перевода в оплаченный)",
                "Действует(Оплачен)",
                "Выполняется...",
                "Урегулирование",
                "Действует(ожидает перевода в прекращенный)",
                "Действует(освобожден от взноса)",
                "Действует(Выплата приостановлена)",
                "Действует (Гарант)",
                "договор заключен"
        );

        List<String> actualStatuses = response.extract().jsonPath().getList("data.status");
        for (String status : actualStatuses) {
            assertTrue(activeStatuses.contains(status),
                    "Статус " + status + " не в списке активных");
        }
    }
}