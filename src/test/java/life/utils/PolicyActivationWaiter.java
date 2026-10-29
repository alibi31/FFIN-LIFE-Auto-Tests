package life.utils;

import life.cabinet.api.PolicyApi;
import life.cabinet.api.SessionApi;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

/**
 * Заменяет безусловный Thread.sleep(90) на polling: пытается активировать
 * полис, пока backend не обработает регистрацию (или пока не истечёт таймаут).
 */
public final class PolicyActivationWaiter {

    private PolicyActivationWaiter() {}

    public static void activateWhenReady(String policyNumber) {
        await("activation of policy " + policyNumber)
                .atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(10))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    String sessionId = SessionApi.getSessionId();
                    PolicyApi.activatePolicy(sessionId, policyNumber); // уже кидает исключение при неудаче
                });
    }
}