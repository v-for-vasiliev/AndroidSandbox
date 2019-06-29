package ru.vasiliev.sandbox.location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Precondition: user must allow location permission request. See {@link LocationBaseActivity}
 *
 * Fused location provider uses all available sources. Sources choose strategy based on priority
 * (Ex. {@link LocationRequest#PRIORITY_HIGH_ACCURACY}). Location updates comes every ~10 min., so
 * it not suited for fast location detection from cold start. For this case use
 * {@link RxLegacyLocationProvider}
 */
public class RxFusedLocationProvider implements RxLocationProvider {

    private Context mContext;

    private SettingsClient mSettingsClient;

    private LocationSettingsRequest mLocationSettingsRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LocationCallback mLocationCallback;

    private LocationRequest mLocationRequest;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    private PublishSubject<Location> mResultPublisher = PublishSubject.create();

    private ReplaySubject<Location> mResultHistoryPublisher = ReplaySubject.create();

    private RxLocationCallback mRxLocationCallback;

    private long mUpdateIntervalInMilliseconds;

    private long mFastestUpdateIntervalInMilliseconds;

    private int mPriority;

    private boolean mRequestingUpdates = false;

    private RxFusedLocationProvider(Context context, long updateInterval,
            long fastestUpdateInterval, int priority, RxLocationCallback callback) {
        mContext = context;
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
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(task -> {
                        mRequestingUpdates = false;
                        mFusedLocationProviderClient = null;
                        mLocationRequest = null;
                        mSettingsClient = null;
                        mLocationSettingsRequest = null;
                        mLocationCallback = null;
                    });

        }
    }

    @Override
    public boolean isRequestingUpdates() {
        return mRequestingUpdates;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Observable<Location> getLastKnownLocation() {
        return Observable.create(subscriber -> mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        subscriber.onNext(location);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new RuntimeException("No last location"));
                    }
                }).addOnFailureListener(
                        command -> subscriber.onError(new RuntimeException("No last location"))));
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationRequest = createLocationRequest();
        mSettingsClient = LocationServices.getSettingsClient(mContext);
        mLocationSettingsRequest = buildLocationSettingsRequest(mLocationRequest);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    pushNewLocation(lastLocation);
                } else {
                    Timber.d("UPD: no location");
                }
            }
        };
    }

    private boolean isInited() {
        return mSettingsClient != null && mLocationSettingsRequest != null
                && mFusedLocationProviderClient != null && mLocationRequest != null
                && mLocationCallback != null;
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
                    locationSettingsResponse -> mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallback,
                                    Looper.myLooper())).addOnFailureListener(e -> {
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

        private long updateInterval = 10000;

        private long fastestUpdateInterval = updateInterval / 2;

        private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

        private RxLocationCallback rxLocationCallback;

        public Builder context(Context context) {
            this.context = context;
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

        public RxFusedLocationProvider build() {
            return new RxFusedLocationProvider(context, updateInterval, fastestUpdateInterval,
                    priority, rxLocationCallback);
        }
    }
}