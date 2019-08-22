package ru.vasiliev.sandbox.sovestoauth.repository.datasource;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.vasiliev.sandbox.sovestoauth.domain.model.OAuthResponse;
import rx.Observable;

public interface OAuthApi {

    @FormUrlEncoded
    @POST("oauth/authorize")
    Observable<OAuthResponse> getSms(@Field("response_type") String responseType,
            @Field("username") String phone, @Field("device_fingerprint") String fingerprint);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> submitSms(@Field("grant_type") String responseType,
            @Field("code") String code, @Field("vcode") String vcode);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> getToken(@Field("grant_type") String grandType,
            @Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> refreshToken(@Field("grant_type") String grandType,
            @Field("refresh_token") String refreshToken);
}
