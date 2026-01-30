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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("cabinet-api")
@Owner("QA")
public class ContractsFilterTest {
    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    @Test
    @DisplayName("GET /api/v2/contracts?status= - все договоры")
    void shouldReturnAllContracts() {
        apiClient.getWithAuth(apiCabinetUrl, "api/v2/contracts?status=")
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("GET /api/v2/contracts?status=planned - только плановые")
    void shouldFilterPlannedContracts() {
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/contracts?status=planned"
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        // Проверяем статусы
        List<String> plannedStatuses = Arrays.asList(
                "Подготовка",
                "Плановая",
                "Ожидание"
        );

        List<String> actualStatuses = response.extract().jsonPath().getList("data.status");
        for (String status : actualStatuses) {
            assertTrue(plannedStatuses.contains(status),
                    "Статус " + status + " не в списке плановых");
        }
    }

    @Test
    @DisplayName("GET /api/v2/contracts?status=active - только активные")
    void shouldFilterActiveContracts() {
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/contracts?status=active"
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

    @Test
    @DisplayName("GET /api/v2/contracts?status=terminated - только расторгнутые")
    void shouldFilterTerminatedContracts() {
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/contracts?status=terminated"
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        // Проверяем статусы
        List<String> terminatedStatuses = Arrays.asList(
                "Сторно",
                "Закрыт",
                "Сторно. Полис расторгнут",
                "Сторно. Полный возврат премии"
        );

        List<String> actualStatuses = response.extract().jsonPath().getList("data.status");
        for (String status : actualStatuses) {
            assertTrue(terminatedStatuses.contains(status),
                    "Статус " + status + " не в списке расторгнутых");
        }
    }

    @Test
    @DisplayName("GET /api/v2/contracts?status=finished - только завершенные")
    void shouldFilterFinishedContracts() {
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/contracts?status=finished"
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        // Проверяем статусы
        List<String> finishedStatuses = Arrays.asList(
                "Проведено",
                "Закончился срок действия",
                "Завершен",
                "Исполнено",
                "отработан"
        );

        List<String> actualStatuses = response.extract().jsonPath().getList("data.status");
        for (String status : actualStatuses) {
            assertTrue(finishedStatuses.contains(status),
                    "Статус " + status + " не в списке завершенных");
        }
    }
}