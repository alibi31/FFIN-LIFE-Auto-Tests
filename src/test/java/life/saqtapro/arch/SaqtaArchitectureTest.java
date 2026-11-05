package life.saqtapro.arch;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Охраняет слои: тесты не делают HTTP-запросы и не трогают WireMock напрямую —
 * только через SaqtaApi и GovStubs. Иначе URL и стабы со временем расползутся по тестам.
 *
 * Написан как обычный Jupiter-тест (а не @ArchTest), потому что build.gradle.kts
 * запускает только движок junit-jupiter.
 */
@Tag("saqtapro-arch")
class SaqtaArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages("life.saqtapro");
    }

    @Test
    @DisplayName("Тесты SaqtaPro вызывают API только через life.saqtapro.api")
    void testsUseApiLayer() {
        noClasses().that().resideInAPackage("life.saqtapro.test..")
                .should().dependOnClassesThat().belongToAnyOf(RestAssured.class)
                .because("HTTP-вызовы SaqtaPro идут только через life.saqtapro.api")
                .check(classes);
    }

    @Test
    @DisplayName("Тесты SaqtaPro задают поведение гос. сервисов только через life.saqtapro.stubs")
    void testsUseStubLayer() {
        noClasses().that().resideInAPackage("life.saqtapro.test..")
                .should().dependOnClassesThat().belongToAnyOf(WireMock.class)
                .because("поведение гос. сервисов задаётся только через life.saqtapro.stubs")
                .check(classes);
    }
}
