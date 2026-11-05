package life.saqtapro.data;

import life.utils.data.TestPersonFactory;

import java.util.stream.Stream;

/** Невалидные ИИН для негативных проверок. Валидные даёт life.utils.data.IinGenerator. */
public final class Iins {

    private Iins() {}

    /** 12 цифр, но контрольная цифра неверна */
    public static String withBadChecksum() {
        String valid = TestPersonFactory.adult().iin();
        int check = Character.getNumericValue(valid.charAt(11));
        return valid.substring(0, 11) + ((check + 1) % 10);
    }

    /** Набор форматно неверных значений для параметризованного теста */
    public static Stream<String> malformed() {
        return Stream.of(
                "",
                "12345",                 // короче 12
                "1234567890123",         // длиннее 12
                "90010150012A",          // буква
                "900101 500123",         // пробел
                "901301500123",          // 13-й месяц
                withBadChecksum()
        );
    }
}
