package ru.vasiliev.sandbox.visionlabs.data;

public interface VisionLabsRegistrationApi {

    void registerPerson();

    VisionLabsRegistrationApi setListener(Listener listener);

    interface Listener {

        void onRegistrationSuccess();

        void onRegistrationFail(Throwable throwable);
    }
}
