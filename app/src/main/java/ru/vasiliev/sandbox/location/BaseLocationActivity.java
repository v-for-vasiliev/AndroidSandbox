package ru.vasiliev.sandbox.location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.arellomobile.mvp.MvpAppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import ru.vasiliev.sandbox.App;
import rx.Observable;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public abstract class BaseLocationActivity extends MvpAppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        RxLocationCallback {

    public static final int REQUEST_CODE_LOCATION_SETTINGS = 1000;

    public static final String KEY_PROVIDER_TYPE = "key_provider_type";

    private static final int REQUEST_CODE_LOCATION = 100;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 200;

    private GoogleApiClient mGoogleApiClient;

    private RxLocationProvider mRxLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        /*
        int providerType = getIntent().getIntExtra(KEY_PROVIDER_TYPE, PROVIDER_TYPE_LEGACY);
        if (providerType == PROVIDER_TYPE_FUSED) {
            mRxLocationProvider = new RxFusedLocationProvider.Builder()
                    .context(BaseLocationActivity.this).callback(this)
                    .updateIntervalMilliseconds(5000).fastestIntervalMilliseconds(2500)
                    .priority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).build();
        } else {
            mRxLocationProvider = new RxLegacyLocationProvider.Builder()
                    .context(BaseLocationActivity.this).provider(LocationManager.NETWORK_PROVIDER)
                    .callback(this).updateIntervalMilliseconds(5000)
                    .fastestIntervalMilliseconds(2500)
                    .priority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).build();
        }
        */

        mRxLocationProvider = App.getAppComponent().getLocationServices().getLocationProvider();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRxLocationProvider.setCallback(this);
        if (!checkPlayServices()) {
            // Need to install Google Play Services to use the App properly
            onPlayServicesUnresolvableError();
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRxLocationProvider.removeCallback();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        // Do not stop updates if provider acts like a background service
        //stopLocationUpdates();
    }

    @Override
    public abstract void onLocationChange(Location location);

    public abstract void onLocationSettingsUnresolvableError(Exception e);

    public abstract void onPlayServicesUnresolvableError();

    public abstract void onLocationPermissionDenied();

    @Override
    public void onLocationSettingsError(Exception e) {
        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(BaseLocationActivity.this,
                            REQUEST_CODE_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                    onLocationSettingsUnresolvableError(sie);
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                onLocationSettingsUnresolvableError(
                        new RuntimeException("SETTINGS_CHANGE_UNAVAILABLE"));
                break;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                return false;
            }
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    startLocationUpdates();
                } else {
                    // Permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    onLocationPermissionDenied();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void startLocationUpdates() {
        if (!mRxLocationProvider.isRequestingUpdates()) {
            mRxLocationProvider.start();
        }
    }

    private void stopLocationUpdates() {
        mRxLocationProvider.stop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                onLocationPermissionDenied();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            startLocationUpdates();
        }
    }

    public boolean isLocationRequestingRunning() {
        return mRxLocationProvider.isRequestingUpdates();
    }

    public Observable<Location> getLocation() {
        return mRxLocationProvider.getLocationObservable();
    }

    public Observable<Location> getLocationHistory() {
        return mRxLocationProvider.getLocationHistoryObservable();
    }

    public Observable<Location> getLastLocation() {
        return mRxLocationProvider.getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Failed to connect to Play Services
        onPlayServicesUnresolvableError();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onPlayServicesUnresolvableError();
        // Failed to connect to Play Services
    }

    public RxLocationProvider getRxLocationProvider() {
        return mRxLocationProvider;
    }
}
