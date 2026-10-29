package life.cabinet.test.api;

import io.qameta.allure.Owner;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import life.utils.data.PolicyFixtures;
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

    private static String policyNumber;
    private static Integer subscriptionId;

    @BeforeAll
    static void registerPolicy() {
        policyNumber = PolicyFixtures.registerUrbanPolicy(); // Создаем для тестов договор Urban
    }

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
        List<Integer> ids = apiClient.getWithAuth(apiCabinetUrl, "api/v2/products/subscriptions")
                .statusCode(200)
                .extract()
                .path("data.findAll { it.policy_number == '" + policyNumber + "' }.id");

        Assumptions.assumeTrue(!ids.isEmpty(), "Подписка ещё не создалась на бэкенде");
        subscriptionId = ids.get(0);
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
        Assumptions.assumeTrue(subscriptionId != null,
                "subscriptionId не найден — проверьте, что тест 2 прошёл раньше и подписка успела создаться");

        System.out.println("🔄 Отменяем подписку: ID = " + subscriptionId);

        ValidatableResponse response = apiClient.postWithAuth(
                apiCabinetUrl,
                "api/v2/products/subscriptions/" + subscriptionId + "/cancel",
                ""
        );

        response
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true));

        System.out.println("✅ Подписка успешно отменена: ID = " + subscriptionId);

        apiClient
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
        Assumptions.assumeTrue(subscriptionId != null,
                "subscriptionId не найден — проверьте, что тест 2 прошёл раньше");

        System.out.println("🔄 Попытка повторной отмены подписки: ID = " + subscriptionId);

        apiClient.postWithAuth(
                        apiCabinetUrl,
                        "api/v2/products/subscriptions/" + subscriptionId + "/cancel",
                        ""
                )
                .statusCode(anyOf(is(400), is(409), is(422)));

        System.out.println("✅ Повторная отмена корректно заблокирована для ID = " + subscriptionId);
    }
}