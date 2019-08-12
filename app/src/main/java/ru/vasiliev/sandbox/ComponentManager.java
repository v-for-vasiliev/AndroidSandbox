package ru.vasiliev.sandbox;

import android.support.annotation.NonNull;

import ru.vasiliev.sandbox.di.components.AppComponent;
import ru.vasiliev.sandbox.di.components.VisionLabsComponent;

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
