package life.utils.data;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TestPersonFactory {

    private static final List<String> MALE_LAST  = List.of("Алиев", "Смагулов", "Жумабеков", "Ахметов", "Макаров", "Бородин", "Самохвалов", "Орлов");
    private static final List<String> MALE_FIRST = List.of("Алишер", "Арман", "Нұрлан", "Тимур", "Николай", "Владимир", "Алексей", "Александр");
    private static final List<String> MALE_PATR  = List.of("Серикович", "Асқарұлы", "Мұратұлы", "Иванович", "Викторович", "Николаевич", "Григорьевич", "Игоревич");

    private static final List<String> FEMALE_LAST  = List.of("Жапбарова", "Ахметова", "Акмади", "Исаева", "Султанова", "Григорьева", "Соколова", "Смирнова");
    private static final List<String> FEMALE_FIRST = List.of("Аружан", "Айгерим", "Динара", "Анастасия", "Асем", "Айнур", "Оксана", "Галина");
    private static final List<String> FEMALE_PATR  = List.of("Абылғазиновна", "Асқарқызы", "Нұрланқызы", "Александровна", "Алексеевна", "Павловна", "Кайраткызы", "Айбекқызы");

    private static final List<String> ADDRESSES = List.of(
            "Алматы, проспект Aль-Фараби 40",
            "Астана, Улица Кабанбай батыра, 15",
            "Шымкент, Улица Тауке хана, 45"
    );

    private static final List<String> AUTHORITIES = List.of("МВД РК", "МЮ РК");

    private TestPersonFactory() {}

    /** Взрослый застрахованный/страхователь, 25–55 лет. */
    public static TestPerson adult() {
        return person(randomAge(25, 55));
    }

    /** Для pension-annuity сценариев, где нужен предпенсионный возраст. */
    public static TestPerson elder() {
        return person(randomAge(56, 63));
    }

    /** Ребёнок для Education/Jurek-подобных продуктов. */
    public static TestPerson child() {
        return person(randomAge(5, 15));
    }

    public static TestPerson person(int age) {
        boolean male = ThreadLocalRandom.current().nextBoolean();
        IinGenerator.Gender gender = male ? IinGenerator.Gender.MALE : IinGenerator.Gender.FEMALE;
        LocalDate birthDate = LocalDate.now()
                .minusYears(age)
                .minusDays(ThreadLocalRandom.current().nextInt(0, 365));

        String fullName = male
                ? "%s %s %s".formatted(pick(MALE_LAST), pick(MALE_FIRST), pick(MALE_PATR))
                : "%s %s %s".formatted(pick(FEMALE_LAST), pick(FEMALE_FIRST), pick(FEMALE_PATR));

        String iin = IinGenerator.generate(birthDate, gender);

        TestDocument doc = new TestDocument(
                randomDocNumber(),
                13310363,
                pick(AUTHORITIES),
                LocalDate.now().minusYears(ThreadLocalRandom.current().nextInt(1, 10)).toString()
        );

        return new TestPerson(
                iin,
                fullName,
                male ? 1 : 0,
                gender,
                birthDate,
                randomPhone(),
                "autotest+" + iin + "@example.com",
                pick(ADDRESSES),
                doc
        );
    }

    private static int randomAge(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static String randomPhone() {
        return "+7 (7%02d) %03d-%02d-%02d".formatted(
                ThreadLocalRandom.current().nextInt(0, 100),
                ThreadLocalRandom.current().nextInt(0, 1000),
                ThreadLocalRandom.current().nextInt(0, 100),
                ThreadLocalRandom.current().nextInt(0, 100));
    }

    private static String randomDocNumber() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100_000_000, 999_999_999));
    }

    private static <T> T pick(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}