package ru.vasiliev.sandbox.sovestoauth.domain.model;

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
    String mCode;

    public String getAccessToken() {
        return mAccessToken;
    }

    public String getTokenType() {
        return mTokenType;
    }

    public String getExpiresIn() {
        return mExpiresIn;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getConfirmationId() {
        return mConfirmationId;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        this.mCode = code;
    }

    public void setRefreshToken(String refreshToken) {
        this.mRefreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }
}

