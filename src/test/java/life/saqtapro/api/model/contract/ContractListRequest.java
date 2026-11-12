package life.saqtapro.api.model.contract;

import lombok.Builder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query-параметры GET /api/contracts. Структура снята с реальных запросов SaqtaPro (dev-saqta), вкладка «Договоры»:
 * <pre>
 * ?value=SQ&page=1&per_page=10
 * &product=["osrns", "pa_individual", …]          — JSON-массив slug'ов строкой
 * &contract_date_start=&contract_date_end=
 * &responsible_person_id=1255
 * &status_name=["ACTIVE", "CANCELLED", …]           — JSON-массив строкой
 * &branches=1
 * </pre>
 * Rest Assured сам кодирует [ ] " и пробелы (%5B, %22…). Пустые массивы и даты — «без фильтра».
 */
@Builder(toBuilder = true)
public record ContractListRequest(
        /* Номер договора, ИИН/БИН или ФИО — то, что вводят в поиск */
        String value,
        int page,
        int perPage,
        /* product_slug: "pa_individual_calculation", "osrns"… */
        List<String> products,
        LocalDate contractDateStart,
        LocalDate contractDateEnd,
        /* status_name: "ACTIVE", "CANCELLED"… */
        List<String> statuses,
        Long responsiblePersonId,
        /* branch_id: 1 = Алматы */
        List<Long> branches
) {

    /**
     * Формат дат в query. ДОПУЩЕНИЕ: в ответе даты "dd.MM.yyyy", а date-picker'ы обычно шлют ISO.
     * Сверить: выбрать период в SaqtaPro и посмотреть contract_date_start в URL (DevTools → Network).
     */
    public static final DateTimeFormatter DATE_PARAM = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Как открывается вкладка «Договоры»: без фильтров, 1-я страница по 10 */
    public static ContractListRequestBuilder noFilters() {
        return builder()
                .value("")
                .page(1)
                .perPage(10)
                .products(List.of())
                .statuses(List.of())
                .branches(List.of());
    }

    /** Параметры в том же порядке и виде, что шлёт фронт. Незаданные необязательные — не отправляются */
    public Map<String, Object> queryParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("value", value == null ? "" : value);
        params.put("page", page);
        params.put("per_page", perPage);
        params.put("product", jsonArray(products));
        params.put("contract_date_start", date(contractDateStart));
        params.put("contract_date_end", date(contractDateEnd));
        if (responsiblePersonId != null) {
            params.put("responsible_person_id", responsiblePersonId);
        }
        params.put("status_name", jsonArray(statuses));
        if (branches != null && !branches.isEmpty()) {
            // ДОПУЩЕНИЕ: видели только один филиал (branches=1). Как фронт шлёт несколько — сверить
            params.put("branches", branches.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        return params;
    }

    public static String date(LocalDate date) {
        return date == null ? "" : DATE_PARAM.format(date);
    }

    /** ["osrns", "pa_individual"] — как у фронта, с пробелом после запятой; пустой — [] */
    private static String jsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return values.stream()
                .map(v -> "\"" + v + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /** Коротко для шага Allure: только заданные фильтры, длинные списки — количеством */
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (value != null && !value.isEmpty()) parts.add("value=«" + value + "»");
        if (products != null && !products.isEmpty()) parts.add("product=" + shortList(products));
        if (statuses != null && !statuses.isEmpty()) parts.add("status=" + shortList(statuses));
        if (contractDateStart != null) parts.add("с " + date(contractDateStart));
        if (contractDateEnd != null) parts.add("по " + date(contractDateEnd));
        if (responsiblePersonId != null) parts.add("ответственный=" + responsiblePersonId);
        if (branches != null && !branches.isEmpty()) parts.add("филиал=" + branches);
        if (parts.isEmpty()) parts.add("без фильтров");
        return String.join(", ", parts) + ", page=" + page + "/per_page=" + perPage;
    }

    private static String shortList(List<?> list) {
        return list.size() > 3 ? list.size() + " шт." : list.toString();
    }
}
