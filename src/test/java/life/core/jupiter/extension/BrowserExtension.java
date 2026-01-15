package life.core.jupiter.extension;

import com.microsoft.playwright.*;
import io.qameta.allure.Attachment;
import life.core.jupiter.annotation.WebTest;
import life.core.web.BrowserManager;
import life.core.web.UiSession;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class BrowserExtension implements BeforeEachCallback,
        AfterEachCallback,
        TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {

//    private Playwright playwright;
//    private Browser browser;

    private static final ThreadLocal<Playwright> TL_PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> TL_BROWSER = new ThreadLocal<>();

    private final ThreadLocal<Boolean> needVideoThreadLocal = ThreadLocal.withInitial(() -> false);



//    @Override
//    public void beforeAll(ExtensionContext extensionContext) {
//        playwright = Playwright.create();
//        browser = BrowserManager.getBrowser(playwright);
//    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {

        Playwright playwright = TL_PLAYWRIGHT.get();
        if (playwright == null) {
            playwright = Playwright.create();
            TL_PLAYWRIGHT.set(playwright);
        }

        Browser browser = TL_BROWSER.get();
        if (browser == null) {
            browser = BrowserManager.getBrowser(playwright);
            TL_BROWSER.set(browser);
        }

        Browser.NewContextOptions options = new Browser.NewContextOptions();

        AnnotationSupport.findAnnotation(extensionContext.getRequiredTestClass(), WebTest.class)
                .ifPresent(ann -> {
                    if (ann.width() > 0 && ann.height() > 0) {
                        options.setViewportSize(ann.width(), ann.height());
                        if (ann.isMobile()) {
                            options
                                    .setIsMobile(true)
                                    .setHasTouch(true)
                                    .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) " +
                                            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1");
                        }
                    }
                });


        if (EnvConfig.cfg().video()) {
            Path dir = Paths.get(EnvConfig.cfg().baseTestVideoPath());
            try {
                Files.createDirectories(dir);
            } catch (Exception ignored) { }
            options.setRecordVideoDir(dir);
            options.setRecordVideoSize(1280, 720);
        }
        BrowserContext browserContext = browser.newContext(options);

        browserContext.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        UiSession.set(browser, browserContext);

        needVideoThreadLocal.set(false);
    }

//    @Override
//    public void afterAll(ExtensionContext extensionContext) {
//        if (browser != null) {
//            browser.close();
//            browser = null;
//        }
//        if (playwright != null) {
//            playwright.close();
//            playwright = null;
//        }
//    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        BrowserContext browserContext = UiSession.context();
        if (browserContext == null) return;

        Page page = UiSession.page();
        boolean failed = extensionContext.getExecutionException().isPresent();

        Path trace = null;
        Path videoPath = null;

        try {
            // Скриншот только если тест падает
            if (failed && page != null) {
                attachPng(page.screenshot(new Page.ScreenshotOptions().setFullPage(true)));
            }

//            trace = Paths.get("build", "trace-" + sanitize(extensionContext.getDisplayName()) + ".zip");
//            Files.createDirectories(trace.getParent());
//            browserContext.tracing().stop(new Tracing.StopOptions().setPath(trace));
//            attachFile("Trace", Files.readAllBytes(trace), "application/zip", "zip");

            if (page != null) {
                try {
                    videoPath = page.video().path();
                } catch (Exception ignored) { }
            }

            browserContext.close();
        } finally {
            browserContext.close();

            // Приложить видео если тест упал (или был флаг из handlerа)
            boolean shouldAttachVideo = EnvConfig.cfg().video() && (failed || needVideoThreadLocal.get());
            if (shouldAttachVideo && videoPath != null && Files.exists(videoPath)) {
                attachVideo(Files.readAllBytes(videoPath));
            }

            UiSession.clear();
            needVideoThreadLocal.remove();
        }

    }



    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        var page = UiSession.page();
        if (page != null) {
            attachPng(page.screenshot());

        }
        needVideoThreadLocal.set(true);
        throw throwable;
    }

    @Attachment(value = "{name}", type = "{mime}")
    private byte[] attachFile(String name, byte[] bytes, String mime, String ext) {
        return bytes;
    }

    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] attachPng(byte[] bytes) {
        return bytes;
    }

    @Attachment(value = "Test Video", type = "video/webm")
    private byte[] attachVideo(byte[] bytes) {
        return bytes;
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9-_]", "_");
    }


}
