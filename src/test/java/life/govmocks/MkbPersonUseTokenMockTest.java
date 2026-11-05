package life.govmocks;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;
import life.core.http.ApiClient;
import life.utils.data.IinGenerator;
import life.utils.data.IinGenerator.Gender;
import life.utils.data.IinGenerator.KdpScenario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Проверка мока МКБ person-use-token (gov-mocks/mappings/mkb).
 * Нужен запущенный мок: cd gov-mocks && docker compose up -d
 * Если мок не запущен, тесты пропускаются (skipped), а не падают.
 * Адрес можно переопределить: -DgovMocksUrl=http://host:port
 */
@Tag("gov-mocks")
@Owner("QA")
@DisplayName("Мок МКБ: запрос согласия на доступ к ПД (person-use-token)")
public class MkbPersonUseTokenMockTest {

    private static final String BASE_URL = System.getProperty("govMocksUrl", "http://localhost:8089");
    private static final String ENDPOINT = "/api/v1/soap/mkb/person-use-token";
    private static final Header API_KEY = new Header("x-api-key", "test");

    private final ApiClient apiClient = new ApiClient();

    @BeforeAll
    static void mockIsRunning() {
        URI uri = URI.create(BASE_URL);
        boolean up;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), 1000);
            up = true;
        } catch (Exception e) {
            up = false;
        }
        assumeTrue(up, "Мок не запущен на " + BASE_URL + " — запустите: cd gov-mocks && docker compose up -d");
    }

    private static Map<String, Object> body(String iin) {
        Map<String, Object> params = new HashMap<>();
        params.put("iin", iin);
        Map<String, Object> body = new HashMap<>();
        body.put("params", params);
        body.put("is_test", false);
        body.put("request_kdp", true);
        return body;
    }

    private ValidatableResponse send(String iin) {
        Allure.parameter("ИИН", iin);
        return apiClient.post(BASE_URL, ENDPOINT, body(iin), API_KEY);
    }

    // ---------- 200: клиент дал согласие (511) ----------

    static Stream<Arguments> approvedPersons() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                Arguments.of("Мужчина 35 лет", today.minusYears(35), Gender.MALE, "1"),
                Arguments.of("Женщина 35 лет", today.minusYears(35), Gender.FEMALE, "2"),
                Arguments.of("Ровно 18 лет сегодня", today.minusYears(18), Gender.MALE, "1"),
                Arguments.of("18 лет исполнится завтра", today.minusYears(18).plusDays(1), Gender.FEMALE, "2"),
                Arguments.of("Родилась в 1960-х (19xx)", LocalDate.of(1962, 7, 9), Gender.FEMALE, "2")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("approvedPersons")
    @DisplayName("Согласие (511) -> 200, дата рождения и пол берутся из ИИН")
    @Description("ИИН с порядковым номером 0001-8999: мок отвечает как после ответа клиента 511")
    void approvedReturnsPersonFromIin(String caseName, LocalDate birthDate, Gender gender, String genderCode) {
        String iin = IinGenerator.generate(birthDate, gender);

        send(iin)
                .statusCode(200)
                .contentType(JSON)
                .body("success", is(true))
                .body("is_test", is(false))
                .body("data.processingStatus.code", is("00000"))
                .body("data.person.iin", is(iin))
                .body("data.person.birthDate", is(birthDate.toString()))
                .body("data.person.gender.code", is(genderCode))
                .body("data.person.documents.document[0].birthDate", is(birthDate.toString()))
                .body("data.responseInfo.sessionId", matchesPattern("^\\{[0-9a-f-]{36}}$"));
    }

    // ---------- 202 / 502: клиент не ответил / отказал ----------

    @Test
    @DisplayName("Клиент ещё не ответил на SMS -> 202 PENDING")
    void pendingReturns202() {
        String iin = IinGenerator.generate(LocalDate.now().minusYears(30), Gender.MALE, KdpScenario.PENDING);

        send(iin)
                .statusCode(202)
                .body("success", is(false))
                .body("kdp_status", is("PENDING"));
    }

    @Test
    @DisplayName("Клиент отказал (512) -> 502 INVALID")
    void declinedReturns502() {
        String iin = IinGenerator.generate(LocalDate.now().minusYears(30), Gender.FEMALE, KdpScenario.DECLINED);

        send(iin)
                .statusCode(502)
                .body("success", is(false))
                .body("kdp_status", is("INVALID"))
                .body("error", is("Субъект отказал в доступе."));
    }

    // ---------- 400 / 401: ошибки запроса ----------

    @ParameterizedTest(name = "iin=''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {"1", "12345678901", "1234567890123", "12345abc6789", "85031530012!"})
    @DisplayName("ИИН не из 12 цифр -> 400")
    void invalidIinReturns400(String iin) {
        send(iin)
                .statusCode(400)
                .body("success", is(false))
                .body("error", is("params.iin обязателен (12 цифр)"));
    }

    @Test
    @DisplayName("Без x-api-key -> 401")
    void withoutApiKeyReturns401() {
        String iin = IinGenerator.generate(LocalDate.now().minusYears(30), Gender.MALE);

        apiClient.post(BASE_URL, ENDPOINT, body(iin))
                .statusCode(401)
                .body("success", is(false))
                .body("error.detail", is("Учетные данные не были предоставлены."))
                .body("status_code", is(401));
    }
}