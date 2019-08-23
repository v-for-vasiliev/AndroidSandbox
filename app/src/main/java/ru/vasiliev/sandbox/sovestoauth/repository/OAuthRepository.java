package ru.vasiliev.sandbox.sovestoauth.repository;

import ru.vasiliev.sandbox.sovestoauth.domain.model.OAuthResponse;
import ru.vasiliev.sandbox.sovestoauth.repository.datasource.OAuthApi;
import rx.Observable;

public class OAuthRepository {

    private OAuthApi mOAuthApi;

    public OAuthRepository(OAuthApi api) {
        mOAuthApi = api;
    }

    public Observable<OAuthResponse> getSms(String responseType, String phone, String fingerprint) {
        return mOAuthApi.getSms(responseType, phone, fingerprint);
    }

    public Observable<OAuthResponse> submitSms(String responseType, String code, String vcode) {
        return mOAuthApi.submitSms(responseType, code, vcode);
    }

    public Observable<OAuthResponse> getToken(String grandType, String refreshToken) {
        return mOAuthApi.getToken(grandType, refreshToken);
    }

    public Observable<OAuthResponse> refreshToken(String grandType, String refreshToken) {
        return mOAuthApi.refreshToken(grandType, refreshToken);
    }
}
