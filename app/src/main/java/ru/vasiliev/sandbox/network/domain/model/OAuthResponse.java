package ru.vasiliev.sandbox.network.domain.model;

import com.google.gson.annotations.SerializedName;

public class OAuthResponse {

    @SerializedName("access_token")
    String mAccessToken;

    @SerializedName("token_type")
    String mTokenType;

    @SerializedName("expires_in")
    String mExpiresIn;

    @SerializedName("refresh_token")
    String mRefreshToken;

    @SerializedName("confirmation_id")
    String mConfirmationId;

    @SerializedName("code")
    String mConfirmationCode;

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public String getTokenType() {
        return mTokenType;
    }

    public void setTokenType(String tokenType) {
        mTokenType = tokenType;
    }

    public String getExpiresIn() {
        return mExpiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        mExpiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public String getConfirmationId() {
        return mConfirmationId;
    }

    public void setConfirmationId(String confirmationId) {
        mConfirmationId = confirmationId;
    }

    public String getConfirmationCode() {
        return mConfirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        mConfirmationCode = confirmationCode;
    }
}

