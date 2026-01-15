package life.core.http;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import life.core.http.filters.AllureFilter;

public class RequestSpec {

    public static RequestSpecification json(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setAccept("application/json")
                .setContentType("application/json")
                .setRelaxedHTTPSValidation()
                .addFilter(new AllureFilter())
                .log(LogDetail.URI)
                .log(LogDetail.BODY)
                .build();
    }
}
