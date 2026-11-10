package life.saqtapro.api.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Тело POST /api/search/client. Структура снята с реального запроса SaqtaPro (dev-saqta):
 * <pre>{"data": {"value": "...", "filter": {"perPage", "page", "client_type", "statuses", "branches", "managers"}}}</pre>
 * Пустые client_type / branches означают «ничего не найдено», а не «без фильтра».
 */
public record ClientSearchRequest(
        @JsonProperty("data") Data data
) {

    public static ClientSearchRequest of(String value, Filter filter) {
        return new ClientSearchRequest(new Data(value, filter));
    }

    public record Data(
            /* ФИО, ИИН или БИН */
            @JsonProperty("value") String value,
            @JsonProperty("filter") Filter filter
    ) {}

    @Builder(toBuilder = true)
    public record Filter(
            @JsonProperty("perPage") int perPage,
            @JsonProperty("page") int page,
            /* "Физ. лицо", "Юр. лицо", "ИП" */
            @JsonProperty("client_type") List<String> clientType,
            @JsonProperty("statuses") List<String> statuses,
            /* name_ru филиала: "Алматы", "Астана"… */
            @JsonProperty("branches") List<String> branches,
            /* id ответственного лица, создавшего карточку клиента */
            @JsonProperty("managers") List<Long> managers
    ) {}

    /** Коротко для шага Allure: без длинных списков филиалов и менеджеров */
    @Override
    public String toString() {
        Filter f = data.filter();
        return "value=«" + data.value() + "», client_type=" + f.clientType()
                + ", branches=" + (f.branches().size() > 3 ? f.branches().size() + " шт." : f.branches())
                + ", managers=" + f.managers()
                + ", page=" + f.page() + "/perPage=" + f.perPage();
    }
}
