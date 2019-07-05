package ru.vasiliev.sandbox;

import android.app.Application;

import timber.log.Timber;

public class App extends Application {

    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        init();
    }

    private void init() {
        Timber.plant(new Timber.DebugTree());
    }
}
