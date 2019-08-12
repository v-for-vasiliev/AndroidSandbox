package ru.vasiliev.sandbox.visionlabs.data;

public interface VisionLabsRegistrationApi {

    void registerPerson();

    void setListener(Listener listener);

    interface Listener {

        void onRegistrationSuccess();

        void onRegistrationFail(Throwable throwable);
    }
}
