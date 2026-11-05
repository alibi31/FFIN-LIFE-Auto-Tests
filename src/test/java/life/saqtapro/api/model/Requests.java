package life.saqtapro.api.model;

import java.math.BigDecimal;

/** Тела запросов SaqtaPro. Поля — допущение, сверить с контрактом backend. */
public final class Requests {

    private Requests() {}

    public record CreateClient(String iin, String phone, String verificationId) {}

    public record StartGovVerification(String iin, String phone) {}

    public record PolicyDraft(String clientId, String product, BigDecimal insuranceSum,
                              String currency, int termYears) {

        /** Сумма в пределах лимита агента: договор не уходит на андеррайтинг принудительно */
        public static PolicyDraft withinAgentLimit(String clientId) {
            return new PolicyDraft(clientId, "LIFE_TERM", new BigDecimal("5000000"), "KZT", 5);
        }

        /** Сумма выше лимита агента — требует оценки андеррайтера (порог подтвердить у бизнеса) */
        public static PolicyDraft aboveAgentLimit(String clientId) {
            return new PolicyDraft(clientId, "LIFE_TERM", new BigDecimal("150000000"), "KZT", 10);
        }
    }

    public record Decision(Statuses.Decision decision, String comment, Long version) {

        public static Decision approve(long version) {
            return new Decision(Statuses.Decision.APPROVE, "Риск приемлемый, автотест", version);
        }

        public static Decision reject(long version, String reason) {
            return new Decision(Statuses.Decision.REJECT, reason, version);
        }

        public static Decision requestInfo(long version, String what) {
            return new Decision(Statuses.Decision.REQUEST_INFO, what, version);
        }
    }
}
