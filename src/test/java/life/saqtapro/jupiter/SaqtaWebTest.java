package life.saqtapro.jupiter;

import io.qameta.allure.Epic;
import life.core.jupiter.annotation.WebTest;
import life.core.jupiter.extension.TestPersonExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** UI-тест SaqtaPro: браузер из общего @WebTest + роли и стабы гос. сервисов */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Epic("SaqtaPro")
@Tag("saqtapro-ui")
@WebTest(width = 1440, height = 900)
@ExtendWith({SaqtaExtension.class, TestPersonExtension.class})
public @interface SaqtaWebTest {
}
