# Autotest testing playground

Этот репозиторий содержит готовый набор зависимостей и примеров для автоматизации UI- и API-тестов на Java с использованием JUnit 5, Playwright, Rest Assured и Allure. 
## 1. Что такое тест и зачем он нужен
- **Цель:** подтвердить, что продукт работает так, как ожидается, и быстро сообщать о регрессиях.
- **Хороший тест** изолирован, повторяем, читабелен и даёт понятный результат (зелёный/красный).
- **Пирамида тестирования:**
    - **Юнит-тесты** — проверяют отдельные функции/классы и выполняются мгновенно.
    - **Интеграционные/контрактные тесты** — проверяют взаимодействие сервисов и контракты API.
    - **UI-тесты** — подтверждают пользовательские сценарии; их меньше всего, они дороже по времени.

## 2. Минимальный набор инструментов
- **Java 17+** и **Gradle Wrapper** (`./gradlew`).
- **JUnit 5** для написания тестов.
- **Rest Assured** и **Retrofit** для API-слоя (см. `src/test/java/life/core/http`).
- **Playwright** для браузерных сценариев (см. `src/test/java/life/core/web`).
- **Allure** для формирования отчёта (`./gradlew allureReport`).

## 3. Как запустить тесты
1. Установите JDK 17+ и убедитесь, что `JAVA_HOME` настроен.
2. Выполните установку зависимостей и запуск тестов:
   ```bash
   ./gradlew test
   ```
3. Чтобы запускать только часть сценариев, используйте теги:
   ```bash
   ./gradlew test -PincludeTags=ffin-api,PA-UI
   ./gradlew test -PexcludeTags=slow
   ```
4. После прогона откройте отчёт Allure:
   ```bash
   ./gradlew serveAfterTest
   ```
   или сформируйте статический отчёт:
   ```bash
   ./gradlew allureReport
   ```
5. Для отладки Playwright-тестов с инспектором:
   ```bash
   ./gradlew uiDebug -Dheadless=false
   ```


## 5. Пишем API-тест с готовым `ApiClient`
Используйте `life.core.http.ApiClient` и конфиги окружения (`EnvConfig`) вместо ручного создания запросов. Так тесты выглядят одинаково и легко поддерживаются.

```java
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import life.core.http.ApiClient;
import life.ffin.api.model.saykhat.SayakhatRegistrationRequest;
import life.core.jupiter.annotation.CancelPolicy;
import life.utils.PoliciesRegistry;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;

@Tag("ffin-api")
@Owner("QA")
class RegistrationApiTest {
    private final ApiClient api = new ApiClient();
    private final String baseUrl = EnvConfig.cfg().apiFfinUrl();

    @Test
    @CancelPolicy // после теста полис отменится автоматически
    @DisplayName("POST /v1/products/freedom-travel/registration -> 200")
    @Description("Пример позитивного API-теста с готовым клиентом и билдером запроса")
    void shouldCreatePolicy() {
        SayakhatRegistrationRequest body = SayakhatRegistrationRequest.builder()
                .fioKir("АЛЛАЯРОВ АЛИХАН")
                .iin("4343243423432")
                .iin2("4234234234234")
                .dateStart("2025-11-29")
                .dateEnd("2025-12-06")
                .product("travel")
                .agreement("1")
                .build();

        String policyNumber = api.post(baseUrl, "v1/products/freedom-travel/registration", body)
                .statusCode(200)
                .contentType(JSON)
                .body("status", is(true))
                .extract()
                .path("policy_number")
                .toString();

        PoliciesRegistry.register(policyNumber);
    }
}
```
**Что важно:**
- Имя теста должно отвечать на вопрос «что проверяем?». Например: `addsTwoNumbers`.
- Всегда отделяйте подготовку, действие и проверку пустыми строками или комментариями.
- Один тест — один сценарий. Если нужен другой кейс, пишите отдельный тест.

