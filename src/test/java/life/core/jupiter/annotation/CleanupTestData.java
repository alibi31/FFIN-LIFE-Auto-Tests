package life.core.jupiter.annotation;

import life.core.jupiter.extension.CleanupTestDataExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для очистки тестовых данных после выполнения всех тестов.
 * Используется на уровне Suite или класса теста.
 *
 * Пример использования:
 * @CleanupTestData
 * public class CompleteApiTestSuite {
 *     // тесты
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE}) // Применяется к классу
@ExtendWith(CleanupTestDataExtension.class)
public @interface CleanupTestData {
    /**
     * Очищать ли TestDataStorage после всех тестов
     */
    boolean clearStorage() default true;

    /**
     * Отменять ли все зарегистрированные полисы после всех тестов
     */
    boolean cancelPolicies() default true;
}