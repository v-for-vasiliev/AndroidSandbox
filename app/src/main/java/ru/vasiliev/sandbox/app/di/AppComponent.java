package ru.vasiliev.sandbox.app.di;

import dagger.Component;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.di.module.AppModule;
import ru.vasiliev.sandbox.location.LocationServices;
import ru.vasiliev.sandbox.location.di.LocationModule;
import ru.vasiliev.sandbox.network.di.NetworkComponent;
import ru.vasiliev.sandbox.network.di.module.OAuthModule;
import ru.vasiliev.sandbox.visionlabs.di.VisionLabsComponent;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
@AppScope
@Component(modules = {AppModule.class, LocationModule.class})
public interface AppComponent {

    App getApp();

    LocationServices getLocationServices();

    NetworkComponent plusOAuthComponent(OAuthModule oAuthModule);

    VisionLabsComponent plusVisionLabsComponent();
}