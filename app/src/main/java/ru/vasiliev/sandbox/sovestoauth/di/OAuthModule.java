package ru.vasiliev.sandbox.sovestoauth.di;

import android.support.annotation.NonNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.BuildConfig;
import ru.vasiliev.sandbox.network.RetrofitFactory;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;
import ru.vasiliev.sandbox.sovestoauth.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.sovestoauth.repository.OAuthRepository;
import ru.vasiliev.sandbox.sovestoauth.repository.datasource.OAuthApi;

@Module
public class OAuthModule {

    private final String mBaseUrl;

    public OAuthModule(@NonNull String baseUrl) {
        mBaseUrl = baseUrl;
    }

    @OAuthScope
    @Provides
    public OAuthApi provideOAuthApi(@Named(BuildConfig.NETWORK_TRACE_STATE) OkHttpClient client) {
        return RetrofitFactory.getRetrofit(mBaseUrl, client).create(OAuthApi.class);
    }

    @OAuthScope
    @Provides
    public OAuthRepository provideRepository(OAuthApi api) {
        return new OAuthRepository(api);
    }

    @OAuthScope
    @Provides
    public OAuthInteractor provideInteractor(OAuthRepository repository, App app) {
        return new OAuthInteractor(repository, app);
    }

    @OAuthScope
    @Provides
    public CredentialsStorage provideCredentialsStorage() {
        return new CredentialsStorage();
    }
}
