package life.cabinet.api;

import life.utils.XmlRequestBuilder;
import io.qameta.allure.Step;

import static io.restassured.RestAssured.given;

public class SessionApi {
    private static final String URL = "https://app.ffin.life/ws";
    private static final String AUTH = "Basic a2FzcGk6a2FzcGk=";

    @Step("Получить sessionId")
    public static String getSessionId() {
        String xmlBody = XmlRequestBuilder.buildSessionIdRequest();

        String response = given()
                .header("Content-Type", "text/xml")
                .header("Authorization", AUTH)
                .body(xmlBody)
                .when()
                .post(URL)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Просто ищем sessionId в тексте
        String regex = "<ns2:sessionId>(.*?)</ns2:sessionId>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException("SessionId не найден в ответе: " +
                response.substring(0, Math.min(300, response.length())));
    }
}