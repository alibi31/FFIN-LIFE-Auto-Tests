package life.utils.data;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Генератор синтетических ИИН по алгоритму РК (дата рождения + век/пол + порядковый номер + чек-сумма).
 * Гарантирует, что backend, валидирующий формат/чек-сумму ИИН, не отклонит тестовые данные.
 */
public final class IinGenerator {

    private static final int[] W1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private static final int[] W2 = {3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2};

    private IinGenerator() {}

    public enum Gender {MALE, FEMALE}

    public static String generate(LocalDate birthDate, Gender gender) {
        String base11 = String.format("%02d%02d%02d%d%04d",
                birthDate.getYear() % 100,
                birthDate.getMonthValue(),
                birthDate.getDayOfMonth(),
                centuryGenderDigit(birthDate.getYear(), gender),
                ThreadLocalRandom.current().nextInt(1, 9999));

        int check = weightedSum(base11, W1) % 11;
        if (check == 10) {
            check = weightedSum(base11, W2) % 11;
            if (check == 10) {
                return generate(birthDate, gender); // редкий случай — перегенерируем хвост
            }
        }
        return base11 + check;
    }

    private static int centuryGenderDigit(int year, Gender gender) {
        int block = (year < 1900) ? 0 : (year < 2000 ? 1 : 2); // 1800e/1900e/2000e
        int base = 1 + block * 2;                              // 1,3,5 = male
        return gender == Gender.MALE ? base : base + 1;         // 2,4,6 = female
    }

    private static int weightedSum(String digits11, int[] weights) {
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            sum += Character.getNumericValue(digits11.charAt(i)) * weights[i];
        }
        return sum;
    }
}