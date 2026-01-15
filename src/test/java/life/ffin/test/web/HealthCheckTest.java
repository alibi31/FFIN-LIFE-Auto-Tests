package life.ffin.test.web;

import life.core.jupiter.annotation.WebTest;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.Tag;


@WebTest()
@Tag("Ffin-UI")
public class HealthCheckTest {

    private final String ffinUrl = EnvConfig.cfg().ffinUrl();




}
