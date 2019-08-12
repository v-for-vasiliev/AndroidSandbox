package ru.vasiliev.sandbox.visionlabs.domain;

import android.content.Context;

public interface VisionLabsInteractor {

    boolean loadLibraries();

    boolean unpackResourcesAndInitEngine(Context context);
}
