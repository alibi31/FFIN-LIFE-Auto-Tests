package life.core.web;

import com.microsoft.playwright.Page;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class BasePageFactory {

    private static final Logger log = LoggerFactory.getLogger(BasePageFactory.class);

    public static <T extends BasePage<?>> T createInstance(final Page page, final Class<T> clazz) {
        if (page == null) {
            throw new IllegalStateException("Playwright page was not initialised. Call Pages.open(...) first or ensure the current session contains a page instance.");
        }
        try {
            BasePage<?> instance = clazz.getDeclaredConstructor().newInstance();

            instance.setAndConfigurePage(page);
            instance.initComponents();

            return clazz.cast(instance);
        } catch (Exception e) {
            log.error("Failed to instantiate page object {}", clazz.getName(), e);
            throw new IllegalStateException("Unable to instantiate page class " + clazz.getName(), e);
        }
    }

}
