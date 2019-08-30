package ru.vasiliev.sandbox.network.repository;

import io.reactivex.Observable;
import ru.vasiliev.sandbox.network.domain.model.OAuthResponse;
import ru.vasiliev.sandbox.network.repository.datasource.OAuthApi;

public class OAuthRepository {

    private OAuthApi mOAuthApi;

    public OAuthRepository(OAuthApi api) {
        mOAuthApi = api;
    }

    public Observable<OAuthResponse> getSms(String responseType, String phone, String fingerprint) {
        return mOAuthApi.getSms(responseType, phone, fingerprint);
    }

    public Observable<OAuthResponse> submitSms(String responseType, String confirmationKey, String vcode) {
        return mOAuthApi.submitSms(responseType, confirmationKey, vcode);
    }

    public Observable<OAuthResponse> getToken(String grandType, String refreshToken) {
        return mOAuthApi.getToken(grandType, refreshToken);
    }

    public Observable<OAuthResponse> refreshToken(String grandType, String refreshToken) {
        return mOAuthApi.refreshToken(grandType, refreshToken);
    }
}
