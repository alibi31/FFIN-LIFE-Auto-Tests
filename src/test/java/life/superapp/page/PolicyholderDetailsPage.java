package life.superapp.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PolicyholderDetailsPage extends BasePage<PolicyholderDetailsPage> {

    private Locator cardsBottomSheet;
    private Locator card;
    private Locator continueButton;
    private Locator selectCardInput;
    private Locator insuredDataHeader;

    @Override
    public void initComponents() {
        this.cardsBottomSheet = page.locator(
                ".bottom-sheet__main:has-text(\"Карта для зачисления средств\")"
        );
        this.card = cardsBottomSheet.locator(".appSelect__list-item").first();
        this.continueButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Продолжить")
        );
        this.selectCardInput = page.locator(
                "div.appSelect__header:has-text(\"Карта для зачисления средств\") + " +
                        "div.appSelect__field input.v-field__input[placeholder='Выберите из списка']"
        );
        this.insuredDataHeader = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Данные страхователя")
                        .setLevel(1)
        );
    }

    @Override
    public PolicyholderDetailsPage checkThatPageLoaded() {
        insuredDataHeader.waitFor();
        assertThat(insuredDataHeader).isVisible();

        return this;
    }

    @Step("Нажать на выбор карт")
    public PolicyholderDetailsPage clickOnCardSelector() {
        selectCardInput.scrollIntoViewIfNeeded();

        assertThat(selectCardInput).isVisible();

        selectCardInput.click();

        return this;
    }

    @Step("Проверить отображение модалки")
    public PolicyholderDetailsPage bottomSheetCardShouldBeVisible() {
        assertThat(cardsBottomSheet).isVisible();

        return this;
    }

    @Step("Выбрать первую карту в модалке")
    public PolicyholderDetailsPage selectFirstPayoutCard() {
        assertThat(card).isVisible();

        card.click();

        return this;
    }

    @Step("Нажать продолжить")
    public ConfirmRegistrationPage clickContinueButton() {
        continueButton.scrollIntoViewIfNeeded();
        assertThat(continueButton).isVisible();
        continueButton.click();
        return new ConfirmRegistrationPage();
    }
}
