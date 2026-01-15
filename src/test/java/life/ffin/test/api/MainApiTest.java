package life.ffin.test.api;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import life.core.http.ApiClient;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import static org.hamcrest.Matchers.*;

@Tag("ffin-api")
@Owner("QA")
public class MainApiTest {

    private final ApiClient apiClient = new ApiClient();
    private final String apiFfinUrl = EnvConfig.cfg().apiFfinUrl();

    @Test
    @DisplayName("GET /api/v1/main/our-team/employee")
    @Description("Получение всех членов команды")
    void getAllEmployeesTest() {
        apiClient.get(apiFfinUrl, "/v1/main/our-team/employee")
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/our-team/our-team.json"))
                .body("data.size()", greaterThan(0))
                .body("data.our_team", not(empty()));
    }

    @ParameterizedTest(name = "[{index}] employee id={0} -> 200 + схема")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("GET /api/v1/main/our-team/employee/{id}")
    @Description("Возвращает карточку сотрудника по валидному id")
    void getEmployeeByIdTest(int id) {
        apiClient.get(apiFfinUrl,"/v1/main/our-team/employee/" + id)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/our-team/employee.json"))
                .body("data", not(empty()));
    }

    @ParameterizedTest(name = "invalid numeric id={0} -> 422")
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("GET /api/v1/main/our-team/employee/{id}")
    void getEmployeeInvalidNumberTest(int id) {
        apiClient.get(apiFfinUrl,"/v1/main/our-team/employee/" + id)
                .statusCode(422)
                .body(matchesJsonSchemaInClasspath("schema/our-team/error.json"));
    }

    @ParameterizedTest(name = "non-numeric id={0} -> 422")
    @ValueSource(strings = {"abc", "1.5", "null", " ", "%20"})
    @DisplayName("GET /api/v1/main/our-team/employee/{id}")
    void getEmployeeNonNumericTest(String id) {
        apiClient.get(apiFfinUrl,"/v1/main/our-team/employee/" + id)
                .statusCode(422)
                .body(matchesJsonSchemaInClasspath("schema/our-team/error.json"));
    }

    @ParameterizedTest(name = "non-numeric id={0} -> 404")
    @ValueSource(ints = {1234568, 921489743})
    @DisplayName("GET /api/v1/main/our-team/employee/{id}")
    void getEmployeeNotFoundTest(int id) {
        Allure.parameter("employeeId", id);
        apiClient.get(apiFfinUrl,"/v1/main/our-team/employee/" + id)
                .statusCode(404)
                .body(matchesJsonSchemaInClasspath("schema/our-team/error.json"));
    }



}
