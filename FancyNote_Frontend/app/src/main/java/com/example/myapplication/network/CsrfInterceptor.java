package com.example.myapplication.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CsrfInterceptor implements Interceptor {
    private String csrfToken;
    private String cookie;

    public CsrfInterceptor(String csrfToken, String cookie) {
        this.csrfToken = csrfToken;
        this.cookie = cookie;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (csrfToken != null) {
            requestBuilder.addHeader("X-CSRFToken", csrfToken);
        }

        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
