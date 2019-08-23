package ru.vasiliev.sandbox.sovestoauth.di;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.di.AppScope;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;
import ru.vasiliev.sandbox.sovestoauth.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.sovestoauth.repository.OAuthRepository;
import ru.vasiliev.sandbox.sovestoauth.repository.datasource.OAuthApi;

@Module
public class OAuthModule {

    private App mApp;

    public OAuthModule(App app) {
        mApp = app;
    }

    @AppScope
    @Provides
    public OAuthApi provideOAuthApi(Retrofit retrofit) {
        return retrofit.create(OAuthApi.class);
    }

    @AppScope
    @Provides
    public OAuthRepository provideRepository(OAuthApi api) {
        return new OAuthRepository(api);
    }

    @AppScope
    @Provides
    public OAuthInteractor provideInteractor(OAuthRepository repository) {
        return new OAuthInteractor(repository, mApp);
    }

    @AppScope
    @Provides
    public CredentialsStorage provideCredentialsStorage() {
        return new CredentialsStorage();
    }
}
