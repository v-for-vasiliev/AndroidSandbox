package ru.vasiliev.sandbox.location;

import android.location.Location;

import rx.Observable;

public interface RxLocationProvider {

    int PROVIDER_TYPE_LEGACY = 1;

    int PROVIDER_TYPE_FUSED = 2;

    void start();

    void stop();

    boolean isRequestingUpdates();

    Observable<Location> getLastKnownLocation();

    Observable<Location> getLocationObservable();

    Observable<Location> getLocationHistoryObservable();
}
