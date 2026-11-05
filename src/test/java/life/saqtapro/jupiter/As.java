package life.saqtapro.jupiter;

import life.saqtapro.auth.TestUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Внедряет SaqtaApi от имени пользователя:
 * {@code void test(@As(AGENT_A) SaqtaApi agent, @As(UNDERWRITER) SaqtaApi underwriter)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface As {
    TestUser value();
}
