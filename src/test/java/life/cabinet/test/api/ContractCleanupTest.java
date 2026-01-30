package life.cabinet.test.api;

import io.qameta.allure.Owner;
import life.utils.PoliciesRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag("cabinet-api")
@Owner("QA")
public class ContractCleanupTest {
    private static final List<String> POLICY_NUMBERS = new ArrayList<>();


    public static void collectPolicyNumbers(List<String> numbers) {
        POLICY_NUMBERS.addAll(numbers);
    }

    @Test
    @DisplayName("Аннулирование тестовых договоров")
    public void cleanupAll() {
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Задержка была прервана", e);
        }

        for (String policyNumber : POLICY_NUMBERS) {
            PoliciesRegistry.register(policyNumber);
        }
        POLICY_NUMBERS.clear();
    }

}
