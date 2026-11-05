package life.saqtapro.api;

/**
 * Все пути SaqtaPro API в одном месте. Пути — рабочее допущение: сверить со Swagger SaqtaPro
 * и поправить здесь, тесты при этом не меняются.
 */
public final class SaqtaEndpoints {

    private SaqtaEndpoints() {}

    /** Реальный эндпоинт SaqtaPro (dev-lins-refactor) */
    public static final String LOGIN = "/api/login";

    // Клиенты и комплаенс
    public static final String CLIENTS = "/api/v1/clients";
    public static final String CLIENT = "/api/v1/clients/{clientId}";
    public static final String CLIENT_COMPLIANCE = "/api/v1/clients/{clientId}/compliance";

    // Гос. верификация: БМГ → eGov (связка ИИН+телефон) → SMS-согласие → данные
    public static final String GOV_VERIFICATIONS = "/api/v1/gov/verifications";
    public static final String GOV_VERIFICATION = "/api/v1/gov/verifications/{verificationId}";

    // Договоры
    public static final String POLICIES = "/api/v1/policies";
    public static final String POLICY = "/api/v1/policies/{policyId}";
    public static final String POLICY_SUBMIT = "/api/v1/policies/{policyId}/submit-underwriting";
    public static final String POLICY_ISSUE = "/api/v1/policies/{policyId}/issue";
    public static final String POLICY_HISTORY = "/api/v1/policies/{policyId}/history";

    // Андеррайтинг
    public static final String UW_QUEUE = "/api/v1/underwriting/queue";
    public static final String UW_DECISION = "/api/v1/underwriting/policies/{policyId}/decision";
}
