package com.example.myapplication.network;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    @POST("/api/update_user_info/")
    Call<UpdateInfoResponse> updateUserInfo(@Body UpdateInfoRequest request);

    @POST("/api/notes/")
    Call<NoteResponse> createNote(@Body NoteRequest noteRequest);


    @Multipart
    @POST("/api/content/upload/")
    Call<UploadFileResponse> uploadFile(
            @Part MultipartBody.Part file,
            @Part("content_id") RequestBody contentId,
            @Part("type") RequestBody type
    );
}