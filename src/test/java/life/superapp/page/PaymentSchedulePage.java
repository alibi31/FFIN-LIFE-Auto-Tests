package life.superapp.page;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentSchedulePage extends BasePage<PaymentSchedulePage> {

    private Locator header;
    private Locator paymentMonthItem;

    @Override
    public void initComponents() {
        this.header = page.locator(".appHeader__title");
        this.paymentMonthItem = page.locator(".periodPayment__info span").nth(1);
    }

    @Override
    public PaymentSchedulePage checkThatPageLoaded() {
        assertThat(header).isVisible();
        return this;
    }

    @Step("Проверить, что первый платеж виден и не пустой")
    public PaymentSchedulePage checkFirstPaymentMonth() {
        String amountText = paymentMonthItem.textContent().trim();
        String digits = amountText.replaceAll("[^0-9]", "");
        long value = Long.parseLong(digits);

        paymentMonthItem.waitFor();
        assertThat(paymentMonthItem).isVisible();

        assertFalse(digits.isEmpty(), "Сумма выплаты пустая");

        assertTrue(value > 0, "Сумма должна быть больше 0, а получили: " + value);


        return this;
    }

}
