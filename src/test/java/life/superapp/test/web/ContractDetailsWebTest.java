package life.superapp.test.web;

import life.core.jupiter.annotation.WebTest;
import life.superapp.jupiter.annotation.Auth;
import org.junit.jupiter.api.Tag;

@WebTest(width = 375, height = 812, isMobile = true)
@Auth(iin = "700511402493", fullName = "ЖАПБАРОВА ГУЛЬМИРА АБЫЛГАЗИНОВНА")
@Tag("PA-UI")
public class ContractDetailsWebTest {


}
