package life.saqtapro.api.model.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Ответ GET /api/contracts: {"status": true, "data": {"data": [...договоры], "pagination": {...}}}.
 * Описаны поля, нужные для проверок фильтрации и пагинации; остальное игнорируется.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContractListResponse(
        @JsonProperty("status") boolean status,
        @JsonProperty("data") Page data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(
            @JsonProperty("data") List<Contract> contracts,
            @JsonProperty("pagination") Pagination pagination
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(
            @JsonProperty("current_page") int currentPage,
            @JsonProperty("per_page") int perPage,
            @JsonProperty("total") int total,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("first_page_url") String firstPageUrl,
            @JsonProperty("last_page_url") String lastPageUrl,
            /* null на последней странице */
            @JsonProperty("next_page_url") String nextPageUrl,
            /* null на первой странице */
            @JsonProperty("prev_page_url") String prevPageUrl,
            @JsonProperty("applied_filters") AppliedFilters appliedFilters
    ) {}

    /** Фильтры, которые backend реально применил — эхо запроса */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppliedFilters(
            @JsonProperty("value") String value,
            /* product_slug'и */
            @JsonProperty("product") List<String> product,
            @JsonProperty("contract_date_start") String contractDateStart,
            @JsonProperty("contract_date_end") String contractDateEnd,
            @JsonProperty("responsible_person_id") Long responsiblePersonId,
            @JsonProperty("status_name") List<String> statusName,
            /* Пока не видели в ответе — если backend его не возвращает, будет null */
            @JsonProperty("branches") Object branches
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contract(
            @JsonProperty("product_id") Long productId,
            @JsonProperty("product_slug") String productSlug,
            @JsonProperty("contract_number") String contractNumber,
            /* "11.11.2026" */
            @JsonProperty("contract_date") String contractDate,
            @JsonProperty("contract_date_timestamp") long contractDateTimestamp,
            /* "CANCELLED" — код; status_value — «Аннулирован» */
            @JsonProperty("status_name") String statusName,
            @JsonProperty("status_value") String statusValue,
            @JsonProperty("insured_bin") String insuredBin,
            @JsonProperty("insured_name") String insuredName,
            @JsonProperty("policyholder_bin") String policyholderBin,
            @JsonProperty("policyholder_name") String policyholderName,
            /* null у договоров, оформленных не сотрудником (например, ON… из кабинета) */
            @JsonProperty("responsible_person_id") Long responsiblePersonId,
            @JsonProperty("responsible_person_name") String responsiblePersonName,
            @JsonProperty("periodicity_name") String periodicityName,
            @JsonProperty("insurance_sum") Long insuranceSum,
            @JsonProperty("insurance_premium") Long insurancePremium,
            @JsonProperty("branch_id") Long branchId,
            @JsonProperty("branch_name_ru") String branchNameRu
    ) {

        private static final DateTimeFormatter RESPONSE_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        public LocalDate date() {
            return LocalDate.parse(contractDate, RESPONSE_DATE);
        }

        /** Для сообщений об ошибках: только поля, по которым идёт фильтрация */
        @Override
        public String toString() {
            return contractNumber + " от " + contractDate + ", product=" + productSlug
                    + ", status=" + statusName + ", ответственный=" + responsiblePersonId
                    + ", филиал=" + branchId
                    + ", страхователь=" + policyholderBin + " " + policyholderName;
        }
    }
}
