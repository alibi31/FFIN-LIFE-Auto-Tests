package life.saqtapro.data;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Синтетические IBAN Казахстана (KZ + 2 контрольные цифры + 3 код банка + 13 счёт, всего 20 символов)
 * с корректной контрольной суммой ISO 13616 (mod 97).
 */
public final class KzIban {

    private static final String TEST_BANK_CODE = "551";

    private KzIban() {}

    public static String valid() {
        StringBuilder account = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            account.append(ThreadLocalRandom.current().nextInt(10));
        }
        String bban = TEST_BANK_CODE + account;
        return "KZ" + checkDigits(bban) + bban;
    }

    /** Тот же IBAN с испорченной последней цифрой — контрольная сумма не сходится */
    public static String withBadChecksum() {
        String iban = valid();
        char last = iban.charAt(iban.length() - 1);
        char broken = last == '9' ? '0' : (char) (last + 1);
        return iban.substring(0, iban.length() - 1) + broken;
    }

    public static boolean isValid(String iban) {
        if (iban == null || !iban.matches("KZ\\d{2}[0-9A-Z]{16}")) {
            return false;
        }
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        return toNumber(rearranged).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    /** Маска, которую должен показывать UI внештатному агенту: KZ12 **** **** 3456 */
    public static String masked(String iban) {
        return iban.substring(0, 4) + " **** **** " + iban.substring(iban.length() - 4);
    }

    private static String checkDigits(String bban) {
        int mod = toNumber(bban + "KZ00").mod(BigInteger.valueOf(97)).intValue();
        return String.format("%02d", 98 - mod);
    }

    private static BigInteger toNumber(String s) {
        StringBuilder digits = new StringBuilder();
        for (char c : s.toCharArray()) {
            digits.append(Character.isLetter(c) ? String.valueOf(c - 'A' + 10) : String.valueOf(c));
        }
        return new BigInteger(digits.toString());
    }
}
