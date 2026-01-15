package life.cabinet.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import life.core.web.BasePage;
import life.core.web.BasePageFactory;

import javax.annotation.Nonnull;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage<LoginPage> {



    private Locator usernameInput;
    private Locator passwordInput;
    private Locator acknowledgeButton;
    private Locator submitButton;
    private Locator modalWindow;
    private Locator errorContainer;
    private Locator errorText;
    private Locator registrationButton;
    private Locator phoneNumberInput;

    @Override
    public void initComponents() {
        usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("ИИН ИИН"));
        passwordInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Пароль Пароль"));
        acknowledgeButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ознакомлен"));
        submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Войти"));
        modalWindow = page.locator(".confirmation-dialog__body");
        errorContainer = page.locator(".v-card");
        errorText = page.getByRole(AriaRole.DIALOG);
        registrationButton = page.locator("button.login-button:has-text('Регистрация')");
        phoneNumberInput = page.getByLabel("Телефон");
    }

    @Step("Ввести ИИН: {0}")
    @Nonnull
    public LoginPage setUsername(String username) {
        usernameInput.fill(username);
        return this;
    }

    @Step("Кликнуть на 'Регистрация'")
    @Nonnull
    public LoginPage clickRegistrationButton() {
        registrationButton.click();
        return this;
    }

    @Step("Ввести номер телефона")
    @Nonnull
    public LoginPage setPhoneNumber(String phone) {
        phoneNumberInput.fill(phone);
        return this;
    }

    @Step("Ввести пароль")
    @Nonnull
    public LoginPage setPassword(String password) {
        passwordInput.fill(password);
        return this;
    }

    @Step("Заполнить логин и пароль")
    @Nonnull
    public LoginPage fillUsernameAndPassword(String username, String password) {
        usernameInput.fill(username);
        passwordInput.fill(password);
        return this;
    }

    @Step("Кликнуть на кнопку 'Ознакомлен'")
    @Nonnull
    public LoginPage acknowledge() {
        acknowledgeButton.click();
        return this;
    }

    @Step("Кликнуть на кнопку 'Войти'")
    @Nonnull
    public <T extends BasePage<?>> T submit(Class<T> expectedPageClass) {
        submitButton.click();
        return BasePageFactory.createInstance(page, expectedPageClass);

    }


    @Step("Проверить что страница загрузилась")
    @Override
    @Nonnull
    public LoginPage checkThatPageLoaded() {
        assertThat(modalWindow).isVisible();
        return this;
    }

    @Step("Проверить что вышла ошибка: {0}")
    @Nonnull
    public LoginPage checkError(String expectedText) {
        assertThat(errorContainer).isVisible();
        assertThat(errorText).containsText(expectedText);

        return this;
    }
}
