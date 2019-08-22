package ru.vasiliev.sandbox.sovestoauth.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

public class DeviceUtils {

    public static String generateFingerprint(Context context) {
        String imei = getDeviceId(context);
        if (imei == null) {
            throw new RuntimeException("IMEI not exist");
        }
        return SHAHash.getHash(imei);
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private static String getDeviceId(Context context) {
        String imei = null;
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            imei = tm.getDeviceId();
        }
        return imei;
    }
}
