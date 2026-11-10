package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.client.ClientSearchRequest;
import life.saqtapro.api.model.client.ClientSearchResponse;

import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/** Поиск и фильтрация клиентов в SaqtaPro: POST /api/search/client */
public final class ClientSearchApi {

    private static final String SEARCH_CLIENT = "/api/search/client";

    private final Supplier<RequestSpecification> spec;

    private ClientSearchApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    public ClientSearchApi(String token) {
        this(() -> SaqtaRequestSpec.json(token));
    }

    /** Без токена — для проверки 401 */
    public static ClientSearchApi anonymous() {
        return new ClientSearchApi(SaqtaRequestSpec::anonymousJson);
    }

    @Step("Поиск клиентов: {request}")
    public ValidatableResponse search(ClientSearchRequest request) {
        return given()
                .spec(spec.get())
                .body(request)
                .when()
                .post(SEARCH_CLIENT)
                .then();
    }

    /** Успешный поиск: HTTP 200 и status = true, ответ распарсен */
    public ClientSearchResponse.Page found(ClientSearchRequest request) {
        return search(request)
                .statusCode(200)
                .body("status", is(true))
                .extract()
                .as(ClientSearchResponse.class)
                .data();
    }
}