**Практические советы:**
- Ставьте `@Tag` для группировки запусков (см. свойство `includeTags` в `build.gradle.kts`).
- Проверяйте **и** код ответа, и содержимое тела, и время ответа. Для сложных ответов используйте JSON Schema.
- Храните базовые настройки (base URI, токены) в вспомогательных классах (`RequestSpec`, `ResponseSpec`).

### Как устроены готовые API-тесты
- **Позитив + контракт + пагинация:** `MainApiTest.getAllEmployeesTest` отправляет `GET` на `/v1/main/our-team/employee`,
  проверяет код 200, валидирует JSON по схеме `schema/our-team/our-team.json`, а также убеждается, что список не пуст и
  содержит поле `our_team` — хороший пример того, как в одном месте объединять контракт и смысловую проверку.
- **Параметризация позитивов:** `MainApiTest.getEmployeeByIdTest` берёт несколько валидных `id` через `@ValueSource` и
  гарантирует, что для каждого вернётся 200 и корректная схема. Этот шаблон можно копировать для однотипных проверок.
- **Негативы с разными типами данных:** `MainApiTest.getEmployeeInvalidNumberTest` и `getEmployeeNonNumericTest` демонстрируют,
  как зафиксировать ожидаемые ошибки (422) для некорректных числовых и нечисловых идентификаторов через параметризацию.
- **Проверка 404 с параметром в Allure:** `MainApiTest.getEmployeeNotFoundTest` добавляет `Allure.parameter`, чтобы в отчёте
  было видно, на каком `id` тест упал, и валидирует схему ошибки.
- **Флоу с подготовкой/очисткой данных:** `SayakhatRegistrationTest.shouldBeStatus200FromRegistrationTest` собирает body через
  builder (`SayakhatRegistrationRequest`), отправляет `POST` на `/v1/products/freedom-travel/registration`, валидирует 200 и
  `status=true`, а затем регистрирует номер полиса в `PoliciesRegistry`. Аннотация `@CancelPolicy` гарантирует, что после теста
  полис будет отменён через `CancelPolicyExtension`, поэтому окружение не захламляется.

Когда пишете новые проверки:
- Выносите повторяющиеся поля в билдеры/фабрики запросов.
- Для негативов фиксируйте ожидаемую схему ошибки, чтобы ловить неожиданные форматы.
- Добавляйте Allure-параметры для ключевых переменных (id, тип запроса), чтобы отчёт был самодокументируемым.

## 6. Пишем UI-тест с аннотациями `@WebTest` и Page Object
Используйте `@WebTest`, чтобы расширение подняло контекст Playwright, и `Pages.open` для единообразного открытия страниц.

```java
import io.qameta.allure.Owner;
import life.cabinet.page.HomePage;
import life.cabinet.page.LoginPage;
import life.core.web.Pages;
import life.core.jupiter.annotation.WebTest;
import life.superapp.jupiter.annotation.Auth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("PA-UI")
@Owner("QA")
class LoginUiTest {

    @Test
    @WebTest(width = 375, height = 812, isMobile = true)
    @Auth(iin = "123456789012", fullName = "Test User")
    @DisplayName("Пользователь авторизуется и попадает на главную")
    void userCanLogin() {
        LoginPage login = Pages.open("https://example.com/login", LoginPage.class)
                .acknowledge()
                .fillUsernameAndPassword("123456789012", "password");

        HomePage home = login.submit(HomePage.class);
        home.checkThatPageLoaded();
    }
}
```

**Практические советы:**
- Для отладки UI-тестов запускайте `uiDebug`, чтобы открыть Playwright Inspector.
- Аннотация `@Auth` положит токен в `sessionStorage`, поэтому защищённые страницы можно открывать без ручного логина.
- Выносите селекторы в Page Object-классы (`life/.../page/*Page.java`), чтобы переиспользовать их и избежать дублирования.

