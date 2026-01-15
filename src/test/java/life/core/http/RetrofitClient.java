package life.core.http;

import io.qameta.allure.okhttp3.AllureOkHttp3;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public final class RetrofitClient {
    private RetrofitClient() {}

    public static Retrofit create(String baseUrl) {
//        boolean allowInsecure = Boolean.parseBoolean(System.getProperty("ALLOW_INSECURE_TLS", "false"));

        boolean allowInsecure = true;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS)
                .addNetworkInterceptor(new AllureOkHttp3())
                .addInterceptor(loggingInterceptor);

        if(allowInsecure) {
            try {
                TrustManager[] trustAll = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }

                            public void checkClientTrusted(java.security.cert.X509Certificate[] c, String a) {}

                            public void checkServerTrusted(java.security.cert.X509Certificate[] c, String a) {}
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAll, new SecureRandom());
                client.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAll[0]);
                client.hostnameVerifier((hostname, session) -> true);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IllegalStateException(e);
            }
        }

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client.build())
                .build();
    }
}
