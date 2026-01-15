package life.superapp.test.web;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Route;
import life.core.jupiter.annotation.WebTest;
import life.core.web.BrowserFactory;
import life.core.web.Pages;
import life.core.web.UiSession;
import life.superapp.jupiter.annotation.AccessToken;
import life.superapp.jupiter.annotation.Auth;
import life.superapp.page.CalculationPage;
import life.superapp.page.ConfirmRegistrationPage;
import life.superapp.page.PolicyholderDetailsPage;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@WebTest(width = 375, height = 812, isMobile = true)
@Auth(iin = "", fullName = "")
@Tag("PA-UI")
public class RegistrationWebTest {

    private final String regUrlPa = EnvConfig.cfg().superAppRegUrl();

    @Test
    void annuityShouldBeVisibleOnConfirmationPage(@AccessToken String token) {

        BrowserContext context = UiSession.context();
        if(context == null) {
            throw new IllegalStateException("BrowserContext is null. Ensure @WebTest is added");
        }

        context.route("**/api/pension-annuity/registration", route -> {
            route.fulfill(
                    new Route.FulfillOptions()
                            .setStatus(200)
                            .setContentType("application/json")
                            .setBody("""
                                    {
                                        "status": true,
                                        "message": "Успешно",
                                        "data": {
                                            "policy_number": "ON01003859",
                                            "start_date": "2025-11-26",
                                            "start_age": 55,
                                            "documents": {
                                                "contract": "https://dev-api.s-app-life.k8s.ff.in/api/default/storage?t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJcdTA0MWZcdTA0MzVcdTA0M2RcdTA0NDFcdTA0MzhcdTA0M2VcdTA0M2RcdTA0M2RcdTA0NGJcdTA0MzkgXHUwNDMwXHUwNDNkXHUwNDNkXHUwNDQzXHUwNDM4XHUwNDQyXHUwNDM1XHUwNDQyLzcwMDUxMTQwMjQ5My9PTjAxMDAzODU5L1x1MDQxNFx1MDQzZVx1MDQzM1x1MDQzZVx1MDQzMlx1MDQzZVx1MDQ0MC5wZGYiLCJleHAiOjE3NjQxNTMwMzQuNjEwNDE0fQ.ZD7j9XWA41OOmRNn71QYFnJRoGPO4vXdlpHLjGt9Nbw",
                                                "statement": "https://dev-api.s-app-life.k8s.ff.in/api/default/storage?t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJcdTA0MWZcdTA0MzVcdTA0M2RcdTA0NDFcdTA0MzhcdTA0M2VcdTA0M2RcdTA0M2RcdTA0NGJcdTA0MzkgXHUwNDMwXHUwNDNkXHUwNDNkXHUwNDQzXHUwNDM4XHUwNDQyXHUwNDM1XHUwNDQyLzcwMDUxMTQwMjQ5My9PTjAxMDAzODU5L1x1MDQxN1x1MDQzMFx1MDQ0Zlx1MDQzMlx1MDQzYlx1MDQzNVx1MDQzZFx1MDQzOFx1MDQzNS5wZGYiLCJleHAiOjE3NjQxNTMwMzQuNjEwNjIyfQ.HAWn8MVtC0zfHQCYqDi-gdg0qcMYhVpnFpy34O0Plk8",
                                                "frhc_consent": "https://dev-api.s-app-life.k8s.ff.in/api/default/storage?t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJcdTA0MWZcdTA0MzVcdTA0M2RcdTA0NDFcdTA0MzhcdTA0M2VcdTA0M2RcdTA0M2RcdTA0NGJcdTA0MzkgXHUwNDMwXHUwNDNkXHUwNDNkXHUwNDQzXHUwNDM4XHUwNDQyXHUwNDM1XHUwNDQyLzcwMDUxMTQwMjQ5My9cdTA0MjFcdTA0M2VcdTA0MzNcdTA0M2JcdTA0MzBcdTA0NDFcdTA0MzhcdTA0MzUucGRmIiwiZXhwIjoxNzY0MTUzMDM0LjYxMDY2OX0.9VjAJF1Vmixfq1PrCxf8C_qtr0HpajBRNvarkWwn-SM",
                                                "data_processing_agreement": "https://dev-api.s-app-life.k8s.ff.in/api/default/storage?t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJcdTA0MWZcdTA0MzVcdTA0M2RcdTA0NDFcdTA0MzhcdTA0M2VcdTA0M2RcdTA0M2RcdTA0NGJcdTA0MzkgXHUwNDMwXHUwNDNkXHUwNDNkXHUwNDQzXHUwNDM4XHUwNDQyXHUwNDM1XHUwNDQyLzcwMDUxMTQwMjQ5My9PTjAxMDAzODU5L1x1MDQyMVx1MDQzZVx1MDQzM1x1MDQzYlx1MDQzMFx1MDQ0MVx1MDQzOFx1MDQzNSBcdTA0M2RcdTA0MzAgXHUwNDNlXHUwNDMxXHUwNDQwXHUwNDMwXHUwNDMxXHUwNDNlXHUwNDQyXHUwNDNhXHUwNDQzIFx1MDQzZlx1MDQzNVx1MDQ0MFx1MDQ0MVx1MDQzZVx1MDQzZFx1MDQzMFx1MDQzYlx1MDQ0Y1x1MDQzZFx1MDQ0Ylx1MDQ0NSBcdTA0MzRcdTA0MzBcdTA0M2RcdTA0M2RcdTA0NGJcdTA0NDUucGRmIiwiZXhwIjoxNzY0MTUzMDM0LjYxMDcxMX0.u9dLbPWz6bhH6Od36Qo-tPYe3Nf_o85c-WjyvtSssVE",
                                                "app_deduction": "https://dev-api.s-app-life.k8s.ff.in/api/default/storage?t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJcdTA0MWZcdTA0MzVcdTA0M2RcdTA0NDFcdTA0MzhcdTA0M2VcdTA0M2RcdTA0M2RcdTA0NGJcdTA0MzkgXHUwNDMwXHUwNDNkXHUwNDNkXHUwNDQzXHUwNDM4XHUwNDQyXHUwNDM1XHUwNDQyLzcwMDUxMTQwMjQ5My9PTjAxMDAzODU5L1x1MDQxN1x1MDQzMFx1MDQ0Zlx1MDQzMlx1MDQzYlx1MDQzNVx1MDQzZFx1MDQzOFx1MDQzNSBcdTA0M2RcdTA0MzAgXHUwNDMyXHUwNDRiXHUwNDQ3XHUwNDM1XHUwNDQyLnBkZiIsImV4cCI6MTc2NDE1MzAzNC42MTA3NDh9.k5uIxlqV2hjpL-6NlvqqLLQbUKZgyh5HW8hdXwyDBSs"
                                            }
                                        }
                                    }
                            """)

            );
        });

        Pages.open(regUrlPa, CalculationPage.class)
                .checkThatPageLoaded()
                .inputAmountOfOPV("1000000000")
                .assertAnnuityAmountVisible()
                .clickContinueButton();


        Pages.on(PolicyholderDetailsPage.class)
                .checkThatPageLoaded()
                .clickOnCardSelector()
                .bottomSheetCardShouldBeVisible()
                .selectFirstPayoutCard()
                .clickContinueButton();

        Pages.on(ConfirmRegistrationPage.class)
                .checkThatPageLoaded()
                .dividendAmountNotNull()
                .dividendAmountIsVisible()
                .annuityAmountIsVisible()
                .annuityAmountNotNull();

    }
}
