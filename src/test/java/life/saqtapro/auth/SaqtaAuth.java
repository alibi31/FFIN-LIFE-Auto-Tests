package life.saqtapro.auth;

import life.saqtapro.api.AuthApi;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.is;

/**
 * Токены сотрудников SaqtaPro с кэшем на прогон: логин каждого пользователя выполняется один раз,
 * а не в каждом тесте (как в life.core.http.ApiClient).
 *
 * SaqtaPro выдаёт токен вида "2847|3nj4..." (Laravel Sanctum). Срок жизни на стенде не задокументирован,
 * поэтому токен перезапрашивается раз в 55 минут; при 401 его можно сбросить через invalidate().
 */
public final class SaqtaAuth {

    private static final Duration TTL = Duration.ofMinutes(55);
    private static final Map<TestUser, Token> TOKENS = new ConcurrentHashMap<>();
    private static final AuthApi AUTH_API = new AuthApi();

    private record Token(String value, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private SaqtaAuth() {}

    public static String tokenOf(TestUser user) {
        // compute блокирует только этого пользователя: при параллельном запуске логин идёт один раз
        return TOKENS.compute(user, (u, current) ->
                current != null && !current.expired()
                        ? current
                        : new Token(login(u), Instant.now().plus(TTL))
        ).value();
    }

    /** Сбросить токен — после 401 или в тесте на отзыв прав */
    public static void invalidate(TestUser user) {
        TOKENS.remove(user);
    }

    private static String login(TestUser user) {
        String token = AUTH_API.login(user.login(), user.password())
                .statusCode(200)
                .body("status", is(true))
                .extract()
                .path("data.token");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Логин " + user + " вернул status=true без data.token");
        }
        return token;
    }
}
