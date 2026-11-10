package life.saqtapro.test.api.client;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import life.saqtapro.api.ClientSearchApi;
import life.saqtapro.api.model.client.ClientSearchRequest;
import life.saqtapro.api.model.client.ClientSearchRequest.Filter;
import life.saqtapro.api.model.client.ClientSearchResponse.Client;
import life.saqtapro.api.model.client.ClientSearchResponse.Page;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.data.ClientSearchFixtures.RefClient;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static life.saqtapro.data.ClientSearchFixtures.ABILKHADIROV;
import static life.saqtapro.data.ClientSearchFixtures.INDIVIDUAL;
import static life.saqtapro.data.ClientSearchFixtures.LEGAL_ENTITY;
import static life.saqtapro.data.ClientSearchFixtures.MANAGERS;
import static life.saqtapro.data.ClientSearchFixtures.NON_EXISTENT_VALUE;
import static life.saqtapro.data.ClientSearchFixtures.POWER_SPORTS;
import static life.saqtapro.data.ClientSearchFixtures.SOLE_PROPRIETOR;
import static life.saqtapro.data.ClientSearchFixtures.TABYLOV;
import static life.saqtapro.data.ClientSearchFixtures.allFilters;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SaqtaApiTest
@Tag("client-search")
@Feature("Поиск и фильтрация клиентов")
@Owner("QA")
class ClientSearchFilterTest {

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final ClientSearchApi api = new ClientSearchApi(SaqtaAuth.tokenOf(TestUser.QA_EMPLOYEE));

    // ------------------------------------------------------------------ Поиск по value

    @Test
    @Story("Поиск по ФИО / ИИН / БИН")
    @DisplayName("POST /api/search/client — физ. лицо находится по ИИН")
    void individualByIin() {
        List<Client> clients = search(TABYLOV.bin(), allFilters());

        assertCard(findReference(clients, TABYLOV), TABYLOV);
        assertEveryClient(clients, c -> TABYLOV.bin().equals(c.bin()), "bin = " + TABYLOV.bin());
    }

    @Test
    @Story("Поиск по ФИО / ИИН / БИН")
    @DisplayName("POST /api/search/client — юр. лицо находится по БИН")
    void legalEntityByBin() {
        List<Client> clients = search(POWER_SPORTS.bin(), allFilters());

        assertCard(findReference(clients, POWER_SPORTS), POWER_SPORTS);
        assertEveryClient(clients, c -> POWER_SPORTS.bin().equals(c.bin()), "bin = " + POWER_SPORTS.bin());
    }

    @Test
    @Story("Поиск по ФИО / ИИН / БИН")
    @DisplayName("POST /api/search/client — ИП находится по полному ФИО")
    void solePropByFullName() {
        List<Client> clients = search(ABILKHADIROV.name(), allFilters());

        assertCard(findReference(clients, ABILKHADIROV), ABILKHADIROV);
        assertEveryClient(clients, nameContains(ABILKHADIROV.name()), "имя содержит «" + ABILKHADIROV.name() + "»");
    }

    @Test
    @Story("Поиск по ФИО / ИИН / БИН")
    @DisplayName("POST /api/search/client — физ. лицо находится по фамилии в нижнем регистре")
    void individualByLastNameLowercase() {
        String value = "табылов";

        List<Client> clients = search(value, allFilters());

        findReference(clients, TABYLOV);
        assertEveryClient(clients, nameContains(value), "имя содержит «" + value + "»");
    }

    @Test
    @Story("Поиск по ФИО / ИИН / БИН")
    @DisplayName("POST /api/search/client — физ. лицо находится по полному ФИО")
    @Description("По имени и по фамилии по отдельности клиент находится, а по полному ФИО — нет (на 10.11.2026)")
    void individualByFullName() {
        List<Client> clients = search(TABYLOV.name(), allFilters());

        findReference(clients, TABYLOV);
    }

    // ------------------------------------------------------------------ client_type

