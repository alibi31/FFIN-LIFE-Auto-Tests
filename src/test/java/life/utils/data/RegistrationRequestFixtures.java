package life.utils.data;

import life.cabinet.api.model.education.EducationRegistrationRequest;
import life.cabinet.api.model.first.FirstRegistrationRequest;
import life.cabinet.api.model.health.HealthRegistrationRequest;
import life.cabinet.api.model.jurek.JurekRegistrationRequest;

public final class RegistrationRequestFixtures {

    private RegistrationRequestFixtures() {}

    // ---------------- Health ----------------

    public static HealthRegistrationRequest.Document healthDoc(TestPerson p) {
        return HealthRegistrationRequest.Document.builder()
                .number(p.document().number())
                .type(p.document().type())
                .authority(p.document().authority())
                .active(true)
                .isDisabled(true)
                .date(p.document().issueDateStr())
                .build();
    }

    public static HealthRegistrationRequest defaultHealthRequest(TestPerson insurer) {
        HealthRegistrationRequest.Insurer i = HealthRegistrationRequest.Insurer.builder()
                .fullName(insurer.fullName()).uin(insurer.iin()).gender(insurer.genderCode())
                .phone(insurer.phone()).birthDate(insurer.birthDateStr())
                .address(insurer.address()).email(insurer.email()).doc(healthDoc(insurer))
                .build();

        HealthRegistrationRequest.Insured ins = HealthRegistrationRequest.Insured.builder()
                .fullName(insurer.fullName()).uin(insurer.iin()).gender(insurer.genderCode())
                .phone(insurer.phone()).birthDate(insurer.birthDateStr())
                .address(insurer.address()).email(insurer.email()).doc(healthDoc(insurer))
                .build();

        return HealthRegistrationRequest.builder()
                .program(7).period(1463)
                .insurer(i).insured(ins)
                .agreement1(true).agreement2(true).agreement3(false)
                .agent("").nomad("").promo("")
                .build();
    }

    // ---------------- Jurek ----------------

    public static JurekRegistrationRequest.Document jurekDoc(TestPerson p) {
        return JurekRegistrationRequest.Document.builder()
                .type(p.document().type()).number(p.document().number())
                .authority(p.document().authority()).date(p.document().issueDateStr())
                .build();
    }

    public static JurekRegistrationRequest defaultJurekRequest(TestPerson insurer, TestPerson beneficiary) {
        JurekRegistrationRequest.Insurer ir = JurekRegistrationRequest.Insurer.builder()
                .uin(insurer.iin()).fullName(insurer.fullName()).gender(insurer.genderCode())
                .phone(insurer.phone()).birthDate(insurer.birthDateStr())
                .address(insurer.address()).email(insurer.email()).doc(jurekDoc(insurer))
                .build();

        JurekRegistrationRequest.Insured isd = JurekRegistrationRequest.Insured.builder()
                .uin(insurer.iin()).fullName(insurer.fullName()).gender(insurer.genderCode())
                .phone(insurer.phone()).birthDate(insurer.birthDateStr())
                .address(insurer.address()).email(insurer.email()).doc(jurekDoc(insurer))
                .build();

        JurekRegistrationRequest.Beneficiary b = JurekRegistrationRequest.Beneficiary.builder()
                .uin(beneficiary.iin()).fullName(beneficiary.fullName()).gender(beneficiary.genderCode())
                .phone(beneficiary.phone()).birthDate(beneficiary.birthDateStr())
                .address(beneficiary.address()).email(beneficiary.email()).doc(jurekDoc(beneficiary))
                .build();

        return JurekRegistrationRequest.builder()
                .insurer(ir).insured(isd).beneficiary(b)
                .notificationMethod(2).paymentPeriodicity(3).guaranteedPeriod("2")
                .incomingAmount(100000).premiumCalculation(false)
                .bankId("407036346").iban("KZ84551A600000218365")
                .usd(true).periodAnnuityPaymentsYears(5)
                .build();
    }

    // ---------------- First ----------------

    public static FirstRegistrationRequest.Document firstDoc(TestPerson p) {
        return FirstRegistrationRequest.Document.builder()
                .number(p.document().number()).type(p.document().type())
                .authority(p.document().authority()).active(true).isDisabled(true)
                .date(p.document().issueDateStr())
                .build();
    }

    public static FirstRegistrationRequest defaultFirstRequest(TestPerson insured, TestPerson beneficiary) {
        FirstRegistrationRequest.Insured ins = FirstRegistrationRequest.Insured.builder()
                .uin(insured.iin()).fullName(insured.fullName()).gender(insured.genderCode())
                .birthDate(insured.birthDateStr()).phone(insured.phone())
                .email(insured.email()).address(insured.address())
                .isNotKzResident(false).doc(firstDoc(insured))
                .build();

        FirstRegistrationRequest.Beneficiary b = FirstRegistrationRequest.Beneficiary.builder()
                .uin(beneficiary.iin()).fullName(beneficiary.fullName()).gender(beneficiary.genderCode())
                .birthDate(beneficiary.birthDateStr()).phone(beneficiary.phone())
                .email(beneficiary.email()).address(beneficiary.address())
                .isNotKzResident(false).doc(firstDoc(beneficiary))
                .build();

        return FirstRegistrationRequest.builder()
                .insurancePremium(250).insurancePremiumKzt(126763)
                .notificationMethod(2).icSum(250).term(5).agentFullName("")
                .insured(ins).beneficiary(b)
                .build();
    }

    // ---------------- Education ----------------

    public static EducationRegistrationRequest.Document educationDoc(TestPerson p) {
        return EducationRegistrationRequest.Document.builder()
                .number(p.document().number()).type(p.document().type())
                .authority(p.document().authority()).date(p.document().issueDateStr())
                .build();
    }

    public static EducationRegistrationRequest defaultEducationRequest(TestPerson parent, TestPerson child) {
        EducationRegistrationRequest.Parent p = EducationRegistrationRequest.Parent.builder()
                .uin(parent.iin()).fullName(parent.fullName()).gender(parent.genderCode())
                .birthDate(parent.birthDateStr()).phone(parent.phone())
                .email(parent.email()).address(parent.address()).doc(educationDoc(parent))
                .build();

        EducationRegistrationRequest.Child c = EducationRegistrationRequest.Child.builder()
                .uin(child.iin()).fullName(child.fullName()).gender(child.genderCode())
                .phone(child.phone()).birthDate(child.birthDateStr())
                .email(child.email()).address(child.address()).doc(educationDoc(child))
                .build();

        EducationRegistrationRequest.Options options = EducationRegistrationRequest.Options.builder()
                .deathDueToAccident(false).disabilityDueToAccident(false).bodilyInjury(false)
                .criticalIllness(false).temporaryIncapacityForWork(false)
                .hospitalizationAsResultOfAccident(false)
                .childBodilyInjury(false).childCriticalIllness(false)
                .build();

        return EducationRegistrationRequest.builder()
                .period(4).term(3).premium(15000).notificationMethod(2)
                .parent(p).child(c).options(options)
                .build();
    }
}