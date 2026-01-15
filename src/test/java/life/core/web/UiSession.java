package life.core.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

public final class UiSession {
    private UiSession() {}
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> BROWSER_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();

    public static void set(Browser browser, BrowserContext ctx) {
        BROWSER.set(browser);
        BROWSER_CONTEXT.set(ctx);
    }

    public static Browser browser() {
        return BROWSER.get();
    }

    public static BrowserContext context() {
        return BROWSER_CONTEXT.get();
    }

    public static void setAccessToken(String accessToken) {
        ACCESS_TOKEN.set(accessToken);
    }

    public static String getAccessToken() {
        return ACCESS_TOKEN.get();
    }

    public static void setPage(Page page) {
        PAGE.set(page);
    }

    public static Page page() {
        return PAGE.get();
    }

    public static void clear() {
        PAGE.remove();
        BROWSER.remove();
        BROWSER_CONTEXT.remove();
        ACCESS_TOKEN.remove();
    }
}
