package life.saqtapro.data;

/** Нормализация телефонов: TestPersonFactory отдаёт "+7 (7xx) xxx-xx-xx", гос. сервисы ждут цифры. */
public final class Phones {

    private Phones() {}

    /** 77001234567 */
    public static String digits(String phone) {
        String d = phone.replaceAll("\\D", "");
        return d.startsWith("8") && d.length() == 11 ? "7" + d.substring(1) : d;
    }

    /** Последние 10 цифр — для матчинга в стабах независимо от формата (+7 / 8 / 7) */
    public static String last10(String phone) {
        String d = digits(phone);
        return d.substring(d.length() - 10);
    }
}
