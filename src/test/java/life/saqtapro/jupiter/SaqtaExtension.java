package life.saqtapro.jupiter;

import life.saqtapro.api.SaqtaApi;
import life.saqtapro.config.Saqta;
import life.saqtapro.stubs.GovStubs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Внедряет в тест:
 *  - SaqtaApi с аннотацией @As(...) — клиент API от имени роли;
 *  - GovStubs — по одному экземпляру на тест; его стабы удаляются после теста автоматически.
 */
public class SaqtaExtension implements ParameterResolver {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(SaqtaExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        Class<?> type = pc.getParameter().getType();
        return (type == SaqtaApi.class && pc.isAnnotated(As.class)) || type == GovStubs.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        Class<?> type = pc.getParameter().getType();
        if (type == GovStubs.class) {
            return govStubs(ec);
        }
        if (type == SaqtaApi.class) {
            return pc.findAnnotation(As.class)
                    .map(as -> SaqtaApi.as(as.value()))
                    .orElseThrow(() -> new ParameterResolutionException("SaqtaApi требует @As(TestUser)"));
        }
        throw new ParameterResolutionException("Неподдерживаемый параметр " + type);
    }

    /** Один GovStubs на тест: JUnit закроет его (и удалит стабы) после завершения метода */
    private static GovStubs govStubs(ExtensionContext ec) {
        return ec.getStore(NS).getOrComputeIfAbsent(
                StubsResource.class,
                key -> new StubsResource(new GovStubs(Saqta.cfg().wiremockUrl())),
                StubsResource.class
        ).stubs();
    }

    private record StubsResource(GovStubs stubs) implements ExtensionContext.Store.CloseableResource {
        @Override
        public void close() {
            stubs.close();
        }
    }
}