    @Test
    @Story("Фильтр по типу клиента")
    @DisplayName("POST /api/search/client — фильтр «Физ. лицо» возвращает только физ. лиц")
    @Description("Любой запрос с одним типом клиента возвращает 500 (на 10.11.2026)")
    void singleClientType() {
        List<Client> clients = search("", allFilters().clientType(List.of(INDIVIDUAL)));

        findReference(clients, TABYLOV);
        assertEveryClient(clients, c -> INDIVIDUAL.equals(c.clientType()), "client_type = " + INDIVIDUAL);
    }

    @Test
    @Story("Фильтр по типу клиента")
    @DisplayName("POST /api/search/client — физ. лицо не находится по фильтру «Юр. лицо» + «ИП»")
    void clientTypeExcludesIndividual() {
        assertNothingFound(search(TABYLOV.bin(), allFilters().clientType(List.of(LEGAL_ENTITY, SOLE_PROPRIETOR))));
    }

    @Test
    @Story("Фильтр по типу клиента")
    @DisplayName("POST /api/search/client — пустой client_type → ничего не найдено")
    void emptyClientType() {
        assertNothingFound(search(TABYLOV.bin(), allFilters().clientType(List.of())));
    }

    // ------------------------------------------------------------------ branches

    @Test
    @Story("Фильтр по филиалу")
    @DisplayName("POST /api/search/client — фильтр «Астана» возвращает только клиентов Астаны")
    void singleBranch() {
        String branch = ABILKHADIROV.branch();

        List<Client> clients = search("", allFilters().branches(List.of(branch)));

        findReference(clients, ABILKHADIROV);
        assertEveryClient(clients, c -> c.branchNames().contains(branch), "филиал = " + branch);
    }

    @Test
    @Story("Фильтр по филиалу")
    @DisplayName("POST /api/search/client — пустой branches не ограничивает выдачу по филиалу")
    @Description("Филиал не выбран — остальные фильтры работают, выдача та же, что при выборе всех филиалов")
    void emptyBranches() {
        List<Client> noBranch = search("", allFilters().branches(List.of()));
        List<Client> allBranches = search("", allFilters());

        findReference(noBranch, TABYLOV);
        findReference(noBranch, ABILKHADIROV);
        Allure.step("Выдача совпадает с выдачей по всем филиалам", () ->
                assertEquals(bins(allBranches), bins(noBranch),
                        "bin клиентов: слева — все филиалы, справа — филиал не выбран"));
        assertEveryClient(noBranch, c -> c.managerIds().stream().anyMatch(MANAGERS::contains),
                "менеджер из фильтра");
    }

    // ------------------------------------------------------------------ managers

    @Test
    @Story("Фильтр по ответственному лицу")
    @DisplayName("POST /api/search/client — фильтр по менеджеру возвращает только его клиентов")
    void singleManager() {
        long managerId = TABYLOV.managerId();

        List<Client> clients = search("", allFilters().managers(List.of(managerId)));

        findReference(clients, TABYLOV);
        assertEveryClient(clients, c -> c.managerIds().contains(managerId), "менеджер = " + managerId);
    }

    @Test
    @Story("Фильтр по ответственному лицу")
    @DisplayName("POST /api/search/client — клиент не находится, если его менеджера нет в фильтре")
    void managerExcludesClient() {
        List<Long> otherManagers = MANAGERS.stream().filter(id -> id != TABYLOV.managerId()).toList();

        assertNothingFound(search(TABYLOV.bin(), allFilters().managers(otherManagers)));
    }

    // ------------------------------------------------------------------ Прочее

