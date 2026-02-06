package life.suite;

import org.junit.jupiter.api.AfterAll;
import org.junit.platform.suite.api.*;
import org.junit.platform.suite.api.Suite;
import life.cabinet.test.api.*;

import java.util.concurrent.TimeUnit;

@Suite
@SuiteDisplayName("Complete Cabinet API Test Suite")
@SelectPackages({
        "life.cabinet"
})
@IncludeTags("cabinet-api")
@SelectClasses({
        CabinetAuthorizationTest.class,
        GetContractTest.class,
        ContractsFilterTest.class,
        GetDocumentsTest.class,
        GetCardsTest.class,
        GetRequisitesTest.class,
        EducationRegistrationTest.class,
        JurekRegistrationTest.class,
        HealthRegistrationTest.class,
        FirstRegistrationTest.class
})
public class CabinetApiTestSuite {

}
