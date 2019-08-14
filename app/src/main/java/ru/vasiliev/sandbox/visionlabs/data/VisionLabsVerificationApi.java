package ru.vasiliev.sandbox.visionlabs.data;


import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResult;

public interface VisionLabsVerificationApi {

    void verifyPerson();

    VisionLabsVerificationApi setListener(Listener listener);

    interface Listener {

        void onVerificationSuccess(SearchResult searchResult);

        void onVerificationFail(Throwable throwable);
    }
}
