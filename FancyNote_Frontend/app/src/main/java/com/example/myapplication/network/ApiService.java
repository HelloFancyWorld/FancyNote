package com.example.myapplication.network;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface ApiService {

    @GET("/api/get-csrf-token/")
    Call<CsrfTokenResponse> getCsrfToken();

    @POST("/api/signup/")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);

    @POST("/api/login/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/logout/")
    Call<LogoutResponse> logout();
}