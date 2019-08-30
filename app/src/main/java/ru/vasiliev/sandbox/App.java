package ru.vasiliev.sandbox;

import android.app.Application;
import android.content.Intent;

import ru.vasiliev.sandbox.app.AppTaskWatchDog;
import ru.vasiliev.sandbox.app.ComponentManager;
import timber.log.Timber;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class App extends Application {

    private static App sInstance;

    private static ComponentManager sComponentManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        init();
    }

    private void init() {
        // Dagger application component
        sComponentManager = new ComponentManager(sInstance);
        // Timber logger
        Timber.plant(new Timber.DebugTree());
        // Watch dog services which detects application killed by system
        startService(new Intent(this, AppTaskWatchDog.class));
    }

    public static App getInstance() {
        return sInstance;
    }

    public static ComponentManager getComponentManager() {
        return sComponentManager;
    }
}
