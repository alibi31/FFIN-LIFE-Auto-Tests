package life.saqtapro.test.web;

import io.qameta.allure.Feature;
import life.core.web.Pages;
import life.saqtapro.auth.TestUser;
import life.saqtapro.config.Saqta;
import life.saqtapro.data.Iins;
import life.saqtapro.jupiter.SaqtaWebTest;
import life.saqtapro.page.ClientFormPage;
import life.saqtapro.page.SaqtaLoginPage;
import life.saqtapro.stubs.EgovStub;
import life.saqtapro.stubs.GovStubs;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Сквозной UX-флоу автозаполнения: агент видит то, что вернул eGov, и понятные ошибки */
@SaqtaWebTest
@Tag("gov-integration")
@Feature("Интеграция с гос. сервисами")
class GovAutofillWebTest {

    @Test
    @DisplayName("GOV-01 UI: агент получает автозаполненную форму, IBAN маской, без родственников")
    void agentAutofill(GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        EgovStub.EgovPerson egov = stubs.happyPath(person);

        newClientFormAs(TestUser.AGENT_A)
                .requestGovData(person.iin(), person.phone())
                .assertVerificationStatus("Данные получены")
                .assertAutofilledFrom(egov, true)
                .assertRelativesHidden();
    }

    @Test
    @DisplayName("GOV-05 UI: номер не принадлежит ИИН — ошибка, SMS не отправлено")
    void phoneNotOwned(GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();
        stubs.bmg().numberRegistered(person.phone());
        stubs.egov().phoneNotOwnedBy(person);

        newClientFormAs(TestUser.AGENT_A)
                .requestGovData(person.iin(), person.phone())
                .assertVerificationStatus("Номер телефона не принадлежит")
                .assertNotAutofilled();
        assertEquals(0, stubs.consent().sendCallsFor(person));
    }

    @Test
    @DisplayName("GOV-02 UI: неверная контрольная сумма ИИН подсвечивается сразу")
    void badChecksumIsHighlighted(GovStubs stubs) {
        TestPerson person = TestPersonFactory.adult();

        newClientFormAs(TestUser.AGENT_A)
                .requestGovData(Iins.withBadChecksum(), person.phone())
                .assertIinError("Некорректный ИИН");
        assertEquals(0, stubs.bmg().callsFor(person.phone()));
    }

    private static ClientFormPage newClientFormAs(TestUser user) {
        return Pages.open(Saqta.cfg().webUrl() + "/login", SaqtaLoginPage.class)
                .loginAs(user)
                .openNewClientForm();
    }
}
