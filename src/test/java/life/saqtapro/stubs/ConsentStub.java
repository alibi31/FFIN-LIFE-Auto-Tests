package life.saqtapro.stubs;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.qameta.allure.Step;
import life.utils.data.TestPerson;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * SMS-согласие через eGov: SaqtaPro отправляет запрос, клиент подтверждает на телефоне,
 * SaqtaPro опрашивает статус. Реальная доставка SMS не нужна — исход задаётся стабом.
 */
public final class ConsentStub {

    private final GovStubs stubs;

    ConsentStub(GovStubs stubs) {
        this.stubs = stubs;
    }

    /** Первый опрос — PENDING, следующие — APPROVED: так проверяется, что SaqtaPro реально опрашивает */
    @Step("SMS-согласие: клиент подтвердил (после одного PENDING)")
    public ConsentStub approved(TestPerson person) {
        sendAccepted(person);
        String scenario = "consent-" + person.iin();
        stubs.register(status(person).inScenario(scenario)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(GovPayloads.consent(person.iin(), "PENDING")))
                .willSetStateTo("approved"));
        stubs.register(status(person).inScenario(scenario)
                .whenScenarioStateIs("approved")
                .willReturn(okJson(GovPayloads.consent(person.iin(), "APPROVED"))));
        return this;
    }

    @Step("SMS-согласие: клиент отклонил запрос")
    public ConsentStub declined(TestPerson person) {
        sendAccepted(person);
        stubs.register(status(person).willReturn(okJson(GovPayloads.consent(person.iin(), "DECLINED"))));
        return this;
    }

    @Step("SMS-согласие: клиент не отвечает")
    public ConsentStub noAnswer(TestPerson person) {
        sendAccepted(person);
        stubs.register(status(person).willReturn(okJson(GovPayloads.consent(person.iin(), "PENDING"))));
        return this;
    }

    @Step("SMS-согласие: шлюз не смог отправить SMS (503)")
    public ConsentStub sendFails(TestPerson person) {
        stubs.register(send(person).willReturn(aResponse().withStatus(503)));
        return this;
    }

    public int sendCallsFor(TestPerson person) {
        return stubs.count(postRequestedFor(urlPathEqualTo(GovPaths.EGOV_CONSENTS))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin()))));
    }

    private void sendAccepted(TestPerson person) {
        stubs.register(send(person).willReturn(okJson(GovPayloads.consent(person.iin(), "SENT"))));
    }

    private static MappingBuilder send(TestPerson person) {
        return post(urlPathEqualTo(GovPaths.EGOV_CONSENTS))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin())));
    }

    private static MappingBuilder status(TestPerson person) {
        return get(urlPathEqualTo(GovPaths.EGOV_CONSENT_PREFIX + GovPaths.consentIdFor(person.iin())));
    }
}
