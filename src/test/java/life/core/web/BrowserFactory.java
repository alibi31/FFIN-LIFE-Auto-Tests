package life.core.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import life.utils.config.EnvConfig;

import java.util.Arrays;
import java.util.List;



public enum BrowserFactory {

    CHROMIUM {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.chromium().launch(options());
        }
    },

    FIREFOX {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.firefox().launch(options());
        }
    },

    WEBKIT {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.webkit().launch(options());
        }
    };

    public BrowserType.LaunchOptions options() {
        return new BrowserType.LaunchOptions()
                .setHeadless(resolveHeadless())
                .setArgs(List.of("--ignore-certificate-errors"))
                .setSlowMo(EnvConfig.cfg().slowMotion());
    }

    public abstract Browser createInstance(Playwright playwright);

    public static BrowserFactory from(String browserName) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(browserName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown browser type: " + browserName));
    }

    private static boolean resolveHeadless() {
        String overridden = System.getProperty("headless");
        return overridden == null ? EnvConfig.cfg().headless() : Boolean.parseBoolean(overridden);
    }
}
