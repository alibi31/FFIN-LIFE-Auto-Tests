package life.saqtapro.config;

import org.aeonbits.owner.ConfigCache;

/** Точка доступа к конфигурации SaqtaPro, по аналогии с life.utils.config.EnvConfig */
public final class Saqta {

    private static final SaqtaConfig CFG = ConfigCache.getOrCreate(SaqtaConfig.class);

    private Saqta() {}

    public static SaqtaConfig cfg() {
        return CFG;
    }

    /**
     * Секреты (логины/пароли тестовых пользователей) — только из -D или переменных окружения CI,
     * никогда из properties-файлов в git.
     */
    public static String secret(String key) {
        String value = System.getProperty(key, System.getenv(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задан секрет " + key
                    + ": передайте его через переменную окружения или -D" + key + "=...");
        }
        return value;
    }
}
