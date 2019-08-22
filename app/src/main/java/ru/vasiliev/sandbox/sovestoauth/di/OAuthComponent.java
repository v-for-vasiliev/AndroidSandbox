package ru.vasiliev.sandbox.sovestoauth.di;

import dagger.Subcomponent;
import ru.vasiliev.sandbox.app.di.module.NetworkModule;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;

@OAuthScope
@Subcomponent(modules = {NetworkModule.class, OAuthModule.class})
public interface OAuthComponent {

    OAuthInteractor getOAuthInteractor();
}
