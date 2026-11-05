package life.saqtapro.stubs;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.qameta.allure.Step;
import life.saqtapro.data.Phones;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/** База мобильных граждан: есть ли номер в реестре */
public final class BmgStub {

    private final GovStubs stubs;

    BmgStub(GovStubs stubs) {
        this.stubs = stubs;
    }

    @Step("БМГ: номер {phone} зарегистрирован")
    public BmgStub numberRegistered(String phone) {
        stubs.register(forPhone(phone).willReturn(okJson(GovPayloads.bmgRegistered(phone, true))));
        return this;
    }

    @Step("БМГ: номера {phone} нет в базе")
    public BmgStub numberNotRegistered(String phone) {
        stubs.register(forPhone(phone).willReturn(okJson(GovPayloads.bmgRegistered(phone, false))));
        return this;
    }

    @Step("БМГ: сервис недоступен (503) для {phone}")
    public BmgStub unavailable(String phone) {
        stubs.register(forPhone(phone).willReturn(aResponse().withStatus(503)));
        return this;
    }

    @Step("БМГ: ответ для {phone} приходит через {delay}")
    public BmgStub respondsAfter(String phone, Duration delay) {
        stubs.register(forPhone(phone).willReturn(okJson(GovPayloads.bmgRegistered(phone, true))
                .withFixedDelay((int) delay.toMillis())));
        return this;
    }

    @Step("БМГ: соединение рвётся для {phone}")
    public BmgStub connectionReset(String phone) {
        stubs.register(forPhone(phone).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        return this;
    }

    /** Первый вызов 503, второй успешен — для проверки политики ретраев */
    @Step("БМГ: первый запрос по {phone} — 503, повторный — успех")
    public BmgStub failsOnceThenRegistered(String phone) {
        String scenario = "bmg-retry-" + Phones.digits(phone);
        stubs.register(forPhone(phone).inScenario(scenario)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("recovered"));
        stubs.register(forPhone(phone).inScenario(scenario)
                .whenScenarioStateIs("recovered")
                .willReturn(okJson(GovPayloads.bmgRegistered(phone, true))));
        return this;
    }

    public int callsFor(String phone) {
        return stubs.count(requestedFor(phone));
    }

    private static MappingBuilder forPhone(String phone) {
        return post(urlPathEqualTo(GovPaths.BMG_SUBSCRIBER_CHECK))
                .withRequestBody(matchingJsonPath("$.phone", matching(".*" + Phones.last10(phone) + "$")));
    }

    private static RequestPatternBuilder requestedFor(String phone) {
        return postRequestedFor(urlPathEqualTo(GovPaths.BMG_SUBSCRIBER_CHECK))
                .withRequestBody(matchingJsonPath("$.phone", matching(".*" + Phones.last10(phone) + "$")));
    }
}
