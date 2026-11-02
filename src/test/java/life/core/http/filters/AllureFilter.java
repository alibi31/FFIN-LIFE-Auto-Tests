package life.core.http.filters;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.internal.NameAndValue;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Пишет HTTP-запрос и ответ в Allure как text/plain.
 * Не HTML: в однофайловом отчёте (singleFile) HTML-вложения браузер декодирует
 * как windows-1251, и кириллица превращается в кракозябры.
 */
public class AllureFilter implements Filter {

    private static final String MASK = "[ BLACKLISTED ]";

    /** Секретные поля в JSON-телах: "password": "..." -> "password": "[ BLACKLISTED ]" */
    private static final Pattern SECRET_JSON_FIELDS = Pattern.compile(
            "(\"(?:password|access_token|refresh_token|id_token|token)\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    /** Bearer-токен в любом месте текста */
    private static final Pattern BEARER = Pattern.compile(
            "(Bearer\\s+)[A-Za-z0-9._~+/=-]+", Pattern.CASE_INSENSITIVE);

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpecification,
                           FilterContext filterContext) {

        // Список скрываемых заголовков берётся из LogConfig (задаётся в RequestSpec)
        final Set<String> hiddenHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        hiddenHeaders.addAll(Objects.requireNonNull(
                request.getConfig().getLogConfig().blacklistedHeaders()));

        attachRequest(request, hiddenHeaders);

        final Response response = filterContext.next(request, responseSpecification);

        attachResponse(response, hiddenHeaders);

        return response;
    }

    private static void attachRequest(FilterableRequestSpecification request, Set<String> hiddenHeaders) {
        final String method = request.getMethod();
        final String url = request.getURI();

        final Map<String, String> headers = request.getHeaders().asList().stream()
                .collect(Collectors.toMap(
                        NameAndValue::getName,
                        h -> hiddenHeaders.contains(h.getName()) ? MASK : h.getValue(),
                        (a, b) -> a + ", " + b,
                        java.util.LinkedHashMap::new));

        final String cookies = StreamSupport.stream(request.getCookies().spliterator(), false)
                .map(c -> c.getName() + "=" + MASK)
                .collect(Collectors.joining("; "));

        final String body = request.getBody() == null
                ? null
                : maskSecrets(new Prettifier().getPrettifiedBodyIfPossible(request));

        final StringBuilder text = new StringBuilder()
                .append(method).append(' ').append(url).append("\n\n");

        if (!headers.isEmpty()) {
            text.append("Headers\n");
            headers.forEach((name, value) -> text.append(name).append(": ").append(value).append('\n'));
            text.append('\n');
        }
        if (!cookies.isEmpty()) {
            text.append("Cookies\n").append(cookies).append("\n\n");
        }
        if (request.getFormParams() != null && !request.getFormParams().isEmpty()) {
            text.append("Form params\n");
            request.getFormParams().forEach((k, v) -> text.append(k).append(": ").append(v).append('\n'));
            text.append('\n');
        }
        if (body != null && !body.isEmpty()) {
            text.append("Body\n").append(body).append("\n\n");
        }

        text.append("Curl\n").append(toCurl(method, url, headers, body));

        Allure.addAttachment("Request: " + method + " " + url, "text/plain", text.toString(), ".txt");
    }

    private static void attachResponse(Response response, Set<String> hiddenHeaders) {
        final String headers = response.getHeaders().asList().stream()
                .map(h -> h.getName() + ": " + (hiddenHeaders.contains(h.getName()) ? MASK : h.getValue()))
                .collect(Collectors.joining("\n"));

        final String body = response.getBody() == null
                ? ""
                : maskSecrets(response.getBody().asPrettyString());

        Allure.addAttachment(
                "Response: " + response.getStatusLine(),
                "text/plain",
                headers + (body.isEmpty() ? "" : "\n\n" + body),
                ".txt");
    }

    /** curl с уже замаскированными заголовками; тело в одну строку */
    private static String toCurl(String method, String url, Map<String, String> headers, String body) {
        final StringBuilder curl = new StringBuilder("curl -v -X ").append(method)
                .append(" '").append(url).append('\'');
        headers.forEach((name, value) -> curl.append(" -H '").append(name).append(": ")
                .append(escape(value)).append('\''));
        if (body != null && !body.isEmpty()) {
            curl.append(" -d '").append(escape(body.replaceAll("\\s*\\n\\s*", " "))).append('\'');
        }
        return curl.toString();
    }

    private static String escape(String value) {
        return value.replace("'", "'\\''");
    }

    private static String maskSecrets(final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String masked = SECRET_JSON_FIELDS.matcher(text).replaceAll("$1\"" + MASK + "\"");
        masked = BEARER.matcher(masked).replaceAll("$1" + MASK);
        return masked;
    }
}