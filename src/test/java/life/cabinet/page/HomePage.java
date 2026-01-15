package life.cabinet.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class HomePage extends BasePage<HomePage> {

    private Locator homeHeaderImg;


    @Override
    public void initComponents() {
        homeHeaderImg = page.locator("header")
                .filter(new Locator.FilterOptions().setHasText("Заявить о страховом случае")).getByRole(AriaRole.IMG).first();

    }

    @Override
    public HomePage checkThatPageLoaded() {
        assertThat(homeHeaderImg).isVisible();

        return this;
    }
}
