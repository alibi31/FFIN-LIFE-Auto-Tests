package life.saqtapro.api.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Ответ POST /api/search/client: {"status": true, "data": <Laravel-пагинатор>}.
 * Описаны только поля, нужные для проверок фильтрации; остальное (контакты, адреса, реквизиты) игнорируется.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientSearchResponse(
        @JsonProperty("status") boolean status,
        @JsonProperty("data") Page data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(
            @JsonProperty("current_page") int currentPage,
            @JsonProperty("data") List<Client> clients,
            /* null, если ничего не найдено */
            @JsonProperty("from") Integer from,
            @JsonProperty("to") Integer to,
            /* 1 даже при total = 0 */
            @JsonProperty("last_page") int lastPage,
            @JsonProperty("per_page") int perPage,
            @JsonProperty("total") int total,
            @JsonProperty("next_page_url") String nextPageUrl,
            @JsonProperty("prev_page_url") String prevPageUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Client(
            @JsonProperty("id") Long id,
            /* ИИН для физ. лиц, БИН для юр. лиц и ИП */
            @JsonProperty("bin") String bin,
            /* ФИО физ. лица */
            @JsonProperty("full_name") String fullName,
            /* Название юр. лица и ФИО ИП: у них нет full_name и id */
            @JsonProperty("name_ru") String nameRu,
            @JsonProperty("client_type") String clientType,
            @JsonProperty("manager") List<Manager> managers,
            @JsonProperty("branch") List<Branch> branches
    ) {

        /** Отображаемое имя клиента любого типа */
        public String name() {
            return fullName != null ? fullName : nameRu;
        }

        public List<Long> managerIds() {
            return managers == null ? List.of()
                    : managers.stream().map(Manager::id).filter(Objects::nonNull).toList();
        }

        public List<String> branchNames() {
            return branches == null ? List.of()
                    : branches.stream().map(Branch::nameRu).filter(Objects::nonNull).toList();
        }

        /** Для сообщений об ошибках: только поля, по которым идёт фильтрация */
        @Override
        public String toString() {
            return "bin=" + bin + ", client_type=" + clientType + ", branch=" + branchNames()
                    + ", managers=" + managerIds() + ", name=" + name();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Manager(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("branch") String branch
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Branch(
            @JsonProperty("id") Long id,
            @JsonProperty("name_ru") String nameRu,
            @JsonProperty("name_kz") String nameKz
    ) {}
}
