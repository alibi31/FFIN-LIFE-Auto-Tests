package life.saqtapro.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import life.core.http.filters.AllureFilter;
import life.saqtapro.config.Saqta;

import java.util.List;

/**
 * Спецификации запросов SaqtaPro. Общий life.core.http.RequestSpec.json() жёстко ставит
 * Content-Type: application/json, а SaqtaPro отправляет договоры как multipart/form-data
 * (JSON лежит в поле "data"), поэтому здесь своя сборка.
 */
public final class SaqtaRequestSpec {

    /** Без этого заголовка backend не знает, что договор пришёл из SaqtaPro (sales_channel = saqta) */
    private static final String SALES_CHANNEL_HEADER = "x-sales-channel";
    private static final String SALES_CHANNEL = "saqta";

    private static final RestAssuredConfig CONFIG = RestAssuredConfig.config()
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
            .logConfig(LogConfig.logConfig().blacklistHeaders(List.of("Authorization")));

    private SaqtaRequestSpec() {}

    public static RequestSpecification multipart(String bearerToken) {
        return base(bearerToken)
                .setContentType("multipart/form-data")
                .build();
    }

    public static RequestSpecification json(String bearerToken) {
        return base(bearerToken)
                .setContentType("application/json; charset=UTF-8")
                .build();
    }

    /**
     * Без токена — для логина. Не через life.core.http.RequestSpec.json(): тот пишет тело запроса
     * в консоль (LogDetail.BODY), и пароль попал бы в лог CI. Здесь логируется только URI.
     */
    public static RequestSpecification anonymousJson() {
        return common()
                .setContentType("application/json; charset=UTF-8")
                .build();
    }

    private static RequestSpecBuilder base(String bearerToken) {
        return common().addHeader("Authorization", "Bearer " + bearerToken);
    }

    private static RequestSpecBuilder common() {
        return new RequestSpecBuilder()
                .setConfig(CONFIG)
                .setBaseUri(Saqta.cfg().apiUrl())
                .setAccept("application/json")
                .addHeader(SALES_CHANNEL_HEADER, SALES_CHANNEL)
                .setRelaxedHTTPSValidation()
                .addFilter(new AllureFilter())
                .log(LogDetail.URI);
    }
}
