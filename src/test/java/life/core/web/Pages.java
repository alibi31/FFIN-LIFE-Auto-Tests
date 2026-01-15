package life.core.web;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;


public final class Pages {
    private Pages() {}

    public static <T extends BasePage<?>> T open(String url, Class<T> pageClass) {
        BrowserContext context = ensureContext();
        Page page = context.newPage();
        UiSession.setPage(page);
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return BasePageFactory.createInstance(page, pageClass);
    }

    public static <T extends BasePage<?>> T on(Class<T> pageClass) {
        Page page = UiSession.page();
        if (page == null) {
            throw new IllegalStateException("No page in session. Use Pages.open(...) first.");
        }
        return BasePageFactory.createInstance(page, pageClass);
    }

    public static Page open(String url) {
        BrowserContext context = ensureContext();
        Page page = context.newPage();
        UiSession.setPage(page);
        page.navigate(url);
        return page;
    }

    private static BrowserContext ensureContext() {
        BrowserContext context = UiSession.context();
        if(context == null) {
            throw new IllegalStateException("Browser context is not initialised. Ensure @WebTest extension configured the session before calling Pages helper methods.");
        }
        return context;
    }
}
