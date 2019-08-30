package ru.vasiliev.sandbox.network.data;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
public class RetrofitFactory {

    public static Retrofit getRetrofit(String baseUrl, OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
    }
}
