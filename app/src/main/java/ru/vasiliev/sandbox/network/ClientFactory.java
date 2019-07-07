package ru.vasiliev.sandbox.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

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
}
