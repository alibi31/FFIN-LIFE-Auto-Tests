package life.saqtapro.page;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import life.core.web.BasePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Вкладка андеррайтера: очередь договоров и карточка с решением */
public class UnderwritingPage extends BasePage<UnderwritingPage> {

    private Locator queue;
    private Locator approve;
    private Locator reject;
    private Locator comment;
    private Locator confirm;
    private Locator decisionToast;

    @Override
    public void initComponents() {
        queue = page.getByTestId("uw-queue");
        approve = page.getByTestId("uw-approve");
        reject = page.getByTestId("uw-reject");
        comment = page.getByTestId("uw-comment");
        confirm = page.getByTestId("uw-confirm");
        decisionToast = page.getByTestId("uw-decision-toast");
    }

    @Override
    public UnderwritingPage checkThatPageLoaded() {
        assertThat(queue).isVisible();
        return this;
    }

    private Locator row(String policyId) {
        return queue.locator("[data-policy-id='" + policyId + "']");
    }

    @Step("Договор {policyId} есть в очереди")
    public UnderwritingPage assertInQueue(String policyId) {
        assertThat(row(policyId)).isVisible();
        return this;
    }

    @Step("Открыть договор {policyId}")
    public UnderwritingPage open(String policyId) {
        row(policyId).click();
        return this;
    }

    @Step("Одобрить договор с комментарием")
    public UnderwritingPage approve(String text) {
        approve.click();
        comment.fill(text);
        confirm.click();
        assertThat(decisionToast).isVisible();
        return this;
    }

    @Step("Отклонить договор с причиной")
    public UnderwritingPage reject(String reason) {
        reject.click();
        comment.fill(reason);
        confirm.click();
        assertThat(decisionToast).isVisible();
        return this;
    }

    /** Режим «только чтение» (менеджер): кнопки решения отсутствуют в DOM */
    @Step("Кнопок решения нет")
    public UnderwritingPage assertReadOnly() {
        assertThat(approve).hasCount(0);
        assertThat(reject).hasCount(0);
        return this;
    }
}