### Как устроены готовые UI-тесты
- **Мобильный сценарий с `@WebTest`:** `life.cabinet.test.web.LoginTest` запускает браузер с мобильными размерами
  (`width=375`, `height=812`, `isMobile=true`) и открывает страницу авторизации через `Pages.open(authUrl, LoginPage.class)`.
  Далее Page Object цепочкой вызывает `acknowledge()`, заполняет логин/пароль и через `submit(HomePage.class)` переходит на
  домашнюю страницу, где `checkThatPageLoaded()` проверяет ключевые элементы. Это образец «счастливого пути».
- **Негативный UI-кейс:** тот же тестовый класс содержит метод `userShouldStayOnLoginPageAfterLoginWithBadCredentials`.
  Он вводит заведомо неверный пароль и через `submit(LoginPage.class)` проверяет, что страница не сменилась и отображается
  сообщение об ошибке. Такой паттерн удобно повторять для всех негативных UI-сценариев.
- **Работа с несколькими страницами:** в тестах `life.superapp.test.web.*` (например, `ContractDetailsWebTest`) используется
  переход по нескольким Page Object — `Pages.open(...)` -> `CalculationPage` -> `PolicyholderDetailsPage` -> `PaymentSchedulePage`.
  Каждый класс инкапсулирует проверки готовности страницы (`checkThatPageLoaded`/`waitReady`) и действия пользователя, что
  облегчает сопровождение селекторов и локаторов.

Когда пишете новые UI-сценарии:
- Добавляйте проверки загрузки (ожидание ключевых элементов) сразу после открытия страницы, чтобы падения были ранними и понятными.
- Соблюдайте принцип «один тест — один пользовательский путь»: если путь меняется (другой тип пользователя или продукт), делайте
  отдельный метод.
- Переиспользуйте `Pages.on(PageClass)` для уже открытой вкладки вместо повторного `open`.

### Как открывать страницы в UI-тестах
В проекте за это отвечает хелпер `Pages` (`src/test/java/life/core/web/Pages.java`). Он создаёт новую вкладку в уже подготовленном контексте и
возвращает Page Object указанного класса:

```java
@WebTest
class LoginTest {
    @Test
    void openLoginPage() {
        LoginPage login = Pages.open("https://example.com/login", LoginPage.class);
        login.waitReady(); // ваш метод ожидания
    }
}
```

- Метод `open(url, PageClass)` открывает ссылку, дожидается загрузки сети и отдаёт готовый объект страницы.
- Метод `on(PageClass)` используется, если вкладка уже открыта: `HomePage home = Pages.on(HomePage.class);`.
- Если контекст не инициализирован, вы получите исключение — значит, на классе/методе забыли повесить `@WebTest`.

### Паттерн Page Object: как писать в одном стиле
- **Где хранить:** создавайте страницы в каталогах `src/test/java/life/<product>/page`, например `life/cabinet/page/LoginPage.java`.
- **Шаблон класса:**
  ```java
  package life.cabinet.page;

  import com.microsoft.playwright.Page;
  import com.microsoft.playwright.Locator;
  import com.microsoft.playwright.options.AriaRole;
  import life.core.web.BasePage;
  import life.core.web.BasePageFactory;
  import javax.annotation.Nonnull;

  import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

  public class SamplePage extends BasePage<SamplePage> {
      private Locator actionButton;

      @Override
      public void initComponents() {
          actionButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Действие"));
      }

      @Override
      @Nonnull
      public SamplePage checkThatPageLoaded() {
          assertThat(actionButton).isVisible();
          return this;
      }

      public <T extends BasePage<?>> T clickAction(Class<T> expectedPage) {
          actionButton.click();
          return BasePageFactory.createInstance(page, expectedPage);
      }
  }
  ```
- **Инициализация:** `BasePageFactory` создаёт объект страницы и вызывает `initComponents()`. В тесте используйте `Pages.open(..., SamplePage.class)` или `Pages.on(SamplePage.class)`.
- **Правила оформления:** храните селекторы как поля, давайте методам имена на русском/английском, отражающие действие (`fillUsernameAndPassword`, `checkError`). Один метод — одно осмысленное действие или проверка.

