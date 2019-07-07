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

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 *
 * Precondition: user must allow location permission request. See {@link BaseLocationActivity}
 */
public class RxLegacyLocationProvider implements RxLocationProvider {

    private Context mContext;

    private String mProvider;

    private SettingsClient mSettingsClient;

    private LocationSettingsRequest mLocationSettingsRequest;

    private LocationManager mLegacyLocationManager;

    private LocationListener mLegacyLocationListener;

    private LocationRequest mLocationRequest;

    private PublishSubject<Location> mResultPublisher = PublishSubject.create();

    private ReplaySubject<Location> mResultHistoryPublisher = ReplaySubject.create();

    private RxLocationCallback mRxLocationCallback;

    private long mUpdateIntervalInMilliseconds;

    private long mFastestUpdateIntervalInMilliseconds;

    private int mPriority;

    private boolean mRequestingUpdates = false;

    private CompositeSubscription mTrackingSubscriptions = new CompositeSubscription();

    private boolean mTracking = false;

    private RxLegacyLocationProvider(Context context, String provider, long updateInterval,
            long fastestUpdateInterval, int priority, RxLocationCallback callback) {
        Timber.d("constructor() {provider=%s, updateInterval=%d, "
                        + "fastestUpdateInterval=%d, priority=%d, hasCallback=%b}", provider,
                updateInterval, fastestUpdateInterval, priority, (callback != null));
        mContext = context;
        mProvider = provider;
        mUpdateIntervalInMilliseconds = updateInterval;
        mFastestUpdateIntervalInMilliseconds = fastestUpdateInterval;
        mPriority = priority;
        mRxLocationCallback = callback;
    }

    @Override
    public void setCallback(RxLocationCallback callback) {
        Timber.d("setCallback() = " + callback);
        mRxLocationCallback = callback;
    }

    @Override
    public void removeCallback() {
        Timber.d("removeCallback()");
        mRxLocationCallback = null;
    }

    @Override
    public void start() {
        Timber.d("start()");
        if (!isInited()) {
            init();
        }
        requestLocationUpdates();
    }

    @Override
    public void stop() {
        if (isInited()) {
            Timber.d("stop()");
            stopTracking();
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

    @SuppressLint("MissingPermission")
    @Override
    public Observable<Location> getLastKnownLocation() {
        return Observable.create(subscriber -> {
            Location location = mLegacyLocationManager.getLastKnownLocation(mProvider);
            if (location != null) {
                Timber.d("getLastKnownLocation() {" + location.getLatitude() + ": " + location
                        .getLongitude() + "; Accuracy: " + location.getAccuracy() + "}");
                subscriber.onNext(location);
                subscriber.onCompleted();
            } else {
                Timber.d("getLastKnownLocation() = null");
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

    @Override
    public void startTracking() {
        if (!mTracking) {
            mTracking = true;
            mTrackingSubscriptions
                    .add(Observable.interval(0, TRACKING_INTERVAL_SECONDS, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(tick -> sendDsaLocation(), e -> {
                                mTracking = false;
                                Timber.e("", e);
                            }, () -> Timber.d("Tracking stopped")));
        }
    }

    @Override
    public void stopTracking() {
        if (mTracking) {
            Timber.d("stopTracking()");
            if (mTrackingSubscriptions.hasSubscriptions()) {
                mTrackingSubscriptions.clear();
            }
            mTracking = false;
        }
    }

    @Override
    public boolean isTracking() {
        return mTracking;
    }

    private void init() {
        Timber.d("init()");
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
                    Timber.d("onLocationChanged() = null");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Timber.d("onStatusChanged() {provider=%s, status=%d, extras={%s}}", provider,
                        status, extras);
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onProviderEnabled(String provider) {
                Timber.d("onProviderEnabled() {provider=%s}", provider);
                Location location = mLegacyLocationManager.getLastKnownLocation(provider);
                if (location != null) {
                    pushNewLocation(location);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                Timber.d("onProviderDisabled() {provider=%s}", provider);
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
            Timber.d("requestLocationUpdates()");
            mRequestingUpdates = true;
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(
                    locationSettingsResponse -> mLegacyLocationManager
                            .requestLocationUpdates(mProvider, mFastestUpdateIntervalInMilliseconds,
                                    0.0f, mLegacyLocationListener)).addOnFailureListener(e -> {
                mRequestingUpdates = false;
                if (mRxLocationCallback != null) {
                    mRxLocationCallback.onLocationSettingsError(e);
                }
            });
        }
    }

    private void pushNewLocation(@NonNull Location location) {
        Timber.d("onLocationChanged() {" + location.getLatitude() + ": " + location.getLongitude()
                + "; Accuracy: " + location.getAccuracy() + "}");
        mResultPublisher.onNext(location);
        mResultHistoryPublisher.onNext(location);
        if (mRxLocationCallback != null) {
            mRxLocationCallback.onLocationChange(location);
        }
    }

    @SuppressLint("MissingPermission")
    private void sendDsaLocation() {
        Location location = mLegacyLocationManager.getLastKnownLocation(mProvider);
        if (location != null) {
            // Send location to server (mTrackingSubscriptions)
            Timber.d("sendDsaLocation() {" + location.getLatitude() + ": " + location.getLongitude()
                    + "; Accuracy: " + location.getAccuracy() + "}");
        } else {
            Timber.d("sendDsaLocation() skipped, no location");
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