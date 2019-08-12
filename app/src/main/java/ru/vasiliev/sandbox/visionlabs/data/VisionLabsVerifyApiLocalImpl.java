package ru.vasiliev.sandbox.visionlabs.data;

import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResult;
import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResultPerson;
import ru.visionlab.faceengine.PhotoProcessor;

public class VisionLabsVerifyApiLocalImpl implements VisionLabsVerifyApi {

    private VisionLabsVerifyApi.Listener mListener;

    private PhotoProcessor mPhotoProcessor;

    private VisionLabsPreferences mPreferences;

    public VisionLabsVerifyApiLocalImpl(PhotoProcessor photoProcessor,
            VisionLabsPreferences preferences) {
        mPhotoProcessor = photoProcessor;
        mPreferences = preferences;
    }

    @Override
    public void setListener(VisionLabsVerifyApi.Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void verifyPerson() {
        mPhotoProcessor.calcFaceDescriptorFromBestFrame();

        byte[] bestFrameDescriptorByteArray = mPhotoProcessor.getFaceDescriptorByteArray();

        if (bestFrameDescriptorByteArray == null || bestFrameDescriptorByteArray.length == 0) {
            onFail(new VisionLabsRegistrationApiLocalImpl.DescriptorNotExtractedException(
                    "Registration failed: could not extract descriptor "));
            return;
        }

        byte[] savedDescriptorByteArray = Base64
                .decode(mPreferences.getAuthDescriptor(), Base64.DEFAULT);

        float similarity = mPhotoProcessor.matchDescriptors(bestFrameDescriptorByteArray,
                savedDescriptorByteArray); // Check for similarity

        System.out.println("Similarity is :  " + similarity);

        // Fill fake search result for compatibility
        List<SearchResultPerson> personsSearchList = new ArrayList<>();

        SearchResultPerson person = new SearchResultPerson();
        person.similarity = similarity;

        personsSearchList.add(person);

        SearchResult searchResult = new SearchResult();
        searchResult.setPersons(personsSearchList);

        onSuccess(searchResult);
    }

    private void onFail(Throwable throwable) {
        if (mListener != null) {
            mListener.onVerificationFail(throwable);
        }
    }

    private void onSuccess(SearchResult searchResult) {
        if (mListener != null) {
            mListener.onVerificationSuccess(searchResult);
        }
    }
}
