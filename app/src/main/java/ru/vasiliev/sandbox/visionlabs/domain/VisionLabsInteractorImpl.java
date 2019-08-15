package ru.vasiliev.sandbox.visionlabs.domain;

import android.content.Context;

import java.io.File;

import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.FaceEngineJNI;
import timber.log.Timber;

public class VisionLabsInteractorImpl implements VisionLabsInteractor {

    private final int MAX_INIT_ATTEMPTS = 1;

    private VisionLabsPreferences mPreferences;

    private int mInitAttemptsCount;

    public VisionLabsInteractorImpl(VisionLabsPreferences preferences) {
        mPreferences = preferences;
    }

    @Override
    public boolean loadLibraries() {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("flower");
            System.loadLibrary("PhotoMaker");
            System.loadLibrary("LivenessEngineSDK");
            System.loadLibrary("wrapper");
            return true;
        } catch (UnsatisfiedLinkError e) {
            Timber.e("Luna Mobile", "Native library failed to load: " + e);
            return false;
        }
    }

    @Override
    public boolean unpackResourcesAndInitEngine(Context context) {
        if (mPreferences.getFirstRun()) {
            if (!Resources.createVLDataFolder(context)) {
                return false;
            }
            boolean dataAssetsUnpackedSuccess = Resources
                    .createFilesFromAssetFolder(context, Resources.VL_DATA_PACK);
            Timber.w("Assets unpacked: " + dataAssetsUnpackedSuccess);
            if (!dataAssetsUnpackedSuccess) {
                Timber.w("Couldn't unpack resources from assets");
                return false;
            } else {
                mPreferences.setFirstRun(false);
            }
        }
        if (!FaceEngineJNI.initFaceEngine(context.getFilesDir() + "/vl/data")) {
            File dataDir = new File(context.getFilesDir() + "/vl/data");
            if (!dataDir.isDirectory()) {
                if (!mPreferences.getFirstRun() && mInitAttemptsCount < MAX_INIT_ATTEMPTS) {
                    Timber.w("Face engine data not found, trying to unpack data and reload engine");
                    mPreferences.setFirstRun(true);
                    mInitAttemptsCount++;
                    return unpackResourcesAndInitEngine(context);
                }
            }
            Timber.w("Couldn't init face engine by path: " + context.getFilesDir() + "/vl/data");
            return false;
        } else {
            Timber.w("Successfully initialized face engine from path: " + context.getFilesDir()
                    + "/vl/data");
        }
        return true;
    }
}
