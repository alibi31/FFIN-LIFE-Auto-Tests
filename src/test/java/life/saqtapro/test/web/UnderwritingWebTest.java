package life.saqtapro.test.web;

import io.qameta.allure.Feature;
import life.core.web.Pages;
import life.saqtapro.api.SaqtaApi;
import life.saqtapro.api.model.Statuses.Policy;
import life.saqtapro.auth.TestUser;
import life.saqtapro.config.Saqta;
import life.saqtapro.jupiter.As;
import life.saqtapro.jupiter.SaqtaWebTest;
import life.saqtapro.page.SaqtaLoginPage;
import life.saqtapro.steps.SaqtaSteps;
import life.saqtapro.stubs.GovStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static life.saqtapro.auth.TestUser.AGENT_A;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Комбинированный сценарий: данные готовятся через API (быстро и стабильно),
 * в UI проверяется только то, что делает андеррайтер, результат — снова через API.
 */
@SaqtaWebTest
@Tag("underwriting")
@Feature("Андеррайтинг")
class UnderwritingWebTest {

    @Test
    @DisplayName("UW-04 UI: андеррайтер одобряет договор из очереди")
    void underwriterApprovesFromQueue(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        Pages.open(Saqta.cfg().webUrl() + "/login", SaqtaLoginPage.class)
                .loginAs(TestUser.UNDERWRITER)
                .openUnderwriting()
                .assertInQueue(policyId)
                .open(policyId)
                .approve("Риск приемлемый, автотест UI");

        assertEquals(Policy.APPROVED, SaqtaSteps.statusOf(agent, policyId));
    }

    @Test
    @DisplayName("UW-07 UI: андеррайтер отклоняет договор с причиной")
    void underwriterRejectsWithReason(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
        String policyId = SaqtaSteps.policyUnderReview(agent, stubs).id();

        Pages.open(Saqta.cfg().webUrl() + "/login", SaqtaLoginPage.class)
                .loginAs(TestUser.UNDERWRITER)
                .openUnderwriting()
                .open(policyId)
                .reject("Нет медицинского обследования");

        assertEquals(Policy.REJECTED, SaqtaSteps.statusOf(agent, policyId));
    }
}
