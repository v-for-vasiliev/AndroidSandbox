package ru.vasiliev.sandbox.location;

import android.location.Location;

interface RxLocationCallback {

    void onLocationChange(Location location);

    void onLocationSettingsError(Exception e);
}