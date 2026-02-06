package life.core.junit.listener;

import life.superapp.api.model.cancel.CancelPolicyResponse;
import life.superapp.api.service.CancelPolicyService;
import life.superapp.api.service.impl.CancelPolicyApiService;
import life.utils.PoliciesRegistry;
import life.utils.TestDataStorage;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Listener для очистки тестовых данных после завершения ВСЕХ тестов.
 * Регистрируется через META-INF/services или в build.gradle
 */
public class CleanupAfterAllTestsListener implements TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CleanupAfterAllTestsListener.class);
    private final CancelPolicyService cancelPolicyService = new CancelPolicyApiService();

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("🧹 ========================================");
        log.info("🧹 Начинаем глобальную очистку после ВСЕХ тестов");
        log.info("🧹 ========================================");

        // 1. Отменяем все полисы из PoliciesRegistry
        cancelAllPolicies();

        // 2. Очищаем TestDataStorage
        clearTestDataStorage();

        log.info("✅ Глобальная очистка завершена!");
    }

    private void cancelAllPolicies() {
        // Сначала показываем что есть в реестре
        PoliciesRegistry.debug();

        List<String> policies = PoliciesRegistry.drain();

        if (policies.isEmpty()) {
            log.info("📋 Нет полисов для отмены");
            return;
        }

        log.info("🔄 Найдено {} полисов для отмены", policies.size());

        int successCount = 0;
        int failCount = 0;

        for (String policyNumber : policies) {
            try {
                log.info("   ⏳ Отменяем полис [{}]...", policyNumber);

                CancelPolicyResponse response = cancelPolicyService.cancelPolicy(policyNumber, "test_cleanup");

                if (response.isSuccess()) {
                    log.info("   ✅ Полис [{}] успешно отменён", policyNumber);
                    successCount++;
                } else {
                    log.error("   ❌ Не удалось отменить полис [{}]. Причина: {}",
                            policyNumber, response.getMessage());
                    failCount++;
                }

            } catch (IOException e) {
                log.error("   ❌ Ошибка при отмене полиса [{}]: {}", policyNumber, e.getMessage());
                failCount++;
            }
        }

        log.info("📊 Результаты отмены полисов: успешно={}, ошибок={}", successCount, failCount);
    }

    private void clearTestDataStorage() {
        try {
            log.info("🗑️  Очищаем TestDataStorage...");
            TestDataStorage.printAll();
            TestDataStorage.clear();
            log.info("✅ TestDataStorage очищен");
        } catch (Exception e) {
            log.error("❌ Ошибка при очистке TestDataStorage: {}", e.getMessage(), e);
        }
    }
}