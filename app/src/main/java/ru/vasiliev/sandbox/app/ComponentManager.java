package ru.vasiliev.sandbox.app;

import android.support.annotation.NonNull;

import ru.vasiliev.sandbox.app.di.AppComponent;
import ru.vasiliev.sandbox.visionlabs.di.VisionLabsComponent;

public class ComponentManager {

    private AppComponent mAppComponent;

    private VisionLabsComponent mVisionLabsComponent;

    public ComponentManager(@NonNull AppComponent appComponent) {
        mAppComponent = appComponent;
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
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
