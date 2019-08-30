package ru.vasiliev.sandbox.app;

import android.support.annotation.NonNull;

import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.BuildConfig;
import ru.vasiliev.sandbox.app.di.AppComponent;
import ru.vasiliev.sandbox.app.di.DaggerAppComponent;
import ru.vasiliev.sandbox.app.di.module.AppModule;
import ru.vasiliev.sandbox.network.di.NetworkComponent;
import ru.vasiliev.sandbox.network.di.module.OAuthModule;
import ru.vasiliev.sandbox.visionlabs.di.VisionLabsComponent;

public class ComponentManager {

    private App mApp;

    private AppComponent mAppComponent;

    private VisionLabsComponent mVisionLabsComponent;

    private NetworkComponent mNetworkComponent;

    public ComponentManager(@NonNull App app) {
        mApp = app;
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(mApp)).build();
        mNetworkComponent = mAppComponent
                .plusOAuthComponent(new OAuthModule(BuildConfig.API_OAUTH_ENDPOINT));
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public NetworkComponent getNetworkComponent() {
        return mNetworkComponent;
    }

    public synchronized VisionLabsComponent getVisionLabsComponent() {
        if (mVisionLabsComponent == null) {
            mVisionLabsComponent = mAppComponent.plusVisionLabsComponent();
        }
        return mVisionLabsComponent;
    }

    public synchronized void releaseVisionLabsComponent() {
        mVisionLabsComponent = null;
    }
}
