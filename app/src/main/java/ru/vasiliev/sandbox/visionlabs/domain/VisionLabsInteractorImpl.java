package ru.vasiliev.sandbox.visionlabs.domain;

import android.content.Context;

import ru.visionlab.Resources;
import ru.visionlab.faceengine.FaceEngineJNI;
import timber.log.Timber;

public class VisionLabsInteractorImpl implements VisionLabsInteractor {

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
        if (!Resources.createVLDataFolder(context)) {
            return false;
        }

        boolean dataAssetsUnpackedSuccess = Resources
                .createFilesFromAssetFolder(context, Resources.VL_DATA_PACK);

        Timber.d("ASSETS UNPACKED: " + dataAssetsUnpackedSuccess);

        if (!dataAssetsUnpackedSuccess) {
            Timber.d("COULDN'T UNPACK RESOURCES FROM ASSETS");
            return false;
        }

        if (!FaceEngineJNI.initFaceEngine(context.getFilesDir() + "/vl/data")) {
            Timber.d("COULDN'T INIT FACE ENGINE BY PATH: " + context.getFilesDir() + "/vl/data");
            return false;
        } else {
            Timber.d("SUCCESSFULLY INITED FACE ENGINE FROM PATH: " + context.getFilesDir()
                    + "/vl/data");
        }

        return true;
    }
}
