# SaqtaPro — автотесты

Модуль `life.saqtapro` для репозитория FFIN-LIFE-Auto-Tests. Кладётся поверх репозитория как есть:
переиспользует `life.core` (RequestSpec + AllureFilter, BasePage, Pages, @WebTest) и
`life.utils.data` (TestPersonFactory, IinGenerator). **Новых зависимостей не нужно** —
WireMock, Awaitility, ArchUnit, Playwright, REST Assured уже есть в `build.gradle.kts`.

Стратегия и матрица сценариев — в документе «SaqtaPro — Стратегия автотестирования».
ID тестов в `@DisplayName` (CMP-02, GOV-05, UW-10…) совпадают с ID в документе.

## Структура

```
src/test/java/life/saqtapro/
├── config/        SaqtaConfig (Owner), Saqta.secret() — секреты только из env/-D
├── auth/          Role, TestUser (7 тестовых пользователей), SaqtaAuth (кэш токенов)
├── api/           SaqtaApi.as(user) → clients() / gov() / policies() / underwriting()
│   └── model/     Requests, Statuses — тела запросов и статусы
├── stubs/         GovStubs → bmg() / egov() / consent() / compliance()  (WireMock admin API)
├── data/          KzIban (валидный mod-97), Iins (невалидные ИИН), Phones
├── steps/         SaqtaSteps — составные шаги: verifiedClient, draftPolicy, policyUnderReview
├── jupiter/       @SaqtaApiTest, @SaqtaWebTest, @As(TestUser), SaqtaExtension
├── page/          SaqtaLoginPage, ShellPage, ClientFormPage, UnderwritingPage
├── test/api/      rbac/, compliance/, gov/, underwriting/
├── test/web/      RoleNavigationWebTest, GovAutofillWebTest, UnderwritingWebTest
└── arch/          SaqtaArchitectureTest — тесты не ходят в RestAssured/WireMock напрямую
infra/saqtapro/docker-compose.yml   SaqtaPro + WireMock для локального запуска и CI
```

Как выглядит тест:

```java
@Test
@DisplayName("GOV-05: номер не принадлежит ИИН — SMS-согласие не отправляется")
void phoneNotOwnedByIin(@As(AGENT_A) SaqtaApi agent, GovStubs stubs) {
    TestPerson person = TestPersonFactory.adult();
    stubs.bmg().numberRegistered(person.phone());
    stubs.egov().phoneNotOwnedBy(person);

    finish(agent, person).body("status", equalTo(PHONE_NOT_OWNED.name()));

    assertEquals(0, stubs.consent().sendCallsFor(person));
}
```

## Запуск

```bash
# учётка сотрудника для оформления договоров (в CI — из секретов); токен тест получает сам через POST /api/login
export SAQTA_QA_EMPLOYEE_LOGIN=имя@ffin.life SAQTA_QA_EMPLOYEE_PASSWORD=...
./gradlew test -Ptags="pension-annuity"                # регистрация пенсионного аннуитета
./gradlew test -Ptags="saqtapro-api & smoke"           # проверка логина

# для матрицы ролей — по аналогии: SAQTA_AGENT_A_LOGIN / SAQTA_AGENT_A_PASSWORD и т.д.

./gradlew test -Ptags="saqtapro-api"                 # все API-тесты SaqtaPro
./gradlew test -Ptags="saqtapro-api & rbac"          # только матрица прав
./gradlew test -Ptags="gov-integration"              # API + UI по гос. интеграции
./gradlew test -Ptags="saqtapro-ui"                  # Playwright
./gradlew test -Ptags="saqtapro-arch"                # архитектурные правила
```

Нужные учётки: `AGENT_A`, `AGENT_B` (оба филиал Алматы), `MANAGER_ALMATY`, `MANAGER_ASTANA`,
`UNDERWRITER`, `UNDERWRITER_2`, `COMPLIANCE_OFFICER`.

## Что сверить с командой SaqtaPro до первого прогона

Всё ниже — допущения, вынесенные в одно место; при расхождении правится один файл, тесты не меняются.

| Что | Где поправить |
| --- | --- |
| Пути SaqtaPro API | `api/SaqtaEndpoints` |
| Названия статусов договора, верификации, комплаенса | `api/model/Statuses` |
| Поля запросов (CreateClient, PolicyDraft, Decision) и лимит агента | `api/model/Requests` |
| Пути и формат вызовов SaqtaPro → БМГ / eGov / комплаенс | `stubs/GovPaths`, `stubs/GovPayloads` |
| Как SaqtaPro подключается к WireMock (переменные `*_BASE_URL`) | `infra/saqtapro/docker-compose.yml` |
| Ответ при комплаенс-блокировке: 422 при создании клиента или 201 + `BLOCKED` | `ComplianceTest.blockedClient()` |
| Права ролей (матрица) | `RbacMatrixTest.matrix()` |
| `data-testid` во фронтенде | page-классы: `login-*`, `nav-*`, `client-*`, `gov-verification-status`, `uw-*`, `error-403` |

## Две рекомендации к общему коду

1. `life.core.http.filters.AllureFilter` маскирует пароли и токены, но не ПДн. Для SaqtaPro стоит
   добавить в `SECRET_JSON_FIELDS` поля `iin|phone|iban` — иначе ИИН и IBAN клиентов попадают в Allure.
2. В `src/test/resources/test.properties` лежат реальные `username`/`password`. Их стоит перенести
   в переменные окружения CI (как `CABINET_UIN` / `CABINET_PASSWORD`) и сменить пароль.
