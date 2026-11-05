package life.saqtapro.stubs;

/**
 * Пути внешних сервисов, которые SaqtaPro вызывает и которые подменяет WireMock.
 * Префикс = сервис: SaqtaPro в профиле autotest получает BMG_BASE_URL=http://wiremock:8080/bmg и т.д.
 * Пути и форматы — допущение до получения спецификаций интеграций; правятся здесь и в GovPayloads.
 */
public final class GovPaths {

    private GovPaths() {}

    /** БМГ: есть ли номер в Базе мобильных граждан */
    public static final String BMG_SUBSCRIBER_CHECK = "/bmg/v1/subscribers/check";

    /** eGov: принадлежит ли номер этому ИИН */
    public static final String EGOV_PHONE_OWNERSHIP = "/egov/v1/phone-ownership";

    /** eGov: отправка SMS-запроса на согласие и опрос его статуса */
    public static final String EGOV_CONSENTS = "/egov/v1/consents";
    public static final String EGOV_CONSENT_PREFIX = "/egov/v1/consents/";

    /** eGov: персональные данные, родственные связи, IBAN — доступны после согласия */
    public static final String EGOV_PERSON_PREFIX = "/egov/v1/persons/";

    /** Комплаенс-провайдер: проверка по спискам */
    public static final String COMPLIANCE_SCREENING = "/compliance/v1/screening";

    public static String consentIdFor(String iin) {
        return "consent-" + iin;
    }
}
