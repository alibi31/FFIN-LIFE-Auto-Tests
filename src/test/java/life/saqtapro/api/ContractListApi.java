package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.contract.ContractListRequest;
import life.saqtapro.api.model.contract.ContractListResponse;

import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/** Список и фильтрация договоров в SaqtaPro: GET /api/contracts (все фильтры — в query) */
public final class ContractListApi {

    private static final String CONTRACTS = "/api/contracts";

    private final Supplier<RequestSpecification> spec;

    private ContractListApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    public ContractListApi(String token) {
        this(() -> SaqtaRequestSpec.json(token));
    }

    /** Без токена — для проверки 401 */
    public static ContractListApi anonymous() {
        return new ContractListApi(SaqtaRequestSpec::anonymousJson);
    }

    @Step("Список договоров: {request}")
    public ValidatableResponse list(ContractListRequest request) {
        return given()
                .spec(spec.get())
                .queryParams(request.queryParams())
                .when()
                .get(CONTRACTS)
                .then();
    }

    /** Успешный запрос: HTTP 200 и status = true, ответ распарсен */
    public ContractListResponse.Page found(ContractListRequest request) {
        return list(request)
                .statusCode(200)
                .body("status", is(true))
                .extract()
                .as(ContractListResponse.class)
                .data();
    }
}
