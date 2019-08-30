package ru.vasiliev.sandbox.network.repository.datasource;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface UpdateCertificateApi {

    @GET("/cert/index.sha")
    Call<ResponseBody> getSHA();

    @GET("/cert/index.xml")
    Call<ResponseBody> getCertificates();

    @GET("/cert/{filename}")
    @Streaming
    Call<ResponseBody> downloadCert(@Path("filename") String filename);
}