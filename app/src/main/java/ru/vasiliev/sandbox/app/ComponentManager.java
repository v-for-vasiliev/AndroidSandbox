package ru.vasiliev.sandbox.app;

import android.support.annotation.NonNull;

import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.di.AppComponent;
import ru.vasiliev.sandbox.app.di.DaggerAppComponent;
import ru.vasiliev.sandbox.app.di.module.AppModule;
import ru.vasiliev.sandbox.app.di.module.NetworkModule;
import ru.vasiliev.sandbox.sovestoauth.di.DaggerOAuthComponent;
import ru.vasiliev.sandbox.sovestoauth.di.OAuthComponent;
import ru.vasiliev.sandbox.sovestoauth.di.OAuthModule;
import ru.vasiliev.sandbox.visionlabs.di.VisionLabsComponent;

public class ComponentManager {

    private App mApp;

    private AppComponent mAppComponent;

    private VisionLabsComponent mVisionLabsComponent;

    private OAuthComponent mOAuthComponent;

    public ComponentManager(@NonNull App app) {
        mApp = app;
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(mApp)).build();
        mOAuthComponent = DaggerOAuthComponent.builder().oAuthModule(new OAuthModule(mApp))
                .networkModule(new NetworkModule("api.sovest.com")).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public OAuthComponent getOAuthComponent() {
        return mOAuthComponent;
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
