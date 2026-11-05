package life.saqtapro.test.api.gov;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Requests;
import life.saqtapro.config.Saqta;
import life.saqtapro.data.Iins;
import life.saqtapro.data.KzIban;
import life.saqtapro.data.Phones;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaApiTest;
import life.saqtapro.stubs.EgovStub;
import life.saqtapro.stubs.GovStubs;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static life.saqtapro.api.model.Statuses.Verification.COMPLETED;
import static life.saqtapro.api.model.Statuses.Verification.CONSENT_DECLINED;
import static life.saqtapro.api.model.Statuses.Verification.DATA_MISMATCH;
import static life.saqtapro.api.model.Statuses.Verification.GOV_SERVICE_UNAVAILABLE;
import static life.saqtapro.api.model.Statuses.Verification.PERSON_NOT_FOUND;
import static life.saqtapro.api.model.Statuses.Verification.PHONE_NOT_OWNED;
import static life.saqtapro.api.model.Statuses.Verification.PHONE_NOT_REGISTERED;
import static life.saqtapro.api.model.Statuses.Verification.SMS_SEND_FAILED;
import static life.saqtapro.auth.TestUser.AGENT_A;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Интеграция с гос. сервисами и автозаполнение (раздел 4 стратегии).
 * Каждый тест задаёт поведение БМГ / eGov / SMS-согласия стабами и проверяет две вещи:
 * итоговый статус в SaqtaPro и то, какие внешние вызовы SaqtaPro реально сделал.
 */
@SaqtaApiTest
@Tag("gov-integration")
@Feature("Интеграция с гос. сервисами")
class GovVerificationTest {

    @Test
    @Story("Успешный сценарий")
    @DisplayName("GOV-01: ИИН + телефон проходят БМГ и eGov, согласие получено, форма заполнена")
    void happyPathAutofillsPersonData(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = stubs.happyPath(person);

        String id = agent.gov().startAndGetId(person.iin(), person.phone());

        agent.gov().awaitTerminal(id)
                .body("status", equalTo(COMPLETED.name()))
                .body("source", equalTo("EGOV"))
                .body("person.fullName", equalTo(person.fullName()))
                .body("person.birthDate", equalTo(person.birthDateStr()))
                .body("person.address", equalTo(person.address()))
                .body("person.document.number", equalTo(person.document().number()))
                .body("person.relatives.iin", hasItem(egov.relative().iin()))
                .body("person.iban", equalTo(egov.iban()));

        assertEquals(1, stubs.consent().sendCallsFor(person), "SMS-запрос согласия отправлен ровно один раз");
    }

    static Stream<String> malformedIins() {
        return Iins.malformed();
    }

    @ParameterizedTest(name = "GOV-02/03: ИИН «{0}» отклоняется до обращения во внешние сервисы")
    @MethodSource("malformedIins")
    @Story("Валидация ИИН")
    @DisplayName("GOV-02/03: невалидный ИИН")
    void malformedIinIsRejectedLocally(String badIin, @As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();

        agent.gov().start(new Requests.StartGovVerification(badIin, person.phone()))
                .statusCode(400)
                .body("errors.field", hasItem("iin"));

        assertEquals(0, stubs.bmg().callsFor(person.phone()),
                "Невалидный ИИН не должен порождать платный запрос в БМГ");
    }

    @Test
    @Story("Ошибки связки ИИН + телефон")
    @DisplayName("GOV-04: номера нет в БМГ — eGov и SMS не вызываются")
    void phoneNotInBmg(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberNotRegistered(person.phone());

        finish(agent, person).body("status", equalTo(PHONE_NOT_REGISTERED.name()));

        assertEquals(0, stubs.egov().ownershipCallsFor(person));
        assertEquals(0, stubs.consent().sendCallsFor(person));
    }

    @Test
    @Story("Ошибки связки ИИН + телефон")
    @DisplayName("GOV-05: номер не принадлежит ИИН — SMS-согласие не отправляется")
    void phoneNotOwnedByIin(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneNotOwnedBy(person);

        finish(agent, person)
                .body("status", equalTo(PHONE_NOT_OWNED.name()))
                .body("person", nullValue());

        assertEquals(0, stubs.consent().sendCallsFor(person));
        assertEquals(0, stubs.egov().personDataCallsFor(person));
    }

    @Test
    @Story("Ошибки связки ИИН + телефон")
    @DisplayName("GOV-06: ИИН валиден, но человек не найден в eGov")
    void personNotFoundInEgov(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person).personNotFound(person);
        stubs.consent().approved(person);

