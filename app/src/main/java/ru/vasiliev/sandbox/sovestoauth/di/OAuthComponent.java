package ru.vasiliev.sandbox.sovestoauth.di;

import dagger.Component;
import ru.vasiliev.sandbox.app.di.AppScope;
import ru.vasiliev.sandbox.app.di.module.NetworkModule;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;

@AppScope
@Component(modules = {NetworkModule.class, OAuthModule.class})
public interface OAuthComponent {

    OAuthInteractor getOAuthInteractor();
}
