package life.superapp.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import life.core.web.BasePage;
import com.microsoft.playwright.Locator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CalculationPage extends BasePage<CalculationPage> {

    private Locator header;
    private Locator inputOPV;
    private Locator insurancePremiumAmount;
    private Locator dividendAmount;
    private Locator annuityAmount;
    private Locator inputDPV;
    private Locator inputOwnExpenses;
    private Locator errorModalTitle;
    private Locator errorModalContent;
    private Locator paymentSchedule;
    private Locator continueButton;


    @Override
    public void initComponents() {
        this.header = page.locator(".appHeader__title");
        this.inputOPV = page.locator("#input-v-2");
        this.insurancePremiumAmount =
                page.locator(".calcTotal__item:has(.calcTotal__title:has-text(\"Страховая премия\")) .calcTotal__amount\n ");
        this.dividendAmount = page.locator(".calcTotal__item")
                .filter(new Locator.FilterOptions().setHasText("Страховой дивиденд"))
                .locator(".calcTotal__amount");
        this.annuityAmount = page.locator(".calcTotal__item")
                .filter(new Locator.FilterOptions().setHasText("Аннуитетная выплата"))
                .locator(".calcTotal__amount");
        this.inputDPV = page.locator("#input-v-5");
        this.inputOwnExpenses = page.locator("#input-v-8");
        this.errorModalTitle = page.locator(".customModal__title");
        this.errorModalContent = page.locator(".customModal__content");
        this.paymentSchedule = page.locator("div.detailsListItem:has-text(\"График выплат\")");
        this.continueButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Продолжить")
        );
    }

    @Override
    public CalculationPage checkThatPageLoaded() {
        header.waitFor();
        assertThat(header).isVisible();
        return this;
    }

    @Step("Ввести сумму ОПВ")
    public CalculationPage inputAmountOfOPV(String amount) {
        inputOPV.fill(amount);
        return this;
    }

    @Step("Ввести сумму ДПВ")
    public CalculationPage inputAmountOfDPV(String amount) {
        inputDPV.fill(amount);
        return this;
    }

    @Step("Ввести сумму собственных средств")
    public CalculationPage inputAmountOfOwnExpenses(String amount) {
        inputOwnExpenses.fill(amount);
        return this;
    }


    @Step("Проверить, что страховая премия отображается и не пустая")
    public CalculationPage assertInsurancePremiumVisible() {
        insurancePremiumAmount.scrollIntoViewIfNeeded();

        assertThat(insurancePremiumAmount).isVisible();

        String text = insurancePremiumAmount.innerText().trim();
        assertFalse(text.isBlank(), "Сумма страховой премии не должна быть пустой");
        return this;
    }

    @Step("Проверить, что сумма страхового дивиденда отображается и не пустая")
    public CalculationPage assertDividendAmountVisible() {
        dividendAmount.scrollIntoViewIfNeeded();

        assertThat(dividendAmount).isVisible();

        String text = dividendAmount.innerText().trim();
        assertFalse(text.isBlank(), "Сумма страхового дивиденда не должна быть пустой");
        return this;
    }

    @Step("Проверить, что аннуитетная выплата отображается и не пустая")
    public CalculationPage assertAnnuityAmountVisible() {
        annuityAmount.scrollIntoViewIfNeeded();

        assertThat(annuityAmount).isVisible();

        String text = annuityAmount.innerText().trim();
        assertFalse(text.isBlank(), "Аннуитетная выплата не должна быть пустой");
        return this;
    }

    @Step("Проверить, что кнопка график платежей отображается")
    public CalculationPage paymentScheduleIsVisible() {
        paymentSchedule.scrollIntoViewIfNeeded();
        assertThat(paymentSchedule).isVisible();

        return this;
    }

    @Step("Открыть график платежей")
    public PaymentSchedulePage openPaymentSchedulePage() {
        paymentSchedule.click();
        return new PaymentSchedulePage();
    }


    @Step("Проверить, что отображается заголовок с ошибкой")
    public boolean assertErrorTitleIsVisible(String title) {
        return errorModalTitle.isVisible();
    }

    @Step("Проверить, что отображается текст ошибки")
    public boolean assertErrorTextIsVisible(String content) {
        return errorModalContent.isVisible();
    }

    @Step("Кликунть на продолжить")
    public PolicyholderDetailsPage clickContinueButton() {
        continueButton.scrollIntoViewIfNeeded();
        assertThat(continueButton).isVisible();
        continueButton.click();
        return new PolicyholderDetailsPage();
    }


}
