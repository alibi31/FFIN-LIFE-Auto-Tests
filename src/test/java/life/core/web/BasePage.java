package life.core.web;

import com.microsoft.playwright.Page;
import life.utils.config.EnvConfig;



public abstract class BasePage<T extends BasePage<?>> {

    protected Page page;

    public abstract T checkThatPageLoaded();

    public void setAndConfigurePage(final Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Playwright page instance must not be null");
        }
        this.page = page;
        page.setDefaultNavigationTimeout(EnvConfig.cfg().navigationTimeout());
        page.setDefaultTimeout(EnvConfig.cfg().timeout());
    }

    public abstract void initComponents();

    protected Page page() {
        return page;
    }
}
