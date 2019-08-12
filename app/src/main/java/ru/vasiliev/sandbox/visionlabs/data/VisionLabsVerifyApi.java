package ru.vasiliev.sandbox.visionlabs.data;


import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResult;

public interface VisionLabsVerifyApi {

    void verifyPerson();

    void setListener(Listener listener);

    interface Listener {

        void onVerificationSuccess(SearchResult searchResult);

        void onVerificationFail(Throwable throwable);
    }
}
