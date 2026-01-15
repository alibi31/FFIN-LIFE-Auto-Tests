package life.ffin.page;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MainPage extends BasePage<MainPage> {

    private Locator header;

    @Override
    public void initComponents() {
        header = page.locator(".header");
    }

    @Override
    public MainPage checkThatPageLoaded() {
        return checkMainHeader();
    }

    @Step("Проверить хедер на главной странице")
    public MainPage checkMainHeader() {
        assertThat(header).isVisible();
        return this;
    }
}
