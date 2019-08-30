package ru.vasiliev.sandbox.network.di;

import dagger.Subcomponent;
import ru.vasiliev.sandbox.network.di.module.NetworkModule;
import ru.vasiliev.sandbox.network.di.module.OAuthModule;
import ru.vasiliev.sandbox.network.domain.OAuthInteractor;
import ru.vasiliev.sandbox.network.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.network.presentation.OAuthPresenter;

@NetworkScope
@Subcomponent(modules = {NetworkModule.class, OAuthModule.class})
public interface NetworkComponent {

    OAuthInteractor getOAuthInteractor();

    CredentialsStorage getCredentialStorage();

    void inject(OAuthPresenter oAuthPresenter);
}
