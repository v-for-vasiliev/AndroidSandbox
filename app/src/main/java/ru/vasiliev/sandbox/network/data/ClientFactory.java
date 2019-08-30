package ru.vasiliev.sandbox.network.data;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import ru.vasiliev.sandbox.BuildConfig;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
public class ClientFactory {

    public static OkHttpClient getDefaultOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(BuildConfig.OKHTTP_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(BuildConfig.OKHTTP_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(BuildConfig.OKHTTP_READ_TIMEOUT_SEC, TimeUnit.SECONDS).build();
    }

    public static OkHttpClient getOkHttpClientWithStetho() {
        return new OkHttpClient.Builder()
                .connectTimeout(BuildConfig.OKHTTP_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(BuildConfig.OKHTTP_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(BuildConfig.OKHTTP_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor()).build();
    }

    private static OkHttpClient getSslClientWithPinning(@Nullable String endpoint) {

    }

    public OkHttpClient getAuthorizationClient(String endpoint, String fingerprint,
            String deviceId) {
        try {
            SovestApplication app = SovestApplication.getInstance();
            OkHttpClient.Builder builder = getSslClientBuilder(endpoint);
            builder.addInterceptor(new AuthorizationInterceptor(fingerprint, deviceId));
//            builder.addInterceptor(new MockResponseInterceptor());
            builder.addInterceptor(new HttpLoggingInterceptor());
            builder.addInterceptor(new ErrorInterceptor());
            builder.addInterceptor(
                    new DeviceIdInterceptor(app.getDeviceId(), app.getBackendDeviceId()));
            return builder.build();
        } catch (Exception e) {
            Utils.trace(e);
            return new OkHttpClient();
        }
    }

    private static OkHttpClient.Builder getSslClientBuilder(X509Certificate certificate, @Nullable String endpoint)
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException,
            KeyManagementException {
        if (true) {
            TrustManagerFactory trustedManagerFactory = getTrustedManagerFactory(certificate);
            builder.sslSocketFactory(getSSLSocketFactory(trustedManagerFactory),
                    (X509TrustManager) trustedManagerFactory.getTrustManagers()[0]);

            if (endpoint != null && certificate != null) {
                CertificatePinner.Builder pinnerBuilder = new CertificatePinner.Builder();
                pinnerBuilder.add(endpoint, CertificatePinner.pin(certificate));
                builder.certificatePinner(pinnerBuilder.build());
            }
        } else {
            addAllTrustedSSLSocketFactory(builder);
        }
    }

    private static void addAllTrustedSSLSocketFactory(@NonNull OkHttpClient.Builder builder)
            throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        }};

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        builder.sslSocketFactory(sslContext.getSocketFactory(),
                (X509TrustManager) trustAllCerts[0]);
    }
}
