package life.saqtapro.stubs;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import io.qameta.allure.Step;
import life.saqtapro.data.KzIban;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/** eGov: связка ИИН ↔ телефон и персональные данные после согласия */
public final class EgovStub {

    /** Что eGov «вернул» — тест сверяет с этим автозаполнение */
    public record EgovPerson(TestPerson person, TestPerson relative, String iban) {}

    private final GovStubs stubs;

    EgovStub(GovStubs stubs) {
        this.stubs = stubs;
    }

    // ---------- Связка ИИН + телефон ----------

    @Step("eGov: номер принадлежит ИИН клиента")
    public EgovStub phoneBelongsTo(TestPerson person) {
        stubs.register(ownership(person).willReturn(okJson(GovPayloads.phoneOwnership(person, true))));
        return this;
    }

    @Step("eGov: номер НЕ принадлежит ИИН клиента")
    public EgovStub phoneNotOwnedBy(TestPerson person) {
        stubs.register(ownership(person).willReturn(okJson(GovPayloads.phoneOwnership(person, false))));
        return this;
    }

    @Step("eGov: проверка связки отвечает через {delay}")
    public EgovStub ownershipRespondsAfter(TestPerson person, Duration delay) {
        stubs.register(ownership(person).willReturn(okJson(GovPayloads.phoneOwnership(person, true))
                .withFixedDelay((int) delay.toMillis())));
        return this;
    }

    // ---------- Персональные данные ----------

    @Step("eGov: отдаёт данные клиента, супруга и валидный IBAN")
    public EgovPerson personData(TestPerson person) {
        return personData(person, KzIban.valid());
    }

    @Step("eGov: отдаёт данные клиента с IBAN {iban}")
    public EgovPerson personData(TestPerson person, String iban) {
        TestPerson spouse = TestPersonFactory.adult();
        stubs.register(personRequest(person).willReturn(okJson(GovPayloads.person(person, spouse, iban))));
        return new EgovPerson(person, spouse, iban);
    }

    @Step("eGov: в ответе ИИН другого человека")
    public EgovStub personDataWithForeignIin(TestPerson person) {
        String foreignIin = TestPersonFactory.adult().iin();
        stubs.register(personRequest(person)
                .willReturn(okJson(GovPayloads.person(person, foreignIin, null, KzIban.valid()))));
        return this;
    }

    @Step("eGov: данные неполные (нет адреса, документа, родственников, IBAN)")
    public EgovStub personDataPartial(TestPerson person) {
        stubs.register(personRequest(person).willReturn(okJson(GovPayloads.personPartial(person))));
        return this;
    }

    @Step("eGov: человек с таким ИИН не найден")
    public EgovStub personNotFound(TestPerson person) {
        stubs.register(personRequest(person).willReturn(aResponse().withStatus(404)));
        return this;
    }

    @Step("eGov: сервис недоступен (503)")
    public EgovStub unavailable(TestPerson person) {
        stubs.register(ownership(person).willReturn(aResponse().withStatus(503)));
        stubs.register(personRequest(person).willReturn(aResponse().withStatus(503)));
        return this;
    }

    public int ownershipCallsFor(TestPerson person) {
        return stubs.count(postRequestedFor(urlPathEqualTo(GovPaths.EGOV_PHONE_OWNERSHIP))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin()))));
    }

    public int personDataCallsFor(TestPerson person) {
        return stubs.count(getRequestedFor(urlPathEqualTo(GovPaths.EGOV_PERSON_PREFIX + person.iin())));
    }

    private static MappingBuilder ownership(TestPerson person) {
        return post(urlPathEqualTo(GovPaths.EGOV_PHONE_OWNERSHIP))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin())));
    }

    private static MappingBuilder personRequest(TestPerson person) {
        return get(urlPathEqualTo(GovPaths.EGOV_PERSON_PREFIX + person.iin()));
    }
}
