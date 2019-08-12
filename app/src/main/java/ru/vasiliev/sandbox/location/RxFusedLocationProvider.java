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

import java.util.concurrent.TimeUnit;

import ru.vasiliev.sandbox.location.presentation.BaseLocationActivity;
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

    private PublishSubject<Location> mResultPublisher = PublishSubject.create();

    private ReplaySubject<Location> mResultHistoryPublisher = ReplaySubject.create();

    private RxLocationCallback mRxLocationCallback;

    private long mUpdateIntervalInMilliseconds;

    private long mFastestUpdateIntervalInMilliseconds;

    private int mPriority;

    private boolean mRequestingUpdates = false;

    private CompositeSubscription mTrackingSubscriptions = new CompositeSubscription();

    private boolean mTracking = false;

    private RxFusedLocationProvider(Context context, long updateInterval,
            long fastestUpdateInterval, int priority, RxLocationCallback callback) {
        Timber.d("constructor() {updateInterval=%d, fastestUpdateInterval=%d, "
                        + "priority=%d, hasCallback=%b}", updateInterval, fastestUpdateInterval, priority,
                (callback != null));
        mContext = context;
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
                        Timber.d("getLastKnownLocation() {" + location.getLatitude() + ": "
                                + location.getLongitude() + "; Accuracy: " + location.getAccuracy()
                                + "}");
                        subscriber.onNext(location);
                        subscriber.onCompleted();
                    } else {
                        Timber.d("getLastKnownLocation() = null");
                        subscriber.onError(new RuntimeException("No last location"));
                    }
                }).addOnFailureListener(command -> {
                    Timber.d("getLastKnownLocation() = null");
                    subscriber.onError(new RuntimeException("No last location"));
                }));
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
            Timber.d("startTracking()");
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
                    Timber.d("onLocationResult() = null");
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
            Timber.d("requestLocationUpdates()");
            mRequestingUpdates = true;
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(
                    locationSettingsResponse -> mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallback,
                                    Looper.myLooper())).addOnFailureListener(e -> {
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
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Send location to server (mTrackingSubscriptions)
                Timber.d("sendDsaLocation() {" + location.getLatitude() + ": " + location
                        .getLongitude() + "; Accuracy: " + location.getAccuracy() + "}");
            } else {
                Timber.d("sendDsaLocation() skipped, no location");
            }
        }).addOnFailureListener(command -> Timber.d("sendDsaLocation() skipped, no location"));
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