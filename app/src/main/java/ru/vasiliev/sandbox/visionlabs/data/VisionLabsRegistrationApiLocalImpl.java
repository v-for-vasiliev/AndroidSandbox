package ru.vasiliev.sandbox.visionlabs.data;

import android.util.Base64;
import ru.visionlab.faceengine.PhotoProcessor;

public class VisionLabsRegistrationApiLocalImpl implements VisionLabsRegistrationApi {

    private VisionLabsRegistrationApi.Listener mListener;

    private PhotoProcessor mPhotoProcessor;

    private VisionLabsPreferences mPreferences;

    public static class DescriptorNotExtractedException extends RuntimeException {

        public DescriptorNotExtractedException(String detailMessage) {
            super(detailMessage);
        }
    }

    public VisionLabsRegistrationApiLocalImpl(PhotoProcessor photoProcessor,
                                              VisionLabsPreferences preferences) {
        mPhotoProcessor = photoProcessor;
        mPreferences = preferences;
    }

    @Override
    public VisionLabsRegistrationApi setListener(VisionLabsRegistrationApi.Listener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public void registerPerson() {
        mPhotoProcessor.calcFaceDescriptorFromBestFrame();
        byte[] bestFrameDescriptorByteArray = mPhotoProcessor.getFaceDescriptorByteArray();

        if (bestFrameDescriptorByteArray == null || bestFrameDescriptorByteArray.length == 0) {
            onFail(new DescriptorNotExtractedException(
                    "FAILED to register: could not extract descriptor "));
        } else {
            String descriptorEncrypted = Base64
                    .encodeToString(bestFrameDescriptorByteArray, Base64.DEFAULT);
            onSuccess(descriptorEncrypted);
        }
    }

    private void onFail(Throwable throwable) {
        if (mListener != null) {
            mListener.onRegistrationFail(throwable);
        }
    }

    private void onSuccess(String descriptor) {
        mPreferences.setAuthDescriptor(descriptor);
        if (mListener != null) {
            mListener.onRegistrationSuccess();
        }
    }
}
