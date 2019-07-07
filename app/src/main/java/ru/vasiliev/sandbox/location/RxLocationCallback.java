package ru.vasiliev.sandbox.location;

import android.location.Location;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
interface RxLocationCallback {

    void onLocationChange(Location location);

    void onLocationSettingsError(Exception e);
}