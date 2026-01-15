package life.core.http.filters;
import io.qameta.allure.Allure;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.internal.NameAndValue;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.*;
import java.util.stream.Collectors;

import static io.qameta.allure.attachment.http.HttpRequestAttachment.Builder.create;

public class AllureFilter implements Filter {


    @Override
    public Response filter(FilterableRequestSpecification requestSpecification,
                           FilterableResponseSpecification responseSpecification,
                           FilterContext filterContext) {

        final Prettifier prettifier = new Prettifier();
        final String url = requestSpecification.getURI();

        final Set<String> hiddenHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        hiddenHeaders.addAll(Objects.requireNonNull(requestSpecification.getConfig().getLogConfig().blacklistedHeaders()));

        final HttpRequestAttachment.Builder requestAttachmentBuilder = create("Request", url)
                .setMethod(requestSpecification.getMethod())
                .setHeaders(toMapConverter(requestSpecification.getHeaders(), hiddenHeaders))
                .setCookies(toMapConverter(requestSpecification.getCookies(), new HashSet<>()));

        if (Objects.nonNull(requestSpecification.getBody())) {
            requestAttachmentBuilder.setBody(prettifier.getPrettifiedBodyIfPossible(requestSpecification));
        }

        if (Objects.nonNull(requestSpecification.getFormParams())) {
            requestAttachmentBuilder.setFormParams(requestSpecification.getFormParams());
        }

        final HttpRequestAttachment requestAttachment = requestAttachmentBuilder.build();

        new DefaultAttachmentProcessor().addAttachment(
                requestAttachment,
                new FreemarkerAttachmentRenderer("http-request.ftl")
        );

        Response response = filterContext.next(requestSpecification, responseSpecification);

        String responseStatus = response.getStatusLine();
        String responseHeaders = response.getHeaders().asList().stream()
                .map(h -> h.getName() + ": " + h.getValue()).collect(Collectors.joining("\n"));
        String responseBody = response.getBody() == null ? "" : response.getBody().asPrettyString();

        Allure.addAttachment(
                "Response: " + responseStatus,
                "text/plain",
                responseHeaders + (responseBody.isEmpty() ? "" : "\n\n" + responseBody),
                ".txt"
        );

        return response;
    }

    private static Map<String, String> toMapConverter(final Iterable<? extends NameAndValue> items,
                                                      final Set<String> toHide) {
        final Map<String, String> result = new HashMap<>();
        items.forEach(h -> result.put(h.getName(), toHide.contains(h.getName()) ? "[ BLACKLISTED ]" : h.getValue()));
        return result;
    }
}
