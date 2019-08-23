package ru.vasiliev.sandbox.app.di.module;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.vasiliev.sandbox.BuildConfig;
import ru.vasiliev.sandbox.app.di.AppScope;
import ru.vasiliev.sandbox.network.ClientFactory;
import ru.vasiliev.sandbox.network.RetrofitFactory;

@Module
public class NetworkModule {

    private final String mBaseUrl;

    public NetworkModule(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    @Named("release")
    @AppScope
    @Provides
    public OkHttpClient provideNetworkClient() {
        return ClientFactory.getDefaultOkHttpClient();
    }

    @Named("debug")
    @AppScope
    @Provides
    public OkHttpClient getDefaultClientWithStetho() {
        return ClientFactory.getOkHttpClientWithStetho();
    }

    @AppScope
    @Provides
    public Retrofit provideRetrofit(@Named(BuildConfig.NETWORK_TRACE_STATE) OkHttpClient client) {
        return RetrofitFactory.getRetrofit(mBaseUrl, client);
    }
}
