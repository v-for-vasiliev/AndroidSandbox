package ru.vasiliev.sandbox.sovestoauth.domain.model;

public class CredentialsStorage {

    public CredentialsStorage() {

    }

    private String mPhone;

    private String mFirstName;

    private String mLastName;

    private String mMiddleName;

    private String mUserID;

    private String mTokenCode;

    private String mAccessToken;

    private String mRefreshToken;

    private String mConfirmationKey;

    private String mAuthUserId;

    private String mFingerprint;

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getMiddleName() {
        return mMiddleName;
    }

    public void setMiddleName(String middleName) {
        mMiddleName = middleName;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getTokenCode() {
        return mTokenCode;
    }

    public void setTokenCode(String tokenCode) {
        mTokenCode = tokenCode;
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

    public String getConfirmationKey() {
        return mConfirmationKey;
    }

    public void setConfirmationKey(String confirmationKey) {
        mConfirmationKey = confirmationKey;
    }

    public String getAuthUserId() {
        return mAuthUserId;
    }

    public void setAuthUserId(String authUserId) {
        mAuthUserId = authUserId;
    }

    public String getFingerprint() {
        return mFingerprint;
    }

    public void setFingerprint(String fingerprint) {
        mFingerprint = fingerprint;
    }
}
