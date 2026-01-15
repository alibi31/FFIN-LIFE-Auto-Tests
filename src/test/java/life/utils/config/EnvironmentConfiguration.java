package life.utils.config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:${env}.properties",
        "classpath:test.properties"
})
public interface EnvironmentConfiguration extends Config {

    @Key("ffin.url")
    String ffinUrl();

    @Key("api.ffin.url")
    String apiFfinUrl();

    @Key("base.test.video.path")
    String baseTestVideoPath();

    @Key("navigation.timeout")
    Double navigationTimeout();

    String browser();

    boolean headless();

    @Key("slow.motion")
    int slowMotion();

    int timeout();

    boolean video();

    @Key("cabinet.url")
    String cabinetUrl();

    @Key("api.superApp.url")
    String apiSuperAppUrl();

    @Key("cancel.policy.url")
    String cancelPolicyUrl();

    @Key("superApp.registration.url")
    String superAppRegUrl();

    String username();

    String password();

}
