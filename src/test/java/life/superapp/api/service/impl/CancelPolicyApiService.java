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


import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class CancelPolicyApiService implements CancelPolicyService {

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
    public void cancelPolicy(String policyNumber, String reason) throws IOException {
        String soapEnvelope = buildSoapEnvelope(policyNumber, reason);
        RequestBody body = RequestBody.create(soapEnvelope, SOAP_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(cancelPolicyUrl)
                .post(body)
                .addHeader("Content-Type", "text/xml")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to cancel policy. HTTP status: " + response.code());
            }
        }
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

}
