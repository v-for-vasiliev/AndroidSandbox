package ru.vasiliev.sandbox.sovestoauth.di;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;
import ru.vasiliev.sandbox.sovestoauth.repository.OAuthRepository;
import ru.vasiliev.sandbox.sovestoauth.repository.datasource.OAuthApi;

@Module
public class OAuthModule {

    @OAuthScope
    @Provides
    public OAuthApi provideOAuthApi(Retrofit retrofit) {
        return retrofit.create(OAuthApi.class);
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
}
