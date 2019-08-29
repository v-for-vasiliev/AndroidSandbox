package ru.vasiliev.sandbox.app.di.module;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ru.vasiliev.sandbox.app.di.AppScope;
import ru.vasiliev.sandbox.network.ClientFactory;

@Module
public class NetworkModule {

    public NetworkModule() {
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
}
