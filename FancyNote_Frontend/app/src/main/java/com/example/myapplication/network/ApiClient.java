package com.example.myapplication.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static CsrfInterceptor csrfInterceptor = new CsrfInterceptor(null, null);

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(csrfInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8000")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit updateCsrfTokenAndCookie(String csrfToken, String cookie) {
        csrfInterceptor.setCsrfToken(csrfToken);
        csrfInterceptor.setCookie(cookie);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(csrfInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
