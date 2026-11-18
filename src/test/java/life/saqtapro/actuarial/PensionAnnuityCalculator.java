package life.saqtapro.actuarial;

import life.saqtapro.api.model.annuity.PensionAnnuityCalculationRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Эталонный расчёт пенсионного аннуитета — перенос формул калькулятора актуариев
 * ("2026_11_06 ... v2.xlsx", лист "ПА-Алгоритм для АИС") на Java. Тест сравнивает ответ API с этим расчётом.
 *
 * Ссылки на ячейки Excel оставлены в комментариях: при новой версии калькулятора сверяйте по ним.
 * Пока поддержан только немедленный аннуитет при ОППВ за 60+ месяцев (выплаты начинаются с даты договора).
 */
public final class PensionAnnuityCalculator {

    // Справочник актуария (S17:V30)
    private static final LocalDate RATES_2026_FROM = LocalDate.of(2026, 2, 15);
    private static final double PAYOUT_EXPENSE = 0.03;          // γ, V21
    private static final double PREMIUM_EXPENSE = 0.015;        // β, V19 (немедленный) = β2, V25 (отложенный)
    private static final long SUBSISTENCE_MINIMUM_2026 = 50_851; // ПМ на 2026, V30
    private static final int OPPV_MIN_MONTHS = 60;              // AD16 >= 60
    private static final int OPPV_PAYOUT_AGE = 50;              // BT10
    private static final int MALE_PAYOUT_AGE = 55;              // BS10

    private PensionAnnuityCalculator() {}

    public static Quote calculate(PensionAnnuityCalculationRequest request) {
        LocalDate birthDate = LocalDate.parse(request.insured().birthDate());
        LocalDate contractStart = LocalDate.parse(request.contractStartDate());
        boolean female = "FEMALE".equals(request.insured().gender());
        int guaranteedYears = request.guaranteedPeriodYears();

        var oppv = request.mandatoryProfessionalPensionContributions();
        boolean oppvEligible = oppv != null && oppv.amount() > 0 && oppv.months() >= OPPV_MIN_MONTHS;   // R122
        MortalityTable table = MortalityTable.select(request.disability(), oppvEligible);

        LocalDate paymentStart = paymentStartDate(birthDate, contractStart, female, oppvEligible);       // AC6
        int ageAtPaymentStart = fullYears(birthDate, paymentStart);                                      // AC7
        int currentAge = fullYears(birthDate, contractStart);                                            // AC8
        int deferredYears = ageAtPaymentStart - currentAge;

        // Ставки зависят от даты начала действия (V18, Y11)
        boolean newRates = !contractStart.isBefore(RATES_2026_FROM);
        double investmentReturn = newRates ? 0.09 : 0.08;
        double indexation = newRates ? 0.08 : 0.07;
        double i = (1 + investmentReturn) / (1 + indexation) - 1;                                        // AG11
        double v = 1 / (1 + i);                                                                          // J4

        // Коммутационные числа Dx, Nx (A7:Q117)
        double[] d = new double[MortalityTable.MAX_AGE + 1];
        double l = 1_000_000;
        for (int age = 0; age <= MortalityTable.MAX_AGE; age++) {
            d[age] = Math.pow(v, age) * l;
            l -= l * table.qx(age, female);
        }
        double[] n = new double[MortalityTable.MAX_AGE + 2];
        for (int age = MortalityTable.MAX_AGE; age >= 0; age--) {
            n[age] = n[age + 1] + d[age];
        }

        double annuityCertain = (1 - 1 / Math.pow(1 + i, guaranteedYears)) * (1 + i) / i;              // AG13
        double annualNet = annuityCertain + n[ageAtPaymentStart + guaranteedYears] / d[ageAtPaymentStart]; // AG14
        double monthlyNet = annualNet - 11.0 / 24;                                                       // AG15
        double withSurvival = monthlyNet * Math.pow(v, deferredYears);                                   // AG16, AG17
        double grossFactor = withSurvival * (1 + PAYOUT_EXPENSE) / (1 - PREMIUM_EXPENSE);                // AG18
        double indexationToStart = Math.pow(1 + indexation, deferredYears);                              // AG19

        long premium = request.totalPremium();                                                           // Z13
        long monthlyPayment = round(premium / grossFactor / 12 * indexationToStart);                     // AG22
        long minimalPension = (long) Math.ceil(minimalPensionBase(contractStart) * 0.7);                 // V22
        // Excel округляет до копеек (AG21), API отдаёт целые тенге
        long minimumPremium = round(round2(grossFactor * minimalPension * 12));

        List<Payment> schedule = schedule(birthDate, contractStart, paymentStart, monthlyPayment, indexation);

        return new Quote(table, investmentReturn, indexation, grossFactor, paymentStart, ageAtPaymentStart,
                premium, monthlyPayment, minimalPension, minimumPremium, premium >= minimumPremium, schedule);
    }

