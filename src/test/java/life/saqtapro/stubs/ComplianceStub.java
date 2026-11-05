package life.saqtapro.stubs;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import io.qameta.allure.Step;
import life.saqtapro.api.model.Statuses.WatchList;
import life.utils.data.TestPerson;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Проверка по спискам (террористы/экстремисты, санкции, внутренний стоп-лист).
 * Чтобы смоделировать «клиент попал в список позже», достаточно вызвать listed(...) после clean(...):
 * WireMock отдаёт приоритет последнему добавленному стабу.
 */
public final class ComplianceStub {

    private final GovStubs stubs;

    ComplianceStub(GovStubs stubs) {
        this.stubs = stubs;
    }

    @Step("Комплаенс: клиент не найден в списках")
    public ComplianceStub clean(TestPerson person) {
        stubs.register(screening(person)
                .willReturn(okJson(GovPayloads.screening(person.iin(), "CLEAR", List.of()))));
        return this;
    }

    @Step("Комплаенс: точное совпадение в списке {list}")
    public ComplianceStub listed(TestPerson person, WatchList list) {
        stubs.register(screening(person)
                .willReturn(okJson(GovPayloads.screening(person.iin(), "MATCH", List.of(list)))));
        return this;
    }

    @Step("Комплаенс: частичное совпадение (ФИО + дата рождения), нужен ручной досмотр")
    public ComplianceStub possibleMatch(TestPerson person) {
        stubs.register(screening(person).willReturn(okJson(
                GovPayloads.screening(person.iin(), "POSSIBLE_MATCH", List.of(WatchList.TERRORISTS_EXTREMISTS)))));
        return this;
    }

    @Step("Комплаенс: провайдер недоступен (503)")
    public ComplianceStub unavailable(TestPerson person) {
        stubs.register(screening(person).willReturn(aResponse().withStatus(503)));
        return this;
    }

    @Step("Комплаенс: провайдер отвечает через {delay}")
    public ComplianceStub respondsAfter(TestPerson person, Duration delay) {
        stubs.register(screening(person).willReturn(
                okJson(GovPayloads.screening(person.iin(), "CLEAR", List.of())).withFixedDelay((int) delay.toMillis())));
        return this;
    }

    public int callsFor(TestPerson person) {
        return stubs.count(postRequestedFor(urlPathEqualTo(GovPaths.COMPLIANCE_SCREENING))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin()))));
    }

    private static MappingBuilder screening(TestPerson person) {
        return post(urlPathEqualTo(GovPaths.COMPLIANCE_SCREENING))
                .withRequestBody(matchingJsonPath("$.iin", equalTo(person.iin())));
    }
}
