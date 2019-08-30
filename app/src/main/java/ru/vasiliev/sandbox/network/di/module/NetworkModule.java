package ru.vasiliev.sandbox.network.di.module;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ru.vasiliev.sandbox.network.data.ClientFactory;
import ru.vasiliev.sandbox.network.di.NetworkScope;

@Module
public class NetworkModule {

    public NetworkModule() {
    }

    @Named("default")
    @NetworkScope
    @Provides
    public OkHttpClient provideDefaultNetworkClient() {
        return ClientFactory.getDefaultOkHttpClient();
    }

    @Named("default_stetho")
    @NetworkScope
    @Provides
    public OkHttpClient provideDefaultNetworkClientWithStetho() {
        return ClientFactory.getOkHttpClientWithStetho();
    }
}
