package life.saqtapro.api;

import io.restassured.specification.RequestSpecification;
import life.core.http.RequestSpec;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.config.Saqta;

import java.util.function.Supplier;

/**
 * Фасад SaqtaPro API от имени конкретного пользователя.
 * Тест работает так: {@code SaqtaApi.as(AGENT_A).policies().create(...)} — без URL и заголовков в тесте.
 */
public final class SaqtaApi {

    private final String actor;
    private final Supplier<RequestSpecification> spec;

    private SaqtaApi(String actor, Supplier<RequestSpecification> spec) {
        this.actor = actor;
        this.spec = spec;
    }

    public static SaqtaApi as(TestUser user) {
        return new SaqtaApi(user.name(), () -> base().auth().oauth2(SaqtaAuth.tokenOf(user)));
    }

    /** Без токена — для проверок 401 */
    public static SaqtaApi anonymous() {
        return new SaqtaApi("anonymous", SaqtaApi::base);
    }

    /** С произвольным токеном — истёкшим, подделанным, без роли */
    public static SaqtaApi withRawToken(String token) {
        return new SaqtaApi("raw-token", () -> base().auth().oauth2(token));
    }

    /**
     * С произвольным значением заголовка Authorization как есть — для проверок
     * пустого "Bearer ", токена без префикса "Bearer" и т.п.
     */
    public static SaqtaApi withAuthorizationHeader(String headerValue) {
        return new SaqtaApi("raw-header", () -> base().header("Authorization", headerValue));
    }

    private static RequestSpecification base() {
        return io.restassured.RestAssured.given().spec(RequestSpec.json(Saqta.cfg().apiUrl()));
    }

    public ProfileApi profile() {
        return new ProfileApi(spec);
    }

    public ClientApi clients() {
        return new ClientApi(spec);
    }

    public GovVerificationApi gov() {
        return new GovVerificationApi(spec);
    }

    public PolicyApi policies() {
        return new PolicyApi(spec);
    }

    public UnderwritingApi underwriting() {
        return new UnderwritingApi(spec);
    }

    @Override
    public String toString() {
        return actor;
    }
}
