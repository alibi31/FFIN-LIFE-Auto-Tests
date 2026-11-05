package life.saqtapro.api.model;

/**
 * Статусы SaqtaPro, на которые опираются тесты. Значения — допущение, сверить с backend.
 */
public final class Statuses {

    private Statuses() {}

    public enum Policy {
        DRAFT, UNDER_REVIEW, INFO_REQUESTED, APPROVED, REJECTED, BLOCKED, ISSUED
    }

    public enum Compliance {
        CLEAR, BLOCKED, MANUAL_REVIEW, PENDING
    }

    /** Итог цепочки БМГ → eGov → SMS-согласие → данные */
    public enum Verification {
        CONSENT_REQUESTED,
        COMPLETED,
        PHONE_NOT_REGISTERED,   // номера нет в БМГ
        PHONE_NOT_OWNED,        // eGov: номер не принадлежит ИИН
        PERSON_NOT_FOUND,
        CONSENT_DECLINED,
        CONSENT_EXPIRED,
        SMS_SEND_FAILED,
        GOV_SERVICE_UNAVAILABLE,
        DATA_MISMATCH;          // ИИН в ответе eGov не совпал с запрошенным

        public boolean isTerminal() {
            return this != CONSENT_REQUESTED;
        }
    }

    public enum Decision {
        APPROVE, REJECT, REQUEST_INFO
    }

    /** Списки, по которым идёт комплаенс-проверка */
    public enum WatchList {
        TERRORISTS_EXTREMISTS, SANCTIONS, INTERNAL_STOP_LIST
    }
}