    /** AH26:AL746 — график до 110 лет, индексация раз в год в месяц начала выплат */
    private static List<Payment> schedule(LocalDate birthDate, LocalDate contractStart, LocalDate paymentStart,
                                          long firstPayment, double indexation) {
        LocalDate endOfLife = birthDate.plusYears(MortalityTable.MAX_AGE);                               // AD11
        long lastIndex = ChronoUnit.MONTHS.between(paymentStart, endOfLife);                             // AD13 (DATEDIF "M")
        List<Payment> payments = new ArrayList<>();
        double amount = 0;   // Excel индексирует неокруглённую сумму, округляет только при выводе (AL)
        for (int k = 0; k <= lastIndex; k++) {
            LocalDate date = contractStart.plusMonths(k);                                                // EDATE
            if (date.isBefore(paymentStart)) {
                amount = 0;
            } else if (amount == 0) {
                amount = firstPayment;
            } else if (date.getMonth() == paymentStart.getMonth()) {
                amount *= 1 + indexation;
            }
            payments.add(new Payment(date, round(amount)));
        }
        return Collections.unmodifiableList(payments);
    }

    /** AC6: немедленный аннуитет или отложенный до 50 (ОППВ) / 55 (мужчины) / таблицы для женщин */
    private static LocalDate paymentStartDate(LocalDate birthDate, LocalDate contractStart,
                                              boolean female, boolean oppvEligible) {
        if (oppvEligible) {
            return earliestPayout(birthDate, contractStart, OPPV_PAYOUT_AGE);                            // BT7
        }
        if (!female) {
            return earliestPayout(birthDate, contractStart, MALE_PAYOUT_AGE);                            // BS7
        }
        // BR7: для женщин возраст по таблице T4:U13 с шагом в полгода — в эталон пока не перенесено
        throw new UnsupportedOperationException("Отложенный аннуитет для женщин без ОППВ в эталонном расчёте не реализован");
    }

    /** BS7:BS10 / BT7:BT10 */
    private static LocalDate earliestPayout(LocalDate birthDate, LocalDate contractStart, int payoutAge) {
        LocalDate ageReached = birthDate.plusYears(payoutAge);
        // DATE(год, месяц, день начала договора): лишние дни Excel переносит на следующий месяц
        LocalDate sameDay = ageReached.withDayOfMonth(1).plusDays(contractStart.getDayOfMonth() - 1L);
        LocalDate candidate;
        if (sameDay.isBefore(ageReached)) {
            candidate = sameDay.plusMonths(1);
        } else if (sameDay.getMonth() != ageReached.getMonth()) {
            candidate = ageReached.withDayOfMonth(ageReached.lengthOfMonth());
        } else {
            candidate = sameDay;
        }
        return candidate.isBefore(contractStart) ? contractStart : candidate;
    }

    /** V22: 70% прожиточного минимума года заключения */
    private static long minimalPensionBase(LocalDate contractStart) {
        if (contractStart.getYear() != 2026) {
            throw new IllegalStateException("Прожиточный минимум в эталоне есть только на 2026 год. "
                    + "Возьмите ПМ на " + contractStart.getYear() + " из новой версии калькулятора актуариев (V30)");
        }
        return SUBSISTENCE_MINIMUM_2026;
    }

    /**
     * Полных лет. В Excel AC7/AC8 — ROUNDDOWN(YEARFRAC(...)) по базису 30/360: в пару дней
     * около дня рождения может разойтись на год. Тестовые даты рождения берите не на границе.
     */
    private static int fullYears(LocalDate birthDate, LocalDate on) {
        return Period.between(birthDate, on).getYears();
    }

    /** Excel ROUND(x; 0) для положительных чисел */
    private static long round(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public record Payment(LocalDate date, long amount) {
        @Override
        public String toString() {
            return date + " → " + amount;
        }
    }

    public record Quote(
            MortalityTable mortalityTable,
            double investmentReturn,
            double indexation,
            double grossAnnuityFactor,
            LocalDate paymentStartDate,
            int ageAtPaymentStart,
            long premium,
            long monthlyPayment,
            long minimalPension,
            long minimumPremium,
            boolean premiumIsEnough,
            List<Payment> schedule
    ) {
        public boolean immediate(LocalDate contractStart) {
            return paymentStartDate.equals(contractStart);
        }

        /** Для вложения в Allure: по этим числам удобно сверяться с Excel */
        public String describe() {
            return String.join("\n",
                    "Таблица смертности: " + mortalityTable,
                    "Доходность i: " + investmentReturn + ", индексация j: " + indexation,
                    "Аннуитетный фактор брутто (AG18): " + grossAnnuityFactor,
                    "Начало выплат (AC6): " + paymentStartDate + ", возраст (AC7): " + ageAtPaymentStart,
                    "Совокупная премия (Z13): " + premium,
                    "Ежемесячная выплата (AG22): " + monthlyPayment,
                    "Минимальная пенсия (V22): " + minimalPension,
                    "Минимальная премия (AG21): " + minimumPremium,
                    "Премии достаточно: " + premiumIsEnough,
                    "Выплат в графике: " + schedule.size()
                            + " (" + schedule.get(0) + " … " + schedule.get(schedule.size() - 1) + ")");
        }
    }
}
