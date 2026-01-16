package life.core.http;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import life.core.http.filters.AllureFilter;
import io.restassured.http.ContentType;

public class RequestSpec {

    public static RequestSpecification json(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setAccept("application/json")
                .setContentType(ContentType.JSON.withCharset("UTF-8"))
                .setRelaxedHTTPSValidation()
                .addFilter(new AllureFilter())
                .log(LogDetail.URI)
                .log(LogDetail.BODY)
                .build();
    }
}
