package ru.vasiliev.sandbox.network.domain;

import org.joda.time.DateTime;

import io.reactivex.Observable;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.network.domain.model.CredentialsStorage;
import ru.vasiliev.sandbox.network.domain.model.OAuthResponse;
import ru.vasiliev.sandbox.network.repository.OAuthRepository;
import ru.vasiliev.sandbox.network.utils.DeviceUtils;

public class OAuthInteractor {

    private OAuthRepository mOAuthRepository;

    private CredentialsStorage mCredentialsStorage;

    private App mApp;

    public OAuthInteractor(OAuthRepository repository, CredentialsStorage credentialsStorage,
            App app) {
        mOAuthRepository = repository;
        mCredentialsStorage = credentialsStorage;
        mApp = app;
    }

    public Observable<OAuthResponse> getSms(String phone) {
        String fingerprint = DeviceUtils.generateFingerprint(mApp);
        if (fingerprint == null) {
            return Observable.error(new RuntimeException("Null fingerprint"));
        }
        return mOAuthRepository.getSms("code", phone, fingerprint).doOnNext(oAuthResponse -> {
            mCredentialsStorage.setPhone(phone);
            mCredentialsStorage.setConfirmationCode(oAuthResponse.getConfirmationCode());
            mCredentialsStorage.setFingerprint(fingerprint);
        });
    }

    public Observable<OAuthResponse> submitSms(String code) {
        return mOAuthRepository.submitSms("urn:qiwi:oauth:grant-type:vcode",
                mCredentialsStorage.getConfirmationCode(), code).doOnNext(oAuthResponse -> {
            mCredentialsStorage.setRefreshToken(oAuthResponse.getRefreshToken());
            mCredentialsStorage.setAccessToken(oAuthResponse.getAccessToken());
            try {
                mCredentialsStorage.setRefreshTokenExpirationTime(
                        DateTime.now().plusSeconds(Integer.parseInt(oAuthResponse.getExpiresIn())));
            } catch (final Throwable ignore) {
            }
        });
    }
}
