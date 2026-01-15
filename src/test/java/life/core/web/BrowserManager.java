package life.core.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import life.utils.config.EnvConfig;


public class BrowserManager {
    public static Browser getBrowser(final Playwright playwright) {
        String configuredBrowser = EnvConfig.cfg().browser();
        BrowserFactory browserFactory = BrowserFactory.from(configuredBrowser);
        return browserFactory.createInstance(playwright);
    }
}
