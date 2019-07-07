package ru.vasiliev.sandbox.location;

import android.provider.Settings;

import ru.vasiliev.sandbox.App;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class LocationServices {

    private App mApp;

    private RxLocationProvider mLocationProvider;

    public LocationServices(App app, RxLocationProvider locationProvider) {
        mApp = app;
        mLocationProvider = locationProvider;
    }

    protected boolean isLocationEnabled() {
        int locationMode = Settings.Secure
                .getInt(mApp.getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    public RxLocationProvider getLocationProvider() {
        return mLocationProvider;
    }
}
