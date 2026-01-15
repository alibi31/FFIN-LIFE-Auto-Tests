package life.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PoliciesRegistry {
    private PoliciesRegistry() {}

    private static final ThreadLocal<List<String>> POLICIES = ThreadLocal.withInitial(ArrayList::new);

    public static void register(String policyNumber) {
        if (policyNumber == null || policyNumber.isBlank()) return;

        POLICIES.get().add(policyNumber);
    }

    public static List<String> drain() {
        List<String> policies = new ArrayList<>(POLICIES.get());

        POLICIES.get().clear();
        return Collections.unmodifiableList(policies);
    }

    public static void clear() {
        POLICIES.get().clear();
    }
}
