package life.utils.config;

import org.aeonbits.owner.ConfigCache;

public class EnvConfig {
    private static final EnvironmentConfiguration CFG =
            ConfigCache.getOrCreate(EnvironmentConfiguration.class);

    public static EnvironmentConfiguration cfg() {
        return CFG;
    }
}