        finish(agent, person).body("status", equalTo(PERSON_NOT_FOUND.name()));
    }

    @Test
    @Story("SMS-согласие")
    @DisplayName("GOV-07: клиент отклонил согласие — персональные данные не запрашиваются")
    void consentDeclined(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person);
        stubs.consent().declined(person);

        finish(agent, person).body("status", equalTo(CONSENT_DECLINED.name()));

        assertEquals(0, stubs.egov().personDataCallsFor(person),
                "Без согласия SaqtaPro не имеет права запрашивать персональные данные");
    }

    @Test
    @Story("SMS-согласие")
    @DisplayName("GOV-09: шлюз не смог отправить SMS — понятная ошибка, без повторной отправки")
    void smsSendFails(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person);
        stubs.consent().sendFails(person);

        finish(agent, person).body("status", equalTo(SMS_SEND_FAILED.name()));
        assertEquals(1, stubs.consent().sendCallsFor(person));
    }

    @Test
    @Story("Отказоустойчивость")
    @DisplayName("GOV-11: БМГ зависает дольше таймаута — SaqtaPro не висит и сообщает о недоступности")
    void bmgTimeout(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        int clientTimeout = Saqta.cfg().govClientTimeoutMs();
        stubs.bmg().respondsAfter(person.phone(), Duration.ofMillis(clientTimeout + 3_000L));

        // Запуск асинхронный: агент получает ответ сразу, а не ждёт внешний контур
        String id = agent.gov().start(new Requests.StartGovVerification(person.iin(), person.phone()))
                .statusCode(202)
                .time(lessThan(2_000L))
                .extract().path("verificationId");

        agent.gov().awaitTerminal(id).body("status", equalTo(GOV_SERVICE_UNAVAILABLE.name()));
    }

    @Test
    @Story("Отказоустойчивость")
    @DisplayName("GOV-11: разовый сбой БМГ (503) перекрывается повтором")
    void bmgRetryAfterSingleFailure(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().failsOnceThenRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person);
        stubs.consent().approved(person);
        stubs.compliance().clean(person);
        stubs.egov().personData(person);

        finish(agent, person).body("status", equalTo(COMPLETED.name()));
        assertEquals(2, stubs.bmg().callsFor(person.phone()), "Ожидается ровно один повтор");
    }

    @Test
    @Story("Качество данных")
    @DisplayName("GOV-12: eGov отдал неполные данные — заполнено то, что пришло")
    void partialPersonData(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person).personDataPartial(person);
        stubs.consent().approved(person);

        finish(agent, person)
                .body("status", equalTo(COMPLETED.name()))
                .body("person.fullName", equalTo(person.fullName()))
                .body("person.address", nullValue())
                .body("person.iban", nullValue());
    }

    @Test
    @Story("Качество данных")
    @DisplayName("GOV-13: в ответе eGov чужой ИИН — данные отбрасываются")
    void foreignIinInEgovResponse(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person).personDataWithForeignIin(person);
        stubs.consent().approved(person);

        finish(agent, person)
                .body("status", equalTo(DATA_MISMATCH.name()))
                .body("person", nullValue());
    }

    @Test
    @Story("Качество данных")
    @DisplayName("GOV-14: IBAN из eGov с неверной контрольной суммой не подставляется")
    void invalidIbanIsNotAutofilled(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneBelongsTo(person);
        stubs.consent().approved(person);
        stubs.egov().personData(person, KzIban.withBadChecksum());

        finish(agent, person)
                .body("status", equalTo(COMPLETED.name()))
                .body("person.iban", nullValue())
                .body("warnings.code", hasItem("INVALID_IBAN"));
    }

    @Test
    @Story("Идемпотентность")
    @DisplayName("GOV-17: двойной клик «Получить данные» — один запрос в БМГ")
    void doubleSubmitHitsBmgOnce(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.happyPath(person);
        Requests.StartGovVerification request = new Requests.StartGovVerification(person.iin(), person.phone());

        CompletableFuture<ValidatableResponse> first = CompletableFuture.supplyAsync(() -> agent.gov().start(request));
        CompletableFuture<ValidatableResponse> second = CompletableFuture.supplyAsync(() -> agent.gov().start(request));

        String id = first.join().statusCode(202).extract().path("verificationId");
        // второй запрос: либо тот же verificationId, либо 409 «верификация уже идёт»
        second.join().statusCode(anyOf(is(202), is(409)));

        agent.gov().awaitTerminal(id).body("status", equalTo(COMPLETED.name()));
        assertEquals(1, stubs.bmg().callsFor(person.phone()));
    }

    @Test
    @Story("Безопасность")
    @DisplayName("GOV-16: ответ верификации не раскрывает полный номер телефона")
    void phoneIsMaskedInResponse(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.happyPath(person);

        ValidatableResponse done = finish(agent, person).body("phoneMasked", notNullValue());
        String body = done.extract().asString();
        assertEquals(-1, body.indexOf(Phones.digits(person.phone())),
                "Полный номер телефона не должен возвращаться в ответе");
    }

    private static ValidatableResponse finish(SaqtaApi agent, TestPerson person) {
        String id = agent.gov().startAndGetId(person.iin(), person.phone());
        return agent.gov().awaitTerminal(id);
    }
}
