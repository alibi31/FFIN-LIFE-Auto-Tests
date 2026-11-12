package life.saqtapro.data;

import life.saqtapro.api.model.contract.ContractListRequest;
import life.saqtapro.api.model.contract.ContractListRequest.ContractListRequestBuilder;

import java.util.List;

/**
 * Справочники фильтров вкладки «Договоры» SaqtaPro на dev-стенде — сняты с запроса,
 * когда в фильтрах выбрано всё. Появится новый продукт или статус — добавить сюда.
 */
public final class ContractListFixtures {

    private ContractListFixtures() {}

    /** Все продукты (product_slug), как в фильтре SaqtaPro */
    public static final List<String> PRODUCTS = List.of(
            "osrns", "pa_individual", "pa_joint", "future", "capital",
            "incoming_osrns", "pa_individual_calculation", "pac"
    );

    /** Все статусы договора (status_name), как в фильтре SaqtaPro */
    public static final List<String> STATUSES = List.of(
            "COMPLIANCE_CONTROL", "PLANNED", "ACTIVE", "CANCELLED", "COMPLETED", "TERMINATED",
            "CREATED", "TERMINATED_WITH_REFUND", "TERMINATED_WITHOUT_REFUND", "ON_APPROVAL", "WAITING"
    );

    /** Ответственное лицо и филиал из запроса браузера (Абдиева И. Д., Алматы) */
    public static final long BROWSER_RESPONSIBLE_PERSON = 1255L;
    public static final long ALMATY_BRANCH = 1L;

    /** Значение, которого заведомо нет ни в номере договора, ни в ИИН/БИН, ни в ФИО */
    public static final String NON_EXISTENT_VALUE = "ЪЪЪ Несуществующий Договор";
    public static final String NON_EXISTENT_PRODUCT = "non_existent_product";

    /** «Выбрано всё»: все продукты и все статусы, без периода, ответственного и филиала */
    public static ContractListRequestBuilder allFilters() {
        return ContractListRequest.noFilters()
                .products(PRODUCTS)
                .statuses(STATUSES);
    }
}
