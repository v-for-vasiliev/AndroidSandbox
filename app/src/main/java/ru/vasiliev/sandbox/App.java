package ru.vasiliev.sandbox;

import android.app.Application;

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

        Timber.plant(new Timber.DebugTree());
    }

    public static App getInstance() {
        return sInstance;
    }

    public static ComponentManager getComponentManager() {
        return sComponentManager;
    }
}
