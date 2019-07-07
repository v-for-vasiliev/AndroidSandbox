package ru.vasiliev.sandbox;

import android.app.Application;

import ru.vasiliev.sandbox.di.components.AppComponent;
import ru.vasiliev.sandbox.di.components.DaggerAppComponent;
import ru.vasiliev.sandbox.di.modules.AppModule;
import timber.log.Timber;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class App extends Application {

    private static App sInstance;

    private static AppComponent sAppComponent;


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        init();
    }

    private void init() {
        // Dagger application component
        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();

        Timber.plant(new Timber.DebugTree());
    }

    public static App getInstance() {
        return sInstance;
    }

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }
}
