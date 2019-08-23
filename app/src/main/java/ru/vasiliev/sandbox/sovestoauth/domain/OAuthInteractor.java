package ru.vasiliev.sandbox.sovestoauth.domain;

import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.sovestoauth.repository.OAuthRepository;
import ru.vasiliev.sandbox.sovestoauth.utils.DeviceUtils;

public class OAuthInteractor {

    private OAuthRepository mOAuthRepository;

    private App mApp;

    private String mRefreshToken;

    public OAuthInteractor(OAuthRepository repository, App app) {
        mOAuthRepository = repository;
        mApp = app;
    }

    public boolean auth(String phone) {
        String fingerprint = DeviceUtils.generateFingerprint(mApp);
        if (fingerprint == null) {
            return false;
        }
        mOAuthRepository.getSms("code", phone, DeviceUtils.generateFingerprint(mApp));
        return true;
    }
}
