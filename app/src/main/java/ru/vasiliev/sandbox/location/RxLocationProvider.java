package ru.vasiliev.sandbox.location;

import android.location.Location;

import rx.Observable;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public interface RxLocationProvider {

    int PROVIDER_TYPE_LEGACY = 1;

    int PROVIDER_TYPE_FUSED = 2;

    int TRACKING_INTERVAL_SECONDS = 60; // 1 minute

    void setCallback(RxLocationCallback callback);

    void removeCallback();

    void start();

    void stop();

    boolean isRequestingUpdates();

    Observable<Location> getLastKnownLocation();

    Observable<Location> getLocationObservable();

    Observable<Location> getLocationHistoryObservable();

    void startTracking();

    void stopTracking();

    boolean isTracking();
}
