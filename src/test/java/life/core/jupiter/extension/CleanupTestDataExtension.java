package life.core.jupiter.extension;

import life.core.jupiter.annotation.CleanupTestData;
import life.superapp.api.model.cancel.CancelPolicyResponse;
import life.superapp.api.service.CancelPolicyService;
import life.superapp.api.service.impl.CancelPolicyApiService;
import life.utils.PoliciesRegistry;
import life.utils.TestDataStorage;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Extension для очистки тестовых данных после выполнения всех тестов.
 * Вызывается через аннотацию @CleanupTestData
 */
public class CleanupTestDataExtension implements AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(CleanupTestDataExtension.class);
    private final CancelPolicyService cancelPolicyService = new CancelPolicyApiService();

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestClass(), CleanupTestData.class)
                .ifPresent(annotation -> {
                    log.info("🧹 Начинаем очистку тестовых данных после всех тестов в [{}]",
                            context.getDisplayName());

                    // 1. Отменяем все зарегистрированные полисы
                    if (annotation.cancelPolicies()) {
                        cancelAllPolicies(context);
                    }

                    // 2. Очищаем TestDataStorage
                    if (annotation.clearStorage()) {
                        clearTestDataStorage(context);
                    }

                    log.info("✅ Очистка тестовых данных завершена для [{}]", context.getDisplayName());
                });
    }

    /**
     * Отменяет все полисы из PoliciesRegistry
     */
    private void cancelAllPolicies(ExtensionContext context) {
        List<String> policies = PoliciesRegistry.drain();

        if (policies.isEmpty()) {
            log.info("📋 Нет полисов для отмены в [{}]", context.getDisplayName());
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
                log.error("   ❌ Ошибка при отмене полиса [{}]: {}", policyNumber, e.getMessage(), e);
                failCount++;
            }
        }

        log.info("📊 Результаты отмены полисов: успешно={}, ошибок={}", successCount, failCount);
    }

    /**
     * Очищает TestDataStorage
     */
    private void clearTestDataStorage(ExtensionContext context) {
        try {
            log.info("🗑️  Очищаем TestDataStorage...");
            TestDataStorage.printAll(); // Показываем что было
            TestDataStorage.clear();
            log.info("✅ TestDataStorage очищен");
        } catch (Exception e) {
            log.error("❌ Ошибка при очистке TestDataStorage: {}", e.getMessage(), e);
        }
    }
}