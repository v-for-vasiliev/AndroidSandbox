package ru.vasiliev.sandbox.network.di.module;

import android.support.annotation.NonNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.network.data.RetrofitFactory;
import ru.vasiliev.sandbox.network.di.NetworkScope;
import ru.vasiliev.sandbox.network.domain.OAuthInteractor;
import ru.vasiliev.sandbox.network.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.network.repository.OAuthRepository;
import ru.vasiliev.sandbox.network.repository.datasource.OAuthApi;

@Module
public class OAuthModule {

    private final String mBaseUrl;

    public OAuthModule(@NonNull String baseUrl) {
        mBaseUrl = baseUrl;
    }

    @NetworkScope
    @Provides
    public OAuthApi provideOAuthApi(@Named("default") OkHttpClient client) {
        return RetrofitFactory.getRetrofit(mBaseUrl, client).create(OAuthApi.class);
    }

    @NetworkScope
    @Provides
    public OAuthRepository provideRepository(OAuthApi api) {
        return new OAuthRepository(api);
    }

    @NetworkScope
    @Provides
    public CredentialsStorage provideCredentialsStorage() {
        return new CredentialsStorage();
    }

    @NetworkScope
    @Provides
    public OAuthInteractor provideInteractor(OAuthRepository repository,
            CredentialsStorage credentialsStorage, App app) {
        return new OAuthInteractor(repository, credentialsStorage, app);
    }
}
