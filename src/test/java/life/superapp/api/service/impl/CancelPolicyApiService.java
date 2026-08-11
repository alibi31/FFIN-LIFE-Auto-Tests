package life.superapp.api.service.impl;

import io.qameta.allure.okhttp3.AllureOkHttp3;
import life.core.http.RetrofitClient;
import life.superapp.api.CancelPolicyApi;
import life.superapp.api.service.CancelPolicyService;
import life.utils.config.EnvConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import life.superapp.api.model.cancel.CancelPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import java.io.IOException;


public class CancelPolicyApiService implements CancelPolicyService {
    private static final Logger log = LoggerFactory.getLogger(CancelPolicyApiService.class);
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000;

    private static final MediaType SOAP_MEDIA_TYPE = MediaType.parse("text/xml");

    private final OkHttpClient client;
    private final String cancelPolicyUrl = EnvConfig.cfg().cancelPolicyUrl();

    public CancelPolicyApiService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        this.client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS)
                .addNetworkInterceptor(new AllureOkHttp3())
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Override
    public CancelPolicyResponse cancelPolicy(String policyNumber, String reason) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Attempt {}/{} to cancel policy {}", attempt, MAX_RETRIES, policyNumber);

                // Существующий код отправки
                String soapEnvelope = buildSoapEnvelope(policyNumber, reason);
                RequestBody body = RequestBody.create(soapEnvelope, SOAP_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(cancelPolicyUrl)
                        .post(body)
                        .addHeader("Content-Type", "text/xml")
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("HTTP error: " + response.code());
                }

                // Парсинг ответа
                String responseBody = response.body().string();
                CancelPolicyResponse result = parseResponse(responseBody);

                // Проверка успеха
                if (result.isSuccess()) {
                    log.info("Policy {} cancelled successfully", policyNumber);
                    return result;
                }

                // Проверка "уже аннулирован"
                if (isAlreadyCancelled(result.getMessage())) {
                    log.info("Policy {} already cancelled or not found", policyNumber);
                    result.setSuccess(true);
                    return result;
                }

                log.warn("Cancel failed for policy {}: {}", policyNumber, result.getMessage());

                if (attempt == MAX_RETRIES) {
                    return result;
                }

                sleep(RETRY_DELAY_MS);

            } catch (IOException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }

        throw lastException != null ? lastException :
                new IOException("Failed after " + MAX_RETRIES + " attempts");
    }

    private String buildSoapEnvelope(String policyNumber, String reason) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:cli=\"http://client/clientws\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<cli:nullifyPolicyRequest>" +
                "<cli:policyNumber>" + policyNumber + "</cli:policyNumber>" +
                "<cli:reason>" + reason + "</cli:reason>" +
                "</cli:nullifyPolicyRequest>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }

    private CancelPolicyResponse parseResponse(String xml) throws IOException {
        CancelPolicyResponse result = new CancelPolicyResponse();

        try {
            // Извлекаем success флаг
            Pattern successPattern = Pattern.compile("<ns2:success>(true|false)</ns2:success>");
            Matcher successMatcher = successPattern.matcher(xml);
            if (successMatcher.find()) {
                result.setSuccess(Boolean.parseBoolean(successMatcher.group(1)));
            }

            // Извлекаем сообщение
            Pattern messagePattern = Pattern.compile("<ns2:response>(.*?)</ns2:response>");
            Matcher messageMatcher = messagePattern.matcher(xml);
            if (messageMatcher.find()) {
                String encodedMessage = messageMatcher.group(1);
                byte[] bytes = encodedMessage.getBytes(StandardCharsets.ISO_8859_1);
                String decodedMessage = new String(bytes, StandardCharsets.UTF_8);
                result.setMessage(decodedMessage);
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse SOAP response", e);
        }

        return result;
    }

    private boolean isAlreadyCancelled(String message) {
        if (message == null) return false;
        return message.contains("аннулирован") || message.contains("не найден");
    }

    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted");
        }
    }

}
