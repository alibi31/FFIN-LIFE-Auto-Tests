package life.superapp.jupiter.annotation;

import life.superapp.jupiter.extension.AuthExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AuthExtension.class)
public @interface OneTimeToken {
}
