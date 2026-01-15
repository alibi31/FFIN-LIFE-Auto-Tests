package life.core.jupiter.extension;

import life.superapp.api.service.CancelPolicyService;
import life.superapp.api.service.impl.CancelPolicyApiService;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.PoliciesRegistry;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.List;

public class CancelPolicyExtension implements AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(CancelPolicyExtension.class);

    private final CancelPolicyService cancelPolicyService = new CancelPolicyApiService();


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), CancelPolicy.class)
                .ifPresent(ann -> {
                    List<String> policies = PoliciesRegistry.drain();

                    if (policies.isEmpty()) {
                        return;
                    }

                    for (String policy : policies) {
                        try {
                            log.info("Cancelling policy [{}] after test [{}]", policy, context.getDisplayName());
                            cancelPolicyService.cancelPolicy(policy, "test");
                        } catch (IOException e) {
                            log.error("Failed to cancel policy [{}] after test [{}]", policy, context.getDisplayName(), e);
                        }
                    }
                });
    }
}