    @Test
    @Story("Комбинация фильтров")
    @DisplayName("POST /api/search/client — «Нурлан» + все типы + Алматы + менеджеры стенда")
    @Description("Повторяет запрос из браузера: клиент находится, и каждый клиент в выдаче "
            + "удовлетворяет всем фильтрам одновременно")
    void filtersFromBrowser() {
        String value = "Нурлан";
        String branch = TABYLOV.branch();

        List<Client> clients = search(value, allFilters().branches(List.of(branch)));

        findReference(clients, TABYLOV);
        assertEveryClient(clients, nameContains(value)
                        .and(c -> c.branchNames().contains(branch))
                        .and(c -> c.managerIds().stream().anyMatch(MANAGERS::contains)),
                "имя содержит «" + value + "», филиал = " + branch + ", менеджер из фильтра");
    }

    @Test
    @Story("Пустой результат")
    @DisplayName("POST /api/search/client — несуществующее значение → пустая страница")
    void nothingFound() {
        Page page = api.found(ClientSearchRequest.of(NON_EXISTENT_VALUE, allFilters().build()));

        assertAll("Пустой ответ",
                () -> assertTrue(page.clients().isEmpty(), "data.data"),
                () -> assertEquals(0, page.total(), "total"),
                () -> assertEquals(1, page.currentPage(), "current_page"),
                () -> assertEquals(1, page.lastPage(), "last_page"),
                () -> assertNull(page.from(), "from"),
                () -> assertNull(page.to(), "to"));
    }

    @Test
    @Story("Авторизация")
    @DisplayName("POST /api/search/client — без токена → 401")
    void unauthorized() {
        ClientSearchApi.anonymous()
                .search(ClientSearchRequest.of(TABYLOV.bin(), allFilters().build()))
                .statusCode(401);
    }

    // ------------------------------------------------------------------ Хелперы

    private List<Client> search(String value, Filter.FilterBuilder filter) {
        return api.found(ClientSearchRequest.of(value, filter.build())).clients();
    }

    private static Predicate<Client> nameContains(String value) {
        String expected = value.toUpperCase(RU);
        return c -> c.name() != null && c.name().toUpperCase(RU).contains(expected);
    }

    /** Эталонный клиент есть в выдаче; возвращает его карточку */
    private static Client findReference(List<Client> clients, RefClient ref) {
        return Allure.step("В выдаче есть клиент " + ref.name(), () -> clients.stream()
                .filter(c -> ref.bin().equals(c.bin()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Клиент " + ref.name() + " (" + ref.bin()
                        + ") не найден. Найдено " + clients.size() + ":\n" + describe(clients))));
    }

    /** Тип, филиал и менеджер карточки совпадают с эталоном */
    private static void assertCard(Client client, RefClient ref) {
        Allure.step("Карточка совпадает с эталоном", () -> assertAll(
                () -> assertEquals(ref.name(), client.name(), "имя"),
                () -> assertEquals(ref.clientType(), client.clientType(), "client_type"),
                () -> assertTrue(client.branchNames().contains(ref.branch()), "филиал: " + client.branchNames()),
                () -> assertTrue(client.managerIds().contains(ref.managerId()), "менеджер: " + client.managerIds())));
    }

    /** Падает со списком конкретных клиентов, нарушивших условие */
    private static void assertEveryClient(List<Client> clients, Predicate<Client> condition, String description) {
        Allure.step("У всех " + clients.size() + " клиентов в выдаче: " + description, () -> {
            assertFalse(clients.isEmpty(), "Выдача пустая — проверять нечего");
            List<Client> violations = clients.stream().filter(condition.negate()).toList();
            if (!violations.isEmpty()) {
                fail("Условие «" + description + "» нарушено у " + violations.size() + " клиентов:\n"
                        + describe(violations));
            }
        });
    }

    private static void assertNothingFound(List<Client> clients) {
        Allure.step("Ничего не найдено", () ->
                assertTrue(clients.isEmpty(), "Ожидался пустой результат, найдено:\n" + describe(clients)));
    }

    private static Set<String> bins(List<Client> clients) {
        return clients.stream().map(Client::bin).collect(Collectors.toCollection(TreeSet::new));
    }

    private static String describe(List<Client> clients) {
        return clients.stream().map(Client::toString).collect(Collectors.joining("\n"));
    }
}