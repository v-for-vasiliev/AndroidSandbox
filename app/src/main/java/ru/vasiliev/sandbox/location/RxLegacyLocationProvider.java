package ru.vasiliev.sandbox.location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Precondition: user must allow location permission request. See {@link LocationBaseActivity}
 */
public class RxLegacyLocationProvider implements RxLocationProvider {

    private Context mContext;

    private String mProvider;

    private SettingsClient mSettingsClient;

    private LocationSettingsRequest mLocationSettingsRequest;

    private LocationManager mLegacyLocationManager;

    private LocationListener mLegacyLocationListener;

    private LocationRequest mLocationRequest;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    private PublishSubject<Location> mResultPublisher = PublishSubject.create();

    private ReplaySubject<Location> mResultHistoryPublisher = ReplaySubject.create();

    private RxLocationCallback mRxLocationCallback;

    private long mUpdateIntervalInMilliseconds;

    private long mFastestUpdateIntervalInMilliseconds;

    private int mPriority;

    private boolean mRequestingUpdates = false;

    private RxLegacyLocationProvider(Context context, String provider, long updateInterval,
            long fastestUpdateInterval, int priority, RxLocationCallback callback) {
        mContext = context;
        mProvider = provider;
        mUpdateIntervalInMilliseconds = updateInterval;
        mFastestUpdateIntervalInMilliseconds = fastestUpdateInterval;
        mPriority = priority;
        mRxLocationCallback = callback;
    }

    @Override
    public void start() {
        if (!isInited()) {
            init();
        }
        requestLocationUpdates();
    }

    @Override
    public void stop() {
        if (isInited()) {
            if (!mSubscriptions.hasSubscriptions()) {
                mSubscriptions.unsubscribe();
            }
            mLegacyLocationManager.removeUpdates(mLegacyLocationListener);
            mRequestingUpdates = false;
            mLegacyLocationManager = null;
            mLocationRequest = null;
            mSettingsClient = null;
            mLocationSettingsRequest = null;
            mLegacyLocationListener = null;

        }
    }

    @Override
    public boolean isRequestingUpdates() {
        return mRequestingUpdates;
    }

    @Override
    public Observable<Location> getLastKnownLocation() {
        return Observable.create(subscriber -> {
            @SuppressLint("MissingPermission") Location location = mLegacyLocationManager
                    .getLastKnownLocation(mProvider);
            if (location != null) {
                subscriber.onNext(location);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new RuntimeException("No last location"));
            }
        });
    }

    @Override
    public Observable<Location> getLocationObservable() {
        return mResultPublisher.asObservable();
    }

    /**
     * @return all location updates since provider start (use {@link Observable#take(int)})
     */
    @Override
    public Observable<Location> getLocationHistoryObservable() {
        return mResultHistoryPublisher.asObservable();
    }

    private void init() {
        mLegacyLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        mLocationRequest = createLocationRequest();
        mSettingsClient = LocationServices.getSettingsClient(mContext);
        mLocationSettingsRequest = buildLocationSettingsRequest(mLocationRequest);
        mLegacyLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    pushNewLocation(location);
                } else {
                    Timber.d("UPD: no location");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @SuppressLint("MissingPermission")
            @Override
            public void onProviderEnabled(String provider) {
                Location location = mLegacyLocationManager.getLastKnownLocation(provider);
                if (location != null) {
                    pushNewLocation(location);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private boolean isInited() {
        return mSettingsClient != null && mLocationSettingsRequest != null
                && mLegacyLocationManager != null && mLocationRequest != null
                && mLegacyLocationListener != null;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(mUpdateIntervalInMilliseconds);
        locationRequest.setFastestInterval(mFastestUpdateIntervalInMilliseconds);
        locationRequest.setPriority(mPriority);
        return locationRequest;
    }

    private LocationSettingsRequest buildLocationSettingsRequest(LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        return builder.build();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        if (!mRequestingUpdates) {
            mRequestingUpdates = true;
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(
                    locationSettingsResponse -> mLegacyLocationManager
                            .requestLocationUpdates(mProvider, mFastestUpdateIntervalInMilliseconds,
                                    0.0f, mLegacyLocationListener)).addOnFailureListener(e -> {
                mRequestingUpdates = false;
                mRxLocationCallback.onLocationSettingsError(e);
            });
        }
    }

    private void pushNewLocation(@NonNull Location location) {
        Timber.d("UPD: " + location.getLatitude() + ": " + location.getLongitude() + "; Accuracy: "
                + location.getAccuracy());
        mResultPublisher.onNext(location);
        mResultHistoryPublisher.onNext(location);
        if (mRxLocationCallback != null) {
            mRxLocationCallback.onLocationChange(location);
        }
    }

    public static class Builder {

        private Context context;

        private String provider = LocationManager.NETWORK_PROVIDER;

        private long updateInterval = 10000;

        private long fastestUpdateInterval = updateInterval / 2;

        private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

        private RxLocationCallback rxLocationCallback;

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        /**
         * @param provider {@link LocationManager#NETWORK_PROVIDER} or {@link
         *                 LocationManager#GPS_PROVIDER}
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder updateIntervalMilliseconds(long updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public Builder fastestIntervalMilliseconds(long fastestUpdateInterval) {
            this.fastestUpdateInterval = fastestUpdateInterval;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder callback(RxLocationCallback callback) {
            this.rxLocationCallback = callback;
            return this;
        }

        public RxLegacyLocationProvider build() {
            return new RxLegacyLocationProvider(context, provider, updateInterval,
                    fastestUpdateInterval, priority, rxLocationCallback);
        }
    }
}