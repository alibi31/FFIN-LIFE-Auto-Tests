package life.saqtapro.stubs;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.qameta.allure.Step;
import life.utils.data.TestPerson;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Управление подменой гос. сервисов из теста. WireMock работает отдельным процессом (контейнер / под),
 * SaqtaPro ходит в него вместо БМГ / eGov / комплаенса, а тест через admin API:
 *  1) объявляет, как «внешний мир» ответит именно для его ИИН и телефона;
 *  2) проверяет, какие запросы SaqtaPro реально отправил;
 *  3) после теста удаляет только свои стабы (close()).
 *
 * Изоляция параллельных тестов — по данным: у каждого теста свой синтетический ИИН и телефон,
 * стабы матчатся по ним, поэтому общий resetAll() не нужен и запрещён.
 */
public final class GovStubs implements AutoCloseable {

    private final WireMock wireMock;
    private final List<StubMapping> created = new CopyOnWriteArrayList<>();

    public GovStubs(String wiremockUrl) {
        URI uri = URI.create(wiremockUrl);
        int port = uri.getPort() != -1 ? uri.getPort() : ("https".equals(uri.getScheme()) ? 443 : 80);
        this.wireMock = new WireMock(uri.getScheme(), uri.getHost(), port);
    }

    public BmgStub bmg() {
        return new BmgStub(this);
    }

    public EgovStub egov() {
        return new EgovStub(this);
    }

    public ConsentStub consent() {
        return new ConsentStub(this);
    }

    public ComplianceStub compliance() {
        return new ComplianceStub(this);
    }

    /**
     * Все внешние проверки проходят: номер в БМГ, номер принадлежит ИИН, согласие подтверждено,
     * eGov отдаёт данные, клиент не в списках.
     */
    @Step("Стабы: клиент проходит БМГ, eGov, SMS-согласие и комплаенс")
    public EgovStub.EgovPerson happyPath(TestPerson person) {
        bmg().numberRegistered(person.phone());
        egov().phoneBelongsTo(person);
        consent().approved(person);
        compliance().clean(person);
        return egov().personData(person);
    }

    @Step("Проверить: ни одного запроса во внешние сервисы по ИИН и телефону клиента")
    public void verifyNoExternalCallsFor(TestPerson person) {
        int byIin = count(anyRequestedFor(urlMatching("/(bmg|egov|compliance)/.*"))
                .withRequestBody(containing(person.iin())));
        int byPhone = bmg().callsFor(person.phone());
        assertEquals(0, byIin + byPhone,
                "SaqtaPro не должен обращаться к гос. сервисам, пока данные не прошли локальную валидацию");
    }

    // ---- для классов-стабов этого пакета ----

    StubMapping register(MappingBuilder mapping) {
        StubMapping stub = wireMock.register(mapping);
        created.add(stub);
        return stub;
    }

    int count(RequestPatternBuilder pattern) {
        return wireMock.find(pattern).size();
    }

    /** Удаляет только стабы этого теста; журнал запросов общий и не чистится */
    @Override
    public void close() {
        for (StubMapping stub : created) {
            try {
                wireMock.removeStubMapping(stub);
            } catch (RuntimeException ignored) {
                // стаб мог быть уже удалён — очистка не должна ронять тест
            }
        }
        created.clear();
    }
}
