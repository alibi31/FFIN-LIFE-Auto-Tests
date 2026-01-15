package life.core.http;

import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;


import static io.restassured.RestAssured.given;

public class ApiClient {

    public ValidatableResponse get(String baseUri, String uriPath) {
        return given().spec(RequestSpec.json(baseUri))
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse get(String baseUri, String uriPath, Header headers) {
        return given().spec(RequestSpec.json(baseUri))
                .header(headers)
                .when().get(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .when().post(uriPath)
                .then();
    }

    public ValidatableResponse post(String baseUri, String uriPath, Object body, Header headers) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .header(headers)
                .when().post(uriPath)
                .then();
    }

    public ValidatableResponse put(String baseUri, String uriPath, Object body) {
        return given().spec(RequestSpec.json(baseUri))
                .body(body)
                .when().put(uriPath)
                .then();
    }

    public ValidatableResponse delete(String baseUri, String uriPath) {
        return given().spec(RequestSpec.json(baseUri))
                .when().delete(uriPath)
                .then();
    }
}
