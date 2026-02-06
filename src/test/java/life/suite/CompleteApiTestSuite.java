package life.suite;

import org.junit.platform.suite.api.*;
import life.ffin.test.api.UrbanRegistrationTest;
import life.cabinet.test.api.*;

/**
 * Полный набор API тестов.
 *
 * Очистка тестовых данных происходит автоматически через
 * CleanupAfterAllTestsListener (зарегистрирован через META-INF/services)
 */
@Suite
@SuiteDisplayName("Complete API Test Suite")
@SelectClasses({
        // 1. Сначала регистрируем полис (FFIN API)
        UrbanRegistrationTest.class,

        // 2. Потом тестируем Cabinet API
        CabinetAuthorizationTest.class,
        GetContractTest.class,
        ContractsFilterTest.class,
        GetDocumentsTest.class,
        GetCardsTest.class,
        GetRequisitesTest.class,
        SubscriptionManagementTest.class,
        EducationRegistrationTest.class,
        JurekRegistrationTest.class,
        HealthRegistrationTest.class,
        FirstRegistrationTest.class
})
public class CompleteApiTestSuite {
    // Очистка происходит автоматически через CleanupAfterAllTestsListener
    // после завершения ВСЕХ тестов из Suite
}