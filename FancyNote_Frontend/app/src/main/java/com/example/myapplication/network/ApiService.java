package com.example.myapplication.network;


import com.example.myapplication.Note;
import com.example.myapplication.NoteRemote;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

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

    @POST("/api/update_password/")
    Call<UpdatePWResponse> updatePassword(@Body UpdatePWRequest request);

    @POST("/api/ai/")
    Call<AIResponse> generateAITag(@Body AIRequest request);

    @POST("/api/notes/")
    Call<NoteResponse> createNote(@Body NoteRequest noteRequest);


    @Multipart
    @POST("/api/content/upload/")
    Call<UploadFileResponse> uploadFile(
            @Part MultipartBody.Part file,
            @Part("content_id") RequestBody contentId,
            @Part("type") RequestBody type
    );

    @GET("/api/notes/")
    Call<List<NoteRemote>> getNoteList();


    @DELETE("/api/notes/{id}/")
    Call<Void> deleteNote(@Path("id") int id);

    @GET("/api/notes/{id}/")
    Call<NoteResponse> getNote(@Path("id") int id);

    @PATCH("/api/notes/{id}/")
    Call<NoteResponse> editNote(@Path("id") int id, @Body NoteRequest noteRequest);
}