package life.superapp.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfirmRegistrationPage extends BasePage<ConfirmRegistrationPage> {

    private Locator confirmationDataHeader;
    private Locator dividendAmount;
    private Locator annuityAmount;

    @Override
    public void initComponents() {
        this.confirmationDataHeader = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Данные страхователя")
                        .setLevel(1)
        );
        this.annuityAmount = page.locator(
                "div.infoList__item:has-text(\"Аннуитетная выплата\") .infoList__item-content"
        );
        this.dividendAmount = page.locator(
                "div.infoList__item:has-text(\"Страховой дивиденд\") .infoList__item-content"
        );
    }

    @Override
    public ConfirmRegistrationPage checkThatPageLoaded() {
        confirmationDataHeader.waitFor();
        assertThat(confirmationDataHeader).isVisible();
        return this;
    }

    @Step("Проверить, что аннуитетная выпалата отображается")
    public ConfirmRegistrationPage annuityAmountIsVisible() {
        assertThat(annuityAmount).isVisible();
        return this;
    }

    @Step("Проверить, что аннутитетная выпалата не пустая и != 0")
    public ConfirmRegistrationPage annuityAmountNotNull() {
        String amountText = annuityAmount.textContent().trim();
        long value = Long.parseLong(amountText.replaceAll("[^0-9]", ""));

        assertFalse(amountText.isEmpty(), "Сумма аннуитетной выплаты пустая");

        assertTrue(value > 0, "Сумма должна быть больше 0, а получили: " + value);
        return this;
    }

    @Step("Проверить, что страховой дивиденд отображается")
    public ConfirmRegistrationPage dividendAmountIsVisible() {
        assertThat(dividendAmount).isVisible();
        return this;
    }

    @Step("Проверить, что страховой дивиденд не пустая и != 0")
    public ConfirmRegistrationPage dividendAmountNotNull() {
        String amountText = dividendAmount.textContent().trim();
        long value = Long.parseLong(amountText.replaceAll("[^0-9]", ""));

        assertFalse(amountText.isEmpty(), "Сумма страховой дивиденд пустая");

        assertTrue(value > 0, "Сумма должна быть больше 0, а получили: " + value);
        return this;
    }

}
