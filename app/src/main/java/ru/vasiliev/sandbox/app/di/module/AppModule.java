package ru.vasiliev.sandbox.app.di.module;

import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.di.AppScope;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
@Module
public class AppModule {

    private final App mApp;

    public AppModule(@NonNull App app) {
        mApp = app;
    }

    @AppScope
    @Provides
    App provideApp() {
        return mApp;
    }

    @AppScope
    @Provides
    Context provideContext() {
        return mApp.getApplicationContext();
    }
}