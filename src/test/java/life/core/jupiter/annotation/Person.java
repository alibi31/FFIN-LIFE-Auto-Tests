package life.core.jupiter.annotation;

import life.core.jupiter.extension.TestPersonExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ExtendWith(TestPersonExtension.class)
public @interface Person {
    Role value() default Role.ADULT;

    enum Role {ADULT, ELDER, CHILD}
}