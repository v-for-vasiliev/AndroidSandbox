package ru.vasiliev.sandbox.sovestoauth.di;

import dagger.Subcomponent;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;
import ru.vasiliev.sandbox.sovestoauth.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.sovestoauth.presentation.OAuthPresenter;

@OAuthScope
@Subcomponent(modules = {OAuthModule.class})
public interface OAuthComponent {

    OAuthInteractor getOAuthInteractor();

    CredentialsStorage getCredentialStorage();

    void inject(OAuthPresenter oAuthPresenter);
}
