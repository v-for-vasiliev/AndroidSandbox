package ru.vasiliev.sandbox.network.domain.model;

import org.joda.time.DateTime;

public class CredentialsStorage {

    public CredentialsStorage() {

    }

    private String mFingerprint;

    private String mPhone;

    private String mConfirmationCode;

    private String mAccessToken;

    private String mRefreshToken;

    private DateTime mRefreshTokenExpirationTime;

    public String getFingerprint() {
        return mFingerprint;
    }

    public void setFingerprint(String fingerprint) {
        mFingerprint = fingerprint;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getConfirmationCode() {
        return mConfirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        mConfirmationCode = confirmationCode;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public DateTime getRefreshTokenExpirationTime() {
        return mRefreshTokenExpirationTime;
    }

    public void setRefreshTokenExpirationTime(DateTime refreshTokenExpirationTime) {
        mRefreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public boolean isRefreshTokenExpired() {
        return mRefreshTokenExpirationTime != null && mRefreshTokenExpirationTime.isBeforeNow();
    }

    public void reset() {
        mFingerprint = null;
        mPhone = null;
        mConfirmationCode = null;
        mAccessToken = null;
        mRefreshToken = null;
        mRefreshTokenExpirationTime = null;
    }
}
