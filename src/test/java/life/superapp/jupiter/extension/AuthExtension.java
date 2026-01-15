package life.superapp.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import life.superapp.api.service.AuthService;
import life.superapp.api.service.impl.AuthApiService;
import life.superapp.jupiter.annotation.AccessToken;
import life.superapp.jupiter.annotation.Auth;
import life.superapp.jupiter.annotation.OneTimeToken;
import life.core.web.UiSession;
import life.utils.config.EnvConfig;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;


public class AuthExtension implements BeforeEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(AuthExtension.class);
    private static final Duration TTL = Duration.ofMinutes(50);

    private final AuthService authClient = new AuthApiService();

    private record Tokens(String access, Instant createdAt) {}

    @Override
    public void beforeEach(ExtensionContext ctx) throws Exception {

        Optional<Auth> authOpt = findAuth(ctx);
        if (authOpt.isEmpty()) return;

        Auth auth = authOpt.get();


        Parameter[] params = ctx.getRequiredTestMethod().getParameters();
        boolean needOtt    = Arrays.stream(params).anyMatch(p -> p.isAnnotationPresent(OneTimeToken.class) || p.isAnnotationPresent(AccessToken.class));
        boolean needAccess = Arrays.stream(params).anyMatch(p -> p.isAnnotationPresent(AccessToken.class));

        if (!needOtt) return;

        String iin = auth.iin();
        String fullName = auth.fullName();

        if (!needAccess) {
            String freshOtt = getOttSafely(iin, fullName);
            ctx.getStore(NS).put(methodOttKey(ctx), freshOtt); // method-scope
            return;
        }

        ExtensionContext root = ctx.getRoot();
        String accKey = accessKey(iin, fullName);
        Tokens cached = root.getStore(NS).get(accKey, Tokens.class);
        if (cached == null || isExpired(cached)) {
            String freshOtt = getOttSafely(iin, fullName);
            String access   = getAccessSafely(freshOtt);
            cached = new Tokens(access, Instant.now());
            root.getStore(NS).put(accKey, cached);
        }

        UiSession.setAccessToken(cached.access());
        injectAccessIntoSessionStorageIfPossible(cached.access());
    }

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ctx) throws ParameterResolutionException {
        return pc.isAnnotated(OneTimeToken.class) || pc.isAnnotated(AccessToken.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ctx) throws ParameterResolutionException {
        Auth auth = findAuth(ctx).orElseThrow(() -> new ParameterResolutionException("@Auth is missing on test method/class"));

        if (pc.isAnnotated(OneTimeToken.class)) {
            String ott = ctx.getStore(NS).get(methodOttKey(ctx), String.class);
            if (ott == null || ott.isBlank()) {
                ott = getOttSafely(auth.iin(), auth.fullName());
                ctx.getStore(NS).put(methodOttKey(ctx), ott);
            }
            return ott;
        }

        if (pc.isAnnotated(AccessToken.class)) {
            Tokens entry = ctx.getRoot().getStore(NS).get(accessKey(auth.iin(), auth.fullName()), Tokens.class);
            if (entry == null || isExpired(entry)) {
                String freshOtt = getOttSafely(auth.iin(), auth.fullName());
                String access   = getAccessSafely(freshOtt);
                entry = new Tokens(access, Instant.now());
                ctx.getRoot().getStore(NS).put(accessKey(auth.iin(), auth.fullName()), entry);
            }
            UiSession.setAccessToken(entry.access());
            injectAccessIntoSessionStorageIfPossible(entry.access());
            return entry.access();
        }

        throw new ParameterResolutionException("Unsupported parameter");
    }


    private Optional<Auth> findAuth(ExtensionContext ctx) {
        return AnnotationSupport.findAnnotation(ctx.getRequiredTestMethod(), Auth.class)
                .or(() -> AnnotationSupport.findAnnotation(ctx.getRequiredTestClass(), Auth.class));
    }

    private boolean isExpired(Tokens e) {
        return e == null || Instant.now().isAfter(e.createdAt().plus(TTL));
    }

    private String methodOttKey(ExtensionContext ctx) {
        return ctx.getUniqueId() + ":ott";
    }

    private String envBaseUrl() {
        return EnvConfig.cfg().apiSuperAppUrl();
    }

    private String accessKey(String iin, String fullName) {
        return envBaseUrl() + "|" + iin + "|" + fullName + "|access";
    }

    private String getOttSafely(String iin, String fullName) {
        try {
            return authClient.getOtt(iin, fullName);
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Failed to obtain one-time token", e);
        }
    }

    private String getAccessSafely(String ott) {
        try {
            return authClient.getAccessToken(ott);
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Failed to obtain access token", e);
        }
    }

    private void injectAccessIntoSessionStorageIfPossible(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return;
        BrowserContext context = UiSession.context();
        if (context == null) return;
        context.addInitScript("window.sessionStorage.setItem('accessToken','" + accessToken + "')");
    }
}
