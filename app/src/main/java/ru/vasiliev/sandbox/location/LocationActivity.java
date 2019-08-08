package ru.vasiliev.sandbox.location;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.location.BaseLocationActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class LocationActivity extends BaseLocationActivity {

    @BindView(R.id.output_last_location)
    TextView mOutputLastLocation;

    @BindView(R.id.output_location)
    TextView mOutputLocation;

    @BindView(R.id.output_location_history)
    TextView mOutputLocationHistory;

    @BindView(R.id.error)
    TextView mOutputError;

    @BindView(R.id.get_last_location)
    Button mLastLocationButton;

    @BindView(R.id.get_location)
    Button mLocationButton;

    @BindView(R.id.get_locations_history)
    Button mLocationsHistoryButton;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mLastLocationButton.setOnClickListener(v -> {
            if (isLocationRequestingRunning()) {
                mOutputLastLocation.setText("Requesting location...");
                mSubscriptions.add(getLastLocation().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(
                                location -> mOutputLastLocation.setText(
                                        location.getLatitude() + " : " + location.getLongitude()
                                                + "; " + "Accuracy: " + location.getAccuracy()),
                                throwable -> mOutputLastLocation.setText(
                                        "getLastLocation(): error: " + throwable.getMessage())));
            } else {
                mOutputLastLocation.setText("Location monitor is not running");
            }
        });

        mLocationButton.setOnClickListener(v -> {
            if (isLocationRequestingRunning()) {
                mOutputLocation.setText("Requesting location...");
                mSubscriptions.add(getLocation().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(location -> {
                            String locationString = location.getLatitude() + " : " + location
                                    .getLongitude() + "; " + "Accuracy: " + location.getAccuracy();
                            Timber.d("getLocation(): " + locationString);
                            mOutputLocation.setText(locationString);
                        }, throwable -> mOutputLocation
                                .setText("getLocation(): error: " + throwable.getMessage())));
            } else {
                mOutputLocation.setText("Location monitor is not running");
            }
        });

        mLocationsHistoryButton.setOnClickListener(v -> {
            if (isLocationRequestingRunning()) {
                mOutputLocationHistory.setText("Requesting location...");
                mSubscriptions.add(getLocationHistory().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).take(1).subscribe(location -> {
                            String locationString = location.getLatitude() + " : " + location
                                    .getLongitude() + "; " + "Accuracy: " + location.getAccuracy();
                            Timber.d("getLocationHistory(): " + locationString);
                            mOutputLocationHistory.setText(locationString);
                        }, throwable -> mOutputLocationHistory.setText(
                                "getLocationHistory(): error: " + throwable.getMessage())));
            } else {
                mOutputLocationHistory.setText("Location monitor is not running");
            }
        });
    }

    @Override
    public void onLocationChange(Location location) {
        /*
        mOutput.setText(
                "onLocationChange(): " + location.getLatitude() + " : " + location.getLongitude()
                        + "\n" + "Accuracy: " + location.getAccuracy());
                        */
    }

    @Override
    public void onLocationSettingsUnresolvableError(Exception e) {
        mOutputError.setText(e.getMessage());
    }

    @Override
    public void onPlayServicesUnresolvableError() {
        mOutputError.setText("onPlayServicesUnresolvableError");
    }

    @Override
    public void onLocationPermissionDenied() {
        mOutputError.setText("onLocationPermissionDenied");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.clear();
    }
}