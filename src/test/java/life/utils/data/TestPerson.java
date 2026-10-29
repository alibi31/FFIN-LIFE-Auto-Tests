package life.utils.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record TestPerson(
        String iin,
        String fullName,
        int genderCode,               // 1 = male, 0 = female — под конвенцию текущих *RegistrationRequest
        IinGenerator.Gender gender,
        LocalDate birthDate,
        String phone,
        String email,
        String address,
        TestDocument document
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String birthDateStr() {
        return birthDate.format(FMT);
    }
}