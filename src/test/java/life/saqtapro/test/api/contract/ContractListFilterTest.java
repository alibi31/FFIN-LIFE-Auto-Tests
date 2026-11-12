package life.saqtapro.test.api.contract;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import life.saqtapro.api.ContractListApi;
import life.saqtapro.api.model.contract.ContractListRequest;
import life.saqtapro.api.model.contract.ContractListRequest.ContractListRequestBuilder;
import life.saqtapro.api.model.contract.ContractListResponse.AppliedFilters;
import life.saqtapro.api.model.contract.ContractListResponse.Contract;
import life.saqtapro.api.model.contract.ContractListResponse.Page;
import life.saqtapro.api.model.contract.ContractListResponse.Pagination;
import life.saqtapro.auth.SaqtaAuth;
import life.saqtapro.auth.TestUser;
import life.saqtapro.jupiter.SaqtaApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static life.saqtapro.api.model.contract.ContractListRequest.noFilters;
import static life.saqtapro.data.ContractListFixtures.ALMATY_BRANCH;
import static life.saqtapro.data.ContractListFixtures.BROWSER_RESPONSIBLE_PERSON;
import static life.saqtapro.data.ContractListFixtures.NON_EXISTENT_PRODUCT;
import static life.saqtapro.data.ContractListFixtures.NON_EXISTENT_VALUE;
import static life.saqtapro.data.ContractListFixtures.allFilters;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Эталонный договор не хардкодится: на dev договоры создаются и аннулируются каждый день,
 * поэтому в начале теста берём первый договор из общего списка и ищем уже по его полям.
 */
@SaqtaApiTest
@Tag("contract-list")
@Feature("Список и фильтрация договоров")
@Owner("QA")
class ContractListFilterTest {

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final ContractListApi api = new ContractListApi(SaqtaAuth.tokenOf(TestUser.QA_EMPLOYEE));

    // ------------------------------------------------------------------ Пагинация

    @Test
    @Story("Пагинация")
    @DisplayName("GET /api/contracts — без фильтров: 1-я страница по 10, фильтры не применены")
    @Description("Повторяет запрос из браузера при открытии списка договоров")
    void defaultList() {
        Page page = list(noFilters());
        Pagination p = page.pagination();
        AppliedFilters applied = p.appliedFilters();

        assertAll("Первая страница без фильтров",
                () -> assertFalse(page.contracts().isEmpty(), "data.data пустой"),
                () -> assertTrue(page.contracts().size() <= 10, "договоров больше per_page: " + page.contracts().size()),
                () -> assertEquals(1, p.currentPage(), "current_page"),
                () -> assertEquals(10, p.perPage(), "per_page"),
                () -> assertEquals(expectedTotalPages(p), p.totalPages(), "total_pages = ceil(total / per_page)"),
                () -> assertNull(p.prevPageUrl(), "prev_page_url на 1-й странице"),
                () -> assertNull(applied.value(), "applied_filters.value"),
                () -> assertTrue(applied.product().isEmpty(), "applied_filters.product"),
                () -> assertTrue(applied.statusName().isEmpty(), "applied_filters.status_name"),
                () -> assertNull(applied.contractDateStart(), "applied_filters.contract_date_start"),
                () -> assertNull(applied.contractDateEnd(), "applied_filters.contract_date_end"),
                () -> assertNull(applied.responsiblePersonId(), "applied_filters.responsible_person_id"));
    }

    @Test
    @Story("Пагинация")
    @DisplayName("GET /api/contracts — per_page=5 возвращает не больше 5 договоров")
    void customPerPage() {
        Page page = list(noFilters().perPage(5));

        assertAll(
                () -> assertEquals(5, page.pagination().perPage(), "per_page"),
                () -> assertEquals(Math.min(5, page.pagination().total()), page.contracts().size(), "договоров на странице"),
                () -> assertEquals(expectedTotalPages(page.pagination()), page.pagination().totalPages(), "total_pages"));
    }

    @Test
    @Story("Пагинация")
    @DisplayName("GET /api/contracts — 2-я страница не пересекается с 1-й")
    @Description("Если между запросами на стенде создали договор, выдача сдвинется — тогда тест может упасть ложно")
    void secondPageDiffers() {
        Page first = list(noFilters());
        assumeTrue(first.pagination().totalPages() > 1, "На стенде договоров на одну страницу");

        Page second = list(noFilters().page(2));

        Set<String> overlap = numbers(first.contracts());
        overlap.retainAll(numbers(second.contracts()));
        assertAll("2-я страница",
                () -> assertEquals(2, second.pagination().currentPage(), "current_page"),
                () -> assertNotNull(second.pagination().prevPageUrl(), "prev_page_url"),
                () -> assertFalse(second.contracts().isEmpty(), "data.data пустой"),
                () -> assertTrue(overlap.isEmpty(), "Договоры повторяются на 1-й и 2-й страницах: " + overlap));
    }

    @Test
    @Story("Пагинация")
    @DisplayName("GET /api/contracts — последняя страница: остаток договоров и next_page_url = null")
    void lastPage() {
        Pagination p = list(noFilters()).pagination();

        Page last = list(noFilters().page(p.totalPages()));

        int expectedSize = p.total() - (p.totalPages() - 1) * p.perPage();
        assertAll("Последняя страница " + p.totalPages(),
                () -> assertEquals(expectedSize, last.contracts().size(), "договоров на последней странице"),
                () -> assertNull(last.pagination().nextPageUrl(), "next_page_url"));
    }

    @Test
    @Story("Пагинация")
    @DisplayName("GET /api/contracts — страница за пределами total_pages → пустой список")
    void pageBeyondLast() {
        int totalPages = list(noFilters()).pagination().totalPages();

        Page page = list(noFilters().page(totalPages + 1));

        assertTrue(page.contracts().isEmpty(), "Ожидался пустой список, найдено:\n" + describe(page.contracts()));
    }

    // ------------------------------------------------------------------ Поиск по value

    @Test
    @Story("Поиск по номеру / ИИН / ФИО")
    @DisplayName("GET /api/contracts — поиск по номеру договора находит ровно его")
    void byContractNumber() {
        Contract ref = reference();

        List<Contract> contracts = contracts(noFilters().value(ref.contractNumber()));

        Allure.step("Найден ровно один договор " + ref.contractNumber(), () -> {
            assertEquals(1, contracts.size(), "Найдено:\n" + describe(contracts));
            assertEquals(ref.contractNumber(), contracts.get(0).contractNumber(), "contract_number");
        });
    }

    @Test
    @Story("Поиск по номеру / ИИН / ФИО")
    @DisplayName("GET /api/contracts — поиск по ИИН страхователя")
    void byPolicyholderIin() {
        Contract ref = reference();
        String iin = ref.policyholderBin();

        List<Contract> contracts = contracts(noFilters().value(iin).perPage(50));

        findReference(contracts, ref);
        assertEveryContract(contracts, c -> iin.equals(c.policyholderBin()) || iin.equals(c.insuredBin()),
                "ИИН страхователя или застрахованного = " + iin);
    }

    @Test
    @Story("Поиск по номеру / ИИН / ФИО")
    @DisplayName("GET /api/contracts — поиск по фамилии страхователя в нижнем регистре")
    void byLastNameLowercase() {
        Contract ref = reference();
        String lastName = ref.policyholderName().split(" ")[0].toLowerCase(RU);

        List<Contract> contracts = contracts(noFilters().value(lastName).perPage(50));

        assertEveryContract(contracts, matchesValue(lastName), "номер, ИИН или ФИО содержит «" + lastName + "»");
    }

    @Test
    @Story("Поиск по номеру / ИИН / ФИО")
    @DisplayName("GET /api/contracts — несуществующее значение → пустой список")
    void nothingFound() {
        Page page = list(noFilters().value(NON_EXISTENT_VALUE));

        assertNothingFound(page);
    }

    // ------------------------------------------------------------------ product

    @Test
    @Story("Фильтр по продукту")
    @DisplayName("GET /api/contracts — фильтр по продукту возвращает только этот продукт")
    void byProduct() {
        String product = reference().productSlug();

        List<Contract> contracts = contracts(noFilters().products(List.of(product)).perPage(50));

        assertEveryContract(contracts, c -> product.equals(c.productSlug()), "product_slug = " + product);
    }

    @Test
    @Story("Фильтр по продукту")
    @DisplayName("GET /api/contracts — несуществующий продукт → пустой список")
    @Description("Допущение: неизвестный slug игнорируется как «ничего не найдено». Если по ТЗ 422 — поменять проверку")
    void nonExistentProduct() {
        assertNothingFound(list(noFilters().products(List.of(NON_EXISTENT_PRODUCT))));
    }

    // ------------------------------------------------------------------ status_name

    @Test
    @Story("Фильтр по статусу")
    @DisplayName("GET /api/contracts — фильтр по статусу возвращает только этот статус")
    void byStatus() {
        String status = reference().statusName();

        List<Contract> contracts = contracts(noFilters().statuses(List.of(status)).perPage(50));

        assertEveryContract(contracts, c -> status.equals(c.statusName()), "status_name = " + status);
    }

    // ------------------------------------------------------------------ Период

    @Test
    @Story("Фильтр по дате договора")
    @DisplayName("GET /api/contracts — период из одного дня: все договоры за этот день")
    void singleDay() {
        Contract ref = reference();
        LocalDate day = ref.date();

        List<Contract> contracts = contracts(noFilters()
                .value(ref.contractNumber()).contractDateStart(day).contractDateEnd(day));

        findReference(contracts, ref);
        assertEveryContract(contracts, c -> c.date().equals(day), "contract_date = " + day);
    }

    @Test
    @Story("Фильтр по дате договора")
    @DisplayName("GET /api/contracts — период из 7 дней: все договоры внутри периода")
    void dateRange() {
        LocalDate end = reference().date();
        LocalDate start = end.minusDays(7);

        List<Contract> contracts = contracts(noFilters().contractDateStart(start).contractDateEnd(end).perPage(50));

        assertEveryContract(contracts, c -> !c.date().isBefore(start) && !c.date().isAfter(end),
                "contract_date в [" + start + " … " + end + "]");
    }

    @Test
    @Story("Фильтр по дате договора")
    @DisplayName("GET /api/contracts — только дата «с»: договоры не раньше неё")
    void onlyStartDate() {
        LocalDate start = reference().date();

        List<Contract> contracts = contracts(noFilters().contractDateStart(start).perPage(50));

        assertEveryContract(contracts, c -> !c.date().isBefore(start), "contract_date ≥ " + start);
    }

    @Test
    @Story("Фильтр по дате договора")
    @DisplayName("GET /api/contracts — только дата «по»: договоры не позже неё")
    void onlyEndDate() {
        LocalDate end = reference().date().minusDays(1);

        List<Contract> contracts = contracts(noFilters().contractDateEnd(end).perPage(50));

        assertEveryContract(contracts, c -> !c.date().isAfter(end), "contract_date ≤ " + end);
    }

    @Test
    @Story("Фильтр по дате договора")
    @DisplayName("GET /api/contracts — дата «с» позже даты «по» → пустой список")
    @Description("Допущение: backend отдаёт пустой список. Если по ТЗ должен быть 422 — поменять проверку")
    void startAfterEnd() {
        LocalDate day = reference().date();

        assertNothingFound(list(noFilters().contractDateStart(day).contractDateEnd(day.minusDays(1))));
    }

    // ------------------------------------------------------------------ responsible_person_id

    @Test
    @Story("Фильтр по ответственному лицу")
    @DisplayName("GET /api/contracts — фильтр по ответственному возвращает только его договоры")
    void byResponsiblePerson() {
        Contract ref = reference();
        long personId = ref.responsiblePersonId();

        List<Contract> contracts = contracts(noFilters().responsiblePersonId(personId).perPage(50));

        assertEveryContract(contracts, c -> Objects.equals(personId, c.responsiblePersonId()),
                "responsible_person_id = " + personId);
    }

    // ------------------------------------------------------------------ branches

    @Test
    @Story("Фильтр по филиалу")
    @DisplayName("GET /api/contracts — фильтр «Алматы» возвращает только договоры Алматы")
    void byBranch() {
        List<Contract> contracts = contracts(noFilters().branches(List.of(ALMATY_BRANCH)).perPage(50));

        assertEveryContract(contracts, c -> Objects.equals(ALMATY_BRANCH, c.branchId()), "branch_id = " + ALMATY_BRANCH);
    }

    // ------------------------------------------------------------------ Прочее

    @Test
    @Story("Комбинация фильтров")
    @DisplayName("GET /api/contracts — «выбрано всё» = «ничего не выбрано»")
    @Description("Все продукты и все статусы в фильтре дают ту же выдачу, что и пустые фильтры. "
            + "Если упало с разницей в total — у договоров на стенде есть продукт или статус, "
            + "которого нет в ContractListFixtures (или наоборот, фронт что-то скрывает)")
    void allSelectedEqualsNoFilters() {
        Page none = list(noFilters());
        Page all = list(allFilters());

        assertAll("Выдача «всё выбрано» против «без фильтров»",
                () -> assertEquals(none.pagination().total(), all.pagination().total(), "total"),
                () -> assertEquals(numbers(none.contracts()), numbers(all.contracts()), "договоры на 1-й странице"));
    }

    @Test
    @Story("Комбинация фильтров")
    @DisplayName("GET /api/contracts — «SQ» + все продукты + все статусы + ответственный 1255 + Алматы")
    @Description("Повторяет запрос из браузера, когда в фильтрах выбрано всё и в поиск введено «SQ»: "
            + "каждый договор в выдаче удовлетворяет всем фильтрам одновременно")
    void filtersFromBrowser() {
        String value = "SQ";

        List<Contract> contracts = contracts(allFilters()
                .value(value)
                .responsiblePersonId(BROWSER_RESPONSIBLE_PERSON)
                .branches(List.of(ALMATY_BRANCH))
                .perPage(50));

        assertEveryContract(contracts, matchesValue(value)
                        .and(c -> Objects.equals(BROWSER_RESPONSIBLE_PERSON, c.responsiblePersonId()))
                        .and(c -> Objects.equals(ALMATY_BRANCH, c.branchId())),
                "номер/ИИН/ФИО содержит «" + value + "», ответственный = " + BROWSER_RESPONSIBLE_PERSON
                        + ", branch_id = " + ALMATY_BRANCH);
    }

    @Test
    @Story("Комбинация фильтров")
    @DisplayName("GET /api/contracts — ИИН + продукт + статус + день + ответственный + филиал")
    @Description("Эталонный договор находится, и каждый договор в выдаче удовлетворяет всем фильтрам сразу")
    void allFiltersTogether() {
        Contract ref = reference();
        LocalDate day = ref.date();

        List<Contract> contracts = contracts(combined(ref));

        findReference(contracts, ref);
        assertEveryContract(contracts, c -> ref.policyholderBin().equals(c.policyholderBin())
                        && Objects.equals(ref.productSlug(), c.productSlug())
                        && ref.statusName().equals(c.statusName())
                        && c.date().equals(day)
                        && Objects.equals(ref.responsiblePersonId(), c.responsiblePersonId())
                        && Objects.equals(ref.branchId(), c.branchId()),
                "ИИН, продукт, статус, дата, ответственный и филиал совпадают с эталоном");
    }

    @Test
    @Story("Комбинация фильтров")
    @DisplayName("GET /api/contracts — applied_filters повторяют переданные фильтры")
    @Description("Допущение: даты в applied_filters возвращаются в том же формате, в каком переданы")
    void appliedFiltersEchoRequest() {
        Contract ref = reference();
        ContractListRequest request = combined(ref).build();

        AppliedFilters applied = api.found(request).pagination().appliedFilters();

        assertAll("applied_filters",
                () -> assertEquals(ref.policyholderBin(), applied.value(), "value"),
                () -> assertEquals(List.of(ref.productSlug()), applied.product(), "product"),
                () -> assertEquals(List.of(ref.statusName()), applied.statusName(), "status_name"),
                () -> assertEquals(ContractListRequest.date(ref.date()), applied.contractDateStart(), "contract_date_start"),
                () -> assertEquals(ContractListRequest.date(ref.date()), applied.contractDateEnd(), "contract_date_end"),
                () -> assertEquals(ref.responsiblePersonId(), applied.responsiblePersonId(), "responsible_person_id"));
    }

    @Test
    @Story("Сортировка")
    @DisplayName("GET /api/contracts — договоры отсортированы от новых к старым")
    @Description("Поведение снято с dev на 12.11.2026, явно в ТЗ не описано")
    void sortedByDateDesc() {
        List<Contract> contracts = contracts(noFilters().perPage(50));

        Allure.step("contract_date_timestamp не возрастает", () -> {
            for (int i = 1; i < contracts.size(); i++) {
                Contract prev = contracts.get(i - 1);
                Contract next = contracts.get(i);
                if (next.contractDateTimestamp() > prev.contractDateTimestamp()) {
                    fail("Нарушен порядок на позиции " + i + ":\n" + prev + "\n" + next);
                }
            }
        });
    }

    @Test
    @Story("Авторизация")
    @DisplayName("GET /api/contracts — без токена → 401")
    void unauthorized() {
        ContractListApi.anonymous()
                .list(noFilters().build())
                .statusCode(401);
    }

    // ------------------------------------------------------------------ Хелперы

    private Page list(ContractListRequestBuilder request) {
        return api.found(request.build());
    }

    private List<Contract> contracts(ContractListRequestBuilder request) {
        return list(request).contracts();
    }

    /** Свежий договор стенда с ответственным лицом и филиалом (у договоров из кабинета их нет) */
    private Contract reference() {
        return Allure.step("Эталонный договор: первый в общем списке с ответственным лицом и филиалом", () ->
                contracts(noFilters().perPage(50)).stream()
                        .filter(c -> c.responsiblePersonId() != null && c.branchId() != null)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "Среди 50 последних договоров нет ни одного с ответственным лицом и филиалом")));
    }

    /** Все фильтры по полям эталона */
    private static ContractListRequestBuilder combined(Contract ref) {
        return noFilters()
                .value(ref.policyholderBin())
                .products(List.of(ref.productSlug()))
                .statuses(List.of(ref.statusName()))
                .contractDateStart(ref.date())
                .contractDateEnd(ref.date())
                .responsiblePersonId(ref.responsiblePersonId())
                .branches(List.of(ref.branchId()))
                .perPage(50);
    }

    private static int expectedTotalPages(Pagination p) {
        return (int) Math.ceil((double) p.total() / p.perPage());
    }

    private static Predicate<Contract> matchesValue(String value) {
        String expected = value.toUpperCase(RU);
        return c -> Stream.of(c.contractNumber(), c.insuredBin(), c.policyholderBin(),
                        c.insuredName(), c.policyholderName())
                .filter(Objects::nonNull)
                .anyMatch(field -> field.toUpperCase(RU).contains(expected));
    }

    private static void findReference(List<Contract> contracts, Contract ref) {
        Allure.step("В выдаче есть договор " + ref.contractNumber(), () -> {
            if (contracts.stream().noneMatch(c -> ref.contractNumber().equals(c.contractNumber()))) {
                fail("Договор " + ref + " не найден. Найдено " + contracts.size() + ":\n" + describe(contracts));
            }
        });
    }

    /** Падает со списком конкретных договоров, нарушивших условие */
    private static void assertEveryContract(List<Contract> contracts, Predicate<Contract> condition, String description) {
        Allure.step("У всех " + contracts.size() + " договоров в выдаче: " + description, () -> {
            assertFalse(contracts.isEmpty(), "Выдача пустая — проверять нечего");
            List<Contract> violations = contracts.stream().filter(condition.negate()).toList();
            if (!violations.isEmpty()) {
                fail("Условие «" + description + "» нарушено у " + violations.size() + " договоров:\n"
                        + describe(violations));
            }
        });
    }

    private static void assertNothingFound(Page page) {
        Allure.step("Ничего не найдено", () -> assertAll(
                () -> assertTrue(page.contracts().isEmpty(), "Ожидался пустой список, найдено:\n" + describe(page.contracts())),
                () -> assertEquals(0, page.pagination().total(), "total")));
    }

    private static Set<String> numbers(List<Contract> contracts) {
        return contracts.stream().map(Contract::contractNumber).collect(Collectors.toCollection(HashSet::new));
    }

    private static String describe(List<Contract> contracts) {
        return contracts.stream().map(Contract::toString).collect(Collectors.joining("\n"));
    }
}
