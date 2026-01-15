package life.core.jupiter.annotation;

import life.core.jupiter.extension.CancelPolicyExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(CancelPolicyExtension.class)
public @interface CancelPolicy {
}
