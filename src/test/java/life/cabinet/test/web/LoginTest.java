package life.cabinet.test.web;

import life.cabinet.page.HomePage;
import life.cabinet.page.LoginPage;
import life.core.jupiter.annotation.WebTest;
import life.core.web.Pages;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Cabinet-UI")
@WebTest(width = 375, height = 812, isMobile = true)
public class LoginTest {


    private final String authUrl = EnvConfig.cfg().cabinetUrl();
    private final String iin = "010131500057";
    private final String password = "alibi2001";

    @Test
    void homePageShouldBeDisplayedAfterSuccessLogin() {
        Pages.open(authUrl, LoginPage.class)
                .checkThatPageLoaded()
                .acknowledge()
                .setUsername(EnvConfig.cfg().username())
                .setPassword(EnvConfig.cfg().password())
                .submit(HomePage.class)
                .checkThatPageLoaded();
    }

    @Test
    void userShouldStayOnLoginPageAfterLoginWithBadCredentials() {
        Pages.open(authUrl, LoginPage.class)
                .checkThatPageLoaded()
                .acknowledge()
                .fillUsernameAndPassword(EnvConfig.cfg().username(), "1234567")
                .submit(LoginPage.class)
                .checkError("Неверный пароль");
    }





}