## 7. Как писать понятные проверки
- Используйте выразительные сообщения в ассертах: `assertEquals(expected, actual, "описание ошибки")`.
- Старайтесь иметь один ассерт на сценарий. Несколько ассертов допустимы, если они логически связаны.
- При работе с коллекциями проверяйте размер, содержимое и порядок, если он важен.

## 8. Фикстуры и подготовка данных
- В JUnit 5 используйте `@BeforeEach`/`@AfterEach` для подготовки и очистки окружения.
- Для тяжёлых подготовок (создание пользователя через API) делайте вспомогательные сервисы. Пример готовой реализации — `life.superapp.api.service.AuthService`.
- Если данные нужно создавать один раз на набор тестов, применяйте `@BeforeAll`/`@AfterAll` и статические ресурсы.

## 9. Как выбирать сценарии
1. Начните с **критических путей** (регистрация, оплата, авторизация).
2. Добавьте **негативные кейсы**: неверный пароль, пустые поля, неверный токен.
3. Для UI-тестов выделяйте только ключевые пользовательские путешествия, остальные проверки переносите в API или юнит-уровень.

## 10. Отладка и стабильность
- Делайте тесты **детерминированными**: фиксируйте тестовые данные и окружение.
- Логируйте запросы/ответы (Rest Assured фильтр Allure включён) и сохраняйте скриншоты в UI-тестах при падении.
- Повторные запуска (`flaky`) — сигнал к поиску причины, а не к безусловным ретраям.

## 11. Мини-чеклист перед коммитом
- Имя теста понятно без контекста кода.
- Данные генерируются внутри теста или в фабриках, а не завязаны на окружение.
- В отчёте Allure шаги и вложения помогают понять, что пошло не так.
- Запуск `./gradlew test` проходит локально, или зафиксированы причины, почему нет.

## 12. Проектные аннотации, которые экономят время
Эти аннотации регистрируют JUnit-расширения и автоматизируют рутину.

- **`@WebTest`** (`life.core.jupiter.annotation.WebTest`) — инициализирует Playwright-контекст перед тестом, настраивает вьюпорт и мобайл-режим
  (если указаны `width`, `height`, `isMobile`). Без неё `Pages.open(...)` не сможет создать страницу, потому что нет активного контекста. Также
  добавляет Allure JUnit 5 listener для шагов/вложений.
- **`@Auth`** (`life.superapp.jupiter.annotation.Auth`) — Работает только для ПА в суперапп. Принимает `iin` и `fullName`. Расширение `AuthExtension` перед запуском теста получает
  одноразовый токен (OTT) или access-token, кеширует его и кладёт в `UiSession`. Если браузерный контекст уже поднят, токен сразу кладётся в
  `sessionStorage` (ключ `accessToken`), поэтому в UI-тестах можно открывать защищённые страницы без ручного логина. В параметрах теста можно
  запросить `@OneTimeToken` или `@AccessToken`, и расширение подставит их автоматически. (По его анологии можно сделать аннотации для авторизации в ЛК)
- **`@CancelPolicy`** (`life.core.jupiter.annotation.CancelPolicy`) — навешивается на тестовый метод. После теста расширение
  `CancelPolicyExtension` вычитывает номера полисов из `PoliciesRegistry` и отправляет запросы на отмену через `CancelPolicyApiService`.
  Используйте её там, где тест создаёт полисы и нужно гарантированно аннулировать полис.

## 13. Куда смотреть в этом проекте
- **UI-страницы:** `src/test/java/life/cabinet/page` и `src/test/java/life/superapp/page`.
- **API-модели и сервисы:** `src/test/java/life/superapp/api` и `src/test/java/life/core/http`.
- **Примеры тестов:**
    - UI: `src/test/java/life/cabinet/test/web/LoginTest.java`
    - API: `src/test/java/life/ffin/test/api/MainApiTest.java`

Это руководство можно использовать как шпаргалку: двигайтесь сверху вниз, пробуйте примеры в своём окружении и постепенно углубляйтесь в сложные сценарии.