package ru.vasiliev.sandbox.di.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.di.scopes.AppScope;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
@Module
public class AppModule {

    private final App mApp;

    public AppModule(@NonNull App appContext) {
        mApp = appContext;
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