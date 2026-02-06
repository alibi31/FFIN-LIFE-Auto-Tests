package life.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoliciesRegistry {
    // Глобальный список для ВСЕХ тестов
    private static final List<String> POLICIES = Collections.synchronizedList(new ArrayList<>());

    // Для отладки - счетчик добавлений
    private static int addCounter = 0;

    public static synchronized void register(String policyNumber) {
        System.err.println("🔵 PoliciesRegistry.register() вызван");
        System.err.println("   Добавляем полис: " + policyNumber);
        System.err.println("   До добавления: " + POLICIES.size() + " полисов");

        POLICIES.add(policyNumber);
        addCounter++;

        System.err.println("   После добавления: " + POLICIES.size() + " полисов");
        System.err.println("   Всего вызовов register: " + addCounter);
        System.err.println("   Все полисы: " + POLICIES);
    }

    public static synchronized List<String> drain() {
        System.err.println("🟡 PoliciesRegistry.drain() вызван");
        System.err.println("   Сейчас в реестре: " + POLICIES.size() + " полисов");

        List<String> result = new ArrayList<>(POLICIES);
        POLICIES.clear();

        System.err.println("   Возвращаем для очистки: " + result.size() + " полисов");
        System.err.println("   Список: " + String.join(", ", result));
        System.err.println("   После очистки осталось: " + POLICIES.size());

        return result;
    }

    // Метод для отладки
    public static synchronized void debug() {
        System.err.println("=".repeat(50));
        System.err.println("DEBUG PoliciesRegistry:");
        System.err.println("  Number of policies: " + POLICIES.size());
        System.err.println("  List of policies: " + POLICIES);
        System.err.println("  Calls to register(): " + addCounter);
        System.err.println("  Current flow: " + Thread.currentThread().getName());
        System.err.println("=".repeat(50));
    }
}