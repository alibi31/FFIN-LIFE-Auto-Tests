package life.saqtapro.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import life.saqtapro.api.model.Requests;
import life.saqtapro.api.model.Statuses;
import life.saqtapro.config.Saqta;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;

/**
 * Запуск и ожидание цепочки БМГ → eGov → SMS-согласие → автозаполнение.
 * Цепочка асинхронная: клиент подтверждает согласие на своём телефоне, SaqtaPro опрашивает eGov.
 */
public final class GovVerificationApi {

    private final Supplier<RequestSpecification> spec;

    GovVerificationApi(Supplier<RequestSpecification> spec) {
        this.spec = spec;
    }

    @Step("Запустить гос. верификацию по ИИН и телефону")
    public ValidatableResponse start(Requests.StartGovVerification request) {
        return spec.get().body(request).when().post(SaqtaEndpoints.GOV_VERIFICATIONS).then();
    }

    @Step("Получить состояние гос. верификации {verificationId}")
    public ValidatableResponse get(String verificationId) {
        return spec.get().pathParam("verificationId", verificationId)
                .when().get(SaqtaEndpoints.GOV_VERIFICATION).then();
    }

    /** Запустить и вернуть id; ожидается 202 Accepted */
    public String startAndGetId(String iin, String phone) {
        return start(new Requests.StartGovVerification(iin, phone))
                .statusCode(202)
                .extract().path("verificationId");
    }

    /**
     * Дождаться финального статуса (любого, кроме CONSENT_REQUESTED) и вернуть ответ целиком.
     * Опрос вместо sleep: тест не ждёт дольше, чем нужно стабу WireMock.
     */
    @Step("Дождаться завершения гос. верификации {verificationId}")
    public ValidatableResponse awaitTerminal(String verificationId) {
        AtomicReference<ValidatableResponse> last = new AtomicReference<>();
        await("верификация " + verificationId + " завершилась")
                .atMost(Duration.ofSeconds(Saqta.cfg().govVerificationTimeoutSeconds()))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    ValidatableResponse r = get(verificationId).statusCode(200);
                    last.set(r);
                    return Statuses.Verification.valueOf(r.extract().path("status")).isTerminal();
                });
        return last.get();
    }
}
