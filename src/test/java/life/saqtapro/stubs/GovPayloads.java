package life.saqtapro.stubs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import life.saqtapro.api.model.Statuses;
import life.saqtapro.data.Phones;
import life.utils.data.TestPerson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-ответы гос. сервисов, собранные из TestPerson. Строятся в коде, а не лежат статичными
 * файлами: так ответ всегда согласован с ИИН и телефоном конкретного теста.
 * Когда появятся JSON-схемы БМГ/eGov, ответы стоит валидировать по ним (см. README).
 */
final class GovPayloads {

    private static final ObjectMapper JSON = new ObjectMapper();

    private GovPayloads() {}

    static String bmgRegistered(String phone, boolean registered) {
        return json(Map.of("phone", Phones.digits(phone), "registered", registered));
    }

    static String phoneOwnership(TestPerson person, boolean matched) {
        return json(Map.of("iin", person.iin(), "phone", Phones.digits(person.phone()), "matched", matched));
    }

    static String consent(String iin, String status) {
        return json(Map.of("consentId", GovPaths.consentIdFor(iin), "status", status));
    }

    static String person(TestPerson person, TestPerson relative, String iban) {
        return person(person, person.iin(), relative, iban);
    }

    /** reportedIin позволяет смоделировать подмену: eGov вернул данные другого человека */
    static String person(TestPerson person, String reportedIin, TestPerson relative, String iban) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("iin", reportedIin);
        body.put("fullName", person.fullName());
        body.put("birthDate", person.birthDateStr());
        body.put("gender", person.gender().name());
        body.put("address", person.address());
        body.put("document", Map.of(
                "type", "ID_CARD",
                "number", person.document().number(),
                "issuedBy", person.document().authority(),
                "issueDate", person.document().issueDateStr()));
        body.put("relatives", relative == null ? List.of() : List.of(Map.of(
                "relation", "SPOUSE",
                "iin", relative.iin(),
                "fullName", relative.fullName())));
        if (iban != null) {
            body.put("iban", iban);
        }
        return json(body);
    }

    /** Ответ без адреса и родственников — для проверки частичного автозаполнения */
    static String personPartial(TestPerson person) {
        return json(Map.of(
                "iin", person.iin(),
                "fullName", person.fullName(),
                "birthDate", person.birthDateStr()));
    }

    static String screening(String iin, String result, List<Statuses.WatchList> lists) {
        return json(Map.of(
                "iin", iin,
                "result", result,
                "matchedLists", lists.stream().map(Enum::name).toList()));
    }

    private static String json(Object o) {
        try {
            return JSON.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
