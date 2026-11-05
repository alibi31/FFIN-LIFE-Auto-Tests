package life.saqtapro.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import io.qameta.allure.Step;
import life.core.web.BasePage;
import life.saqtapro.data.KzIban;
import life.saqtapro.stubs.EgovStub;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Форма клиента: ИИН + телефон → «Получить данные» → автозаполнение из eGov */
public class ClientFormPage extends BasePage<ClientFormPage> {

    private static final double GOV_WAIT_MS = 30_000;

    private Locator iin;
    private Locator phone;
    private Locator fetchData;
    private Locator verificationStatus;
    private Locator fieldError;
    private Locator fullName;
    private Locator birthDate;
    private Locator address;
    private Locator iban;
    private Locator relatives;

    @Override
    public void initComponents() {
        iin = page.getByTestId("client-iin");
        phone = page.getByTestId("client-phone");
        fetchData = page.getByTestId("client-fetch-gov-data");
        verificationStatus = page.getByTestId("gov-verification-status");
        fieldError = page.getByTestId("client-iin-error");
        fullName = page.getByTestId("client-full-name");
        birthDate = page.getByTestId("client-birth-date");
        address = page.getByTestId("client-address");
        iban = page.getByTestId("client-iban");
        relatives = page.getByTestId("client-relative-row");
    }

    @Override
    public ClientFormPage checkThatPageLoaded() {
        assertThat(iin).isVisible();
        return this;
    }

    @Step("Ввести ИИН и телефон, запросить данные из гос. баз")
    public ClientFormPage requestGovData(String iinValue, String phoneValue) {
        iin.fill(iinValue);
        phone.fill(phoneValue);
        fetchData.click();
        return this;
    }

    @Step("Статус верификации: «{text}»")
    public ClientFormPage assertVerificationStatus(String text) {
        assertThat(verificationStatus).containsText(text,
                new LocatorAssertions.ContainsTextOptions().setTimeout(GOV_WAIT_MS));
        return this;
    }

    @Step("Ошибка валидации ИИН: «{text}»")
    public ClientFormPage assertIinError(String text) {
        assertThat(fieldError).containsText(text);
        return this;
    }

    @Step("Поля заполнены данными eGov и недоступны для редактирования")
    public ClientFormPage assertAutofilledFrom(EgovStub.EgovPerson egov, boolean ibanMasked) {
        assertThat(fullName).hasValue(egov.person().fullName(),
                new LocatorAssertions.HasValueOptions().setTimeout(GOV_WAIT_MS));
        assertThat(birthDate).hasValue(egov.person().birthDateStr());
        assertThat(address).hasValue(egov.person().address());
        assertThat(iban).hasValue(ibanMasked ? KzIban.masked(egov.iban()) : egov.iban());
        assertThat(fullName).not().isEditable();
        assertThat(iban).not().isEditable();
        return this;
    }

    @Step("Родственные связи скрыты")
    public ClientFormPage assertRelativesHidden() {
        assertThat(relatives).hasCount(0);
        return this;
    }

    @Step("Поля не заполнены")
    public ClientFormPage assertNotAutofilled() {
        assertThat(fullName).isEmpty();
        assertThat(iban).isEmpty();
        return this;
    }
}
