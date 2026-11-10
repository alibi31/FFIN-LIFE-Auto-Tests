package life.saqtapro.data;

import life.saqtapro.api.model.client.ClientSearchRequest.Filter;

import java.util.List;

/**
 * Справочники и данные для поиска клиентов SaqtaPro на dev-стенде.
 *
 * Эталонные клиенты не генерируются: dev подключён к боевой АИС, а ИИН/БИН проходят проверку по гос. базе,
 * поэтому опираемся на существующие карточки. Их нельзя удалять и редактировать на стенде.
 */
public final class ClientSearchFixtures {

    private ClientSearchFixtures() {}

    // Типы клиентов
    public static final String INDIVIDUAL = "Физ. лицо";
    public static final String LEGAL_ENTITY = "Юр. лицо";
    public static final String SOLE_PROPRIETOR = "ИП";

    public static final List<String> CLIENT_TYPES = List.of(INDIVIDUAL, LEGAL_ENTITY, SOLE_PROPRIETOR);

    /** Все филиалы (name_ru), как в фильтре SaqtaPro */
    public static final List<String> BRANCHES = List.of(
            "Аксай", "Актау", "Актобе", "Алматы", "Астана", "Атырау", "ГО",
            "Жанаозен", "Караганда", "Кокшетау", "Костанай", "Кульсары",
            "Кызылорда", "Павлодар", "Петропавловск", "Семей", "Талдыкорган",
            "Тараз", "Туркестан", "Уральск", "Усть-Каменогорск", "Шымкент"
    );

    /** Ответственные лица (id менеджеров) из фильтра SaqtaPro на dev-стенде */
    public static final List<Long> MANAGERS = List.of(681L, 338L, 1425L, 977L, 20L, 372L, 421L, 387L, 394L, 542L);

    /** Значение, которого заведомо нет ни в ФИО, ни в ИИН/БИН */
    public static final String NON_EXISTENT_VALUE = "ЪЪЪ Несуществующий Клиент";

    /**
     * Эталонный клиент стенда. Узнаём его в выдаче по bin (ИИН/БИН): у ИП и юр. лиц в ответе нет id.
     *
     * @param name ФИО или название, как в карточке
     */
    public record RefClient(String bin, String name, String clientType, String branch, long managerId) {}

    /** Физ. лицо, Алматы. Он же клиент из исходного curl-запроса */
    public static final RefClient TABYLOV = new RefClient(
            "600207301435", "ТАБЫЛОВ НУРЛАН УТЕУОВИЧ", INDIVIDUAL, "Алматы", 421L);

    /** ИП, Астана */
    public static final RefClient ABILKHADIROV = new RefClient(
            "870203350634", "АБИЛХАДИРОВ САЯТ ЖАРАСОВИЧ", SOLE_PROPRIETOR, "Астана", 681L);

    /** Юр. лицо, Алматы */
    public static final RefClient POWER_SPORTS = new RefClient(
            "120240024018", "ТОО \"POWER SPORTS\"", LEGAL_ENTITY, "Алматы", 372L);

    /** Фильтр «всё включено»: все типы, все филиалы, менеджеры стенда, 1-я страница по 20 */
    public static Filter.FilterBuilder allFilters() {
        return Filter.builder()
                .perPage(20)
                .page(1)
                .clientType(CLIENT_TYPES)
                .statuses(List.of())
                .branches(BRANCHES)
                .managers(MANAGERS);
    }
}
