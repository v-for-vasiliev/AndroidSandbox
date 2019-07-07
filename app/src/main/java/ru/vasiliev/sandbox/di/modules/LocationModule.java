package ru.vasiliev.sandbox.di.modules;

import com.google.android.gms.location.LocationRequest;

import android.location.LocationManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.BuildConfig;
import ru.vasiliev.sandbox.di.scopes.AppScope;
import ru.vasiliev.sandbox.location.LocationServices;
import ru.vasiliev.sandbox.location.RxFusedLocationProvider;
import ru.vasiliev.sandbox.location.RxLegacyLocationProvider;
import ru.vasiliev.sandbox.location.RxLocationProvider;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
@Module
public class LocationModule {

    @Named("fused")
    @AppScope
    @Provides
    public RxLocationProvider provideRxFusedLocationProvider(App app) {
        return new RxFusedLocationProvider.Builder().context(app)
                .updateIntervalMilliseconds(BuildConfig.LOCATION_UPDATE_INTERVAL_MS)
                .fastestIntervalMilliseconds(BuildConfig.LOCATION_FASTEST_UPDATE_INTERVAL_MS)
                .priority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).build();
    }

    @Named("legacy")
    @AppScope
    @Provides
    public RxLocationProvider provideRxLegacyLocationProvider(App app) {
        return new RxLegacyLocationProvider.Builder().context(app)
                .provider(LocationManager.NETWORK_PROVIDER)
                .updateIntervalMilliseconds(BuildConfig.LOCATION_UPDATE_INTERVAL_MS)
                .fastestIntervalMilliseconds(BuildConfig.LOCATION_FASTEST_UPDATE_INTERVAL_MS)
                .priority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).build();
    }

    @AppScope
    @Provides
    public LocationServices provideLocationServices(App app,
            @Named(BuildConfig.LOCATION_PROVIDER_TYPE) RxLocationProvider locationProvider) {
        return new LocationServices(app, locationProvider);
    }
}
