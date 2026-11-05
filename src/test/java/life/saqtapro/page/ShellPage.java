package life.saqtapro.page;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import life.core.web.BasePage;
import life.core.web.Pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Каркас приложения после входа: меню разделов, видимость которых зависит от роли */
public class ShellPage extends BasePage<ShellPage> {

    public enum Section {
        POLICIES("nav-policies"),
        CLIENTS("nav-clients"),
        UNDERWRITING("nav-underwriting"),
        REPORTS("nav-reports");

        final String testId;

        Section(String testId) {
            this.testId = testId;
        }
    }

    private Locator userMenu;

    @Override
    public void initComponents() {
        userMenu = page.getByTestId("user-menu");
    }

    @Override
    public ShellPage checkThatPageLoaded() {
        assertThat(userMenu).isVisible();
        return this;
    }

    public Locator section(Section section) {
        return page.getByTestId(section.testId);
    }

    @Step("Раздел «{section}» отображается в меню")
    public ShellPage assertSectionVisible(Section section) {
        assertThat(section(section)).isVisible();
        return this;
    }

    /** Скрытый раздел должен отсутствовать в DOM, а не прятаться через CSS */
    @Step("Раздела «{section}» нет в DOM")
    public ShellPage assertSectionAbsent(Section section) {
        assertThat(section(section)).hasCount(0);
        return this;
    }

    @Step("Открыть форму нового клиента")
    public ClientFormPage openNewClientForm() {
        section(Section.CLIENTS).click();
        page.getByTestId("client-create").click();
        return Pages.on(ClientFormPage.class).checkThatPageLoaded();
    }

    @Step("Открыть вкладку андеррайтинга")
    public UnderwritingPage openUnderwriting() {
        section(Section.UNDERWRITING).click();
        return Pages.on(UnderwritingPage.class).checkThatPageLoaded();
    }
}
