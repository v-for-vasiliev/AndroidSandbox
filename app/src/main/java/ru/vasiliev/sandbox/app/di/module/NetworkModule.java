package ru.vasiliev.sandbox.app.di.module;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.vasiliev.sandbox.BuildConfig;
import ru.vasiliev.sandbox.network.ClientFactory;
import ru.vasiliev.sandbox.network.RetrofitFactory;

/**
 * Created by vasiliev on 11/02/2018.
 */

@Module
public class NetworkModule {

    private final String mBaseUrl;

    public NetworkModule(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    @Named("release")
    @Singleton
    @Provides
    public OkHttpClient provideNetworkClient() {
        return ClientFactory.getDefaultOkHttpClient();
    }

    @Named("debug")
    @Singleton
    @Provides
    public OkHttpClient getDefaultClientWithStetho() {
        return ClientFactory.getOkHttpClientWithStetho();
    }

    @Singleton
    @Provides
    public Retrofit provideRetrofit(@Named(BuildConfig.NETWORK_TRACE_STATE) OkHttpClient client) {
        return RetrofitFactory.getRetrofit(mBaseUrl, client);
    }
}
