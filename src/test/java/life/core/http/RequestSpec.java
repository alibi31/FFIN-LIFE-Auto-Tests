package life.core.http;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import life.core.http.filters.AllureFilter;

import java.util.List;

public class RequestSpec {

    private static final List<String> SENSITIVE_HEADERS = List.of(
            "Authorization", "X-Auth-Token", "Proxy-Authorization", "Cookie", "Set-Cookie");

    private static final RestAssuredConfig CONFIG = RestAssuredConfig.config()
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
            .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
            .logConfig(LogConfig.logConfig().blacklistHeaders(SENSITIVE_HEADERS));

    public static RequestSpecification json(String baseUri) {
        return new RequestSpecBuilder()
                .setConfig(CONFIG)
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