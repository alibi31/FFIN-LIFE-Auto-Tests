package life.saqtapro.test.web;

import io.qameta.allure.Feature;
import life.core.web.Pages;
import life.saqtapro.auth.TestUser;
import life.saqtapro.config.Saqta;
import life.saqtapro.jupiter.SaqtaWebTest;
import life.saqtapro.page.SaqtaLoginPage;
import life.saqtapro.page.ShellPage;
import life.saqtapro.page.ShellPage.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** UI-часть матрицы RBAC: какие разделы видит каждая роль */
@SaqtaWebTest
@Tag("rbac")
@Feature("RBAC")
class RoleNavigationWebTest {

    static Stream<Arguments> visibleSections() {
        return Stream.of(
                Arguments.of(TestUser.AGENT_A, EnumSet.of(Section.POLICIES, Section.CLIENTS, Section.REPORTS)),
                Arguments.of(TestUser.MANAGER_ALMATY, EnumSet.allOf(Section.class)),
                Arguments.of(TestUser.UNDERWRITER, EnumSet.of(Section.POLICIES, Section.UNDERWRITING))
        );
    }

    @ParameterizedTest(name = "{0} видит {1}")
    @MethodSource("visibleSections")
    @DisplayName("Меню разделов по ролям")
    void sectionsByRole(TestUser user, Set<Section> visible) {
        ShellPage shell = login(user);
        for (Section section : Section.values()) {
            if (visible.contains(section)) {
                shell.assertSectionVisible(section);
            } else {
                shell.assertSectionAbsent(section);
            }
        }
    }

    @Test
    @DisplayName("Менеджер открывает вкладку андеррайтинга только на чтение")
    void managerSeesUnderwritingReadOnly() {
        login(TestUser.MANAGER_ALMATY).openUnderwriting().assertReadOnly();
    }

    @Test
    @DisplayName("Агент не попадает на вкладку андеррайтинга по прямой ссылке")
    void agentDirectUrlToUnderwritingIsForbidden() {
        login(TestUser.AGENT_A);
        var page = Pages.open(Saqta.cfg().webUrl() + "/underwriting");
        assertThat(page.getByTestId("error-403")).isVisible();
        assertThat(page.getByTestId("uw-queue")).hasCount(0);
    }

    private static ShellPage login(TestUser user) {
        return Pages.open(Saqta.cfg().webUrl() + "/login", SaqtaLoginPage.class)
                .checkThatPageLoaded()
                .loginAs(user);
    }
}
