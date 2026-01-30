package life.cabinet.api;

import life.utils.XmlRequestBuilder;
import io.qameta.allure.Step;

import static io.restassured.RestAssured.given;

public class PolicyApi {
    private static final String URL = "https://app.ffin.life/ws";
    private static final String AUTH = "Basic a2FzcGk6a2FzcGk=";
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 3000;

    @Step("Активировать полис {contractId}")
    public static void activatePolicy(String sessionId, String contractId) {
        int attempt = 0;
        boolean activated = false;

        while (attempt < MAX_RETRIES && !activated) {
            attempt++;
            System.out.println("Попытка активации полиса " + contractId + " (попытка " + attempt + ")");

            try {
                activated = tryActivatePolicy(sessionId, contractId);

                if (!activated && attempt < MAX_RETRIES) {
                    System.out.println("Повторная попытка через " + RETRY_DELAY_MS + "мс...");
                    Thread.sleep(RETRY_DELAY_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Активация прервана", e);
            }
        }

        if (!activated) {
            throw new RuntimeException("Не удалось активировать полис " + contractId +
                    " после " + MAX_RETRIES + " попыток");
        }
    }

    private static boolean tryActivatePolicy(String sessionId, String contractId) {
        String xmlBody = XmlRequestBuilder.buildActivatePolicyRequest(sessionId, contractId);

        String response = given()
                .header("Content-Type", "text/xml")
                .header("Authorization", AUTH)
                .body(xmlBody)
                .when()
                .post(URL)
                .then()
                .extract()
                .asString();

        return checkActivationResponse(response);
    }

    private static boolean checkActivationResponse(String response) {
        // Ищем ТОЛЬКО строки success и result
        boolean hasSuccess = response.contains("<ns2:success>true</ns2:success>");
        boolean hasResult = response.contains("<ns2:result>1</ns2:result>");

        // Если нашли оба - активация успешна
        if (hasSuccess && hasResult) {
            return true;
        }

        // Для отладки - логируем что нашли
        System.out.println("Проверка ответа:");
        System.out.println("- success=true: " + hasSuccess);
        System.out.println("- result=1: " + hasResult);

        return false;
    }
}