package life.saqtapro.page;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import life.core.web.BasePage;
import life.core.web.Pages;
import life.saqtapro.auth.TestUser;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Локаторы SaqtaPro — через data-testid (getByTestId): они не ломаются при смене текстов
 * и вёрстки. Атрибуты нужно согласовать с фронтендом (список — в README).
 */
public class SaqtaLoginPage extends BasePage<SaqtaLoginPage> {

    private Locator login;
    private Locator password;
    private Locator submit;

    @Override
    public void initComponents() {
        login = page.getByTestId("login-username");
        password = page.getByTestId("login-password");
        submit = page.getByTestId("login-submit");
    }

    @Override
    public SaqtaLoginPage checkThatPageLoaded() {
        assertThat(submit).isVisible();
        return this;
    }

    @Step("Войти в SaqtaPro как {user}")
    public ShellPage loginAs(TestUser user) {
        login.fill(user.login());
        password.fill(user.password());
        submit.click();
        return Pages.on(ShellPage.class).checkThatPageLoaded();
    }
}
