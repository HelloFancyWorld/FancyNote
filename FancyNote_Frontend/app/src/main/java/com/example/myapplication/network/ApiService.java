package com.example.myapplication.network;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @GET("/api/get-csrf-token/")
    Call<CsrfTokenResponse> getCsrfToken();

    @POST("/api/signup/")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);

    @POST("/api/login/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/logout/")
    Call<LogoutResponse> logout();

    @Multipart
    @POST("/api/change_avatar/")
    Call<UploadAvatarResponse> uploadAvatar(@Part MultipartBody.Part avatar);

    @POST("notes/")
    Call<NoteResponse> createNote(@Body Note note);
}