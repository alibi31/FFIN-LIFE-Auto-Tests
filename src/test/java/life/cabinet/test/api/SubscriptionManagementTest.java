package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.utils.TestDataStorage;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

@Tag("cabinet-api")
@Owner("QA")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Управление подписками - API тесты")
public class SubscriptionManagementTest {

    private final ApiClient apiClient = new ApiClient();
    private final String apiCabinetUrl = EnvConfig.cfg().apiCabinetUrl();

    private static Integer subscriptionId;

    @Test
    @Order(1)
    @DisplayName("GET /api/v2/products/subscriptions - Получить список всех подписок клиента")
    void shouldReturnSubscriptions() {
        apiClient.getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions")
                .statusCode(200)
                .body("data", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v2/products/subscriptions/{id} - Получить подписку по ID")
    void shouldReturnSubscriptionById() {
        // Получаем policy_number из TestDataStorage (после регистрации в UrbanRegistrationTest)
        String policyNumber = TestDataStorage.get("urban_policy_number");

        if (policyNumber == null) {
            System.out.println("⚠️ Policy number не найден в TestDataStorage, пропускаем тест");
            Assumptions.assumeTrue(false, "Требуется сначала зарегистрировать полис в UrbanRegistrationTest");
        }

        // Получаем список всех подписок и ищем нашу по policy_number
        ValidatableResponse allSubscriptionsResponse = apiClient
                .getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions")
                .statusCode(200)
                .body("status", is(true))
                .body("data", notNullValue());

        List<Integer> ids = allSubscriptionsResponse.extract().path("data.findAll { it.policy_number == '" + policyNumber + "' }.id");

        if (ids.isEmpty()) {
            System.out.println("⚠️ Подписка с policy_number " + policyNumber + " не найдена");
            Assumptions.assumeTrue(false, "Подписка не найдена в системе");
        }

        subscriptionId = ids.get(0);
        System.out.println("📌 Найдена подписка: ID = " + subscriptionId + ", Policy = " + policyNumber);

        // Проверяем получение конкретной подписки по ID
        ValidatableResponse response = apiClient.getWithAuth(
                apiCabinetUrl,
                "api/v2/products/subscriptions/" + subscriptionId
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .body("data", notNullValue())
                .body("data.id", equalTo(subscriptionId))
                .body("data.policy_number", equalTo(policyNumber))
                .body("data.product.name", equalTo("Freedom Urban"))
                .body("data.insurance_sum", notNullValue())
                .body("data.status", notNullValue())
                .body("data.start_date", notNullValue())
                .body("data.end_date", notNullValue());

        System.out.println("✅ Подписка успешно получена: ID = " + subscriptionId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v2/products/subscriptions/{id} - Несуществующий ID возвращает 404")
    void shouldReturn404ForNonExistentSubscription() {
        Integer fakeId = 999999;

        apiClient
                .getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions/" + fakeId)
                .statusCode(404);

        System.out.println("✅ Корректный 404 для несуществующего ID: " + fakeId);
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v2/products/subscriptions/{id}/cancel - Отменить активную подписку")
    void shouldCancelSubscription() {
        // Используем subscriptionId из первого теста
        if (subscriptionId == null) {
            String policyNumber = TestDataStorage.get("urban_policy_number");
            Assumptions.assumeTrue(policyNumber != null, "Требуется policy_number из TestDataStorage");

            List<Integer> ids = apiClient
                    .getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions")
                    .statusCode(200)
                    .extract()
                    .path("data.findAll { it.policy_number == '" + policyNumber + "' }.id");

            Assumptions.assumeTrue(!ids.isEmpty(), "Подписка не найдена");
            subscriptionId = ids.get(0);
        }

        System.out.println("🔄 Отменяем подписку: ID = " + subscriptionId);

        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/subscriptions/" + subscriptionId + "/cancel",
                "" // Пустое тело запроса
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        System.out.println("✅ Подписка успешно отменена: ID = " + subscriptionId);

        // Проверяем, что статус действительно изменился на "canceled"
        ValidatableResponse checkResponse = apiClient
                .getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions/" + subscriptionId)
                .statusCode(200)
                .body("data.status", equalTo("canceled"))
                .body("data.canceled_at", notNullValue());

        System.out.println("✅ Статус подписки изменён на 'canceled'");
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v2/products/subscriptions/{id}/cancel - Повторная отмена должна вернуть ошибку")
    void shouldNotCancelAlreadyCancelledSubscription() {
        if (subscriptionId == null) {
            String policyNumber = TestDataStorage.get("urban_policy_number");
            Assumptions.assumeTrue(policyNumber != null, "Требуется policy_number из TestDataStorage");

            List<Integer> ids = apiClient
                    .getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions")
                    .statusCode(200)
                    .extract()
                    .path("data.findAll { it.policy_number == '" + policyNumber + "' }.id");

            Assumptions.assumeTrue(!ids.isEmpty(), "Подписка не найдена");
            subscriptionId = ids.get(0);
        }

        System.out.println("🔄 Попытка повторной отмены подписки: ID = " + subscriptionId);

        // Пытаемся отменить уже отменённую подписку
        apiClient.postWithAuth(
                        apiCabinetUrl,
                        "api/v2/products/subscriptions/" + subscriptionId + "/cancel",
                        ""
                )
                .statusCode(anyOf(is(400), is(409), is(422))); // Bad Request, Conflict или Unprocessable Entity

        System.out.println("✅ Повторная отмена корректно заблокирована для ID = " + subscriptionId);
    }
}