package ru.vasiliev.sandbox.network.repository.datasource;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.vasiliev.sandbox.network.domain.model.OAuthResponse;

public interface OAuthApi {

    @FormUrlEncoded
    @POST("oauth/authorize")
    Observable<OAuthResponse> getSms(@Field("response_type") String responseType,
            @Field("username") String phone, @Field("device_fingerprint") String fingerprint);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> submitSms(@Field("grant_type") String responseType,
            @Field("code") String confirmationKey, @Field("vcode") String vcode);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> getToken(@Field("grant_type") String grandType,
            @Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> refreshToken(@Field("grant_type") String grandType,
            @Field("refresh_token") String refreshToken);
}
