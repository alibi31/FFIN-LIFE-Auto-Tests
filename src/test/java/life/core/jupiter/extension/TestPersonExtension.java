package life.core.jupiter.extension;

import life.core.jupiter.annotation.Person;
import life.utils.data.TestPerson;
import life.utils.data.TestPersonFactory;
import org.junit.jupiter.api.extension.*;

public class TestPersonExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType() == TestPerson.class && pc.isAnnotated(Person.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        Person.Role role = pc.findAnnotation(Person.class).map(Person::value).orElse(Person.Role.ADULT);
        return switch (role) {
            case ADULT -> TestPersonFactory.adult();
            case ELDER -> TestPersonFactory.elder();
            case CHILD -> TestPersonFactory.child();
        };
    }
}