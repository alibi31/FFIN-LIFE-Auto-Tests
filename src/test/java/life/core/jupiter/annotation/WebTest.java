package life.core.jupiter.annotation;

import io.qameta.allure.junit5.AllureJunit5;
import life.core.jupiter.extension.BrowserExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith({BrowserExtension.class, AllureJunit5.class})
public @interface WebTest {
    int width() default -1;
    int height() default -1;
    boolean isMobile() default false;
}
