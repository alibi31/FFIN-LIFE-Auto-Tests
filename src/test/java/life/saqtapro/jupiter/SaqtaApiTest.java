package life.saqtapro.jupiter;

import io.qameta.allure.Epic;
import life.core.jupiter.extension.TestPersonExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** API-тест SaqtaPro: тег для выборочного запуска, внедрение ролей, стабов и тестовых персон */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Epic("SaqtaPro")
@Tag("saqtapro-api")
@ExtendWith({SaqtaExtension.class, TestPersonExtension.class})
public @interface SaqtaApiTest {
}
