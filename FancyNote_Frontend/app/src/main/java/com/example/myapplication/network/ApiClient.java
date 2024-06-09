package com.example.myapplication.network;

import android.os.Build;
import android.provider.Settings;

import com.example.myapplication.FancyNote;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static CsrfInterceptor csrfInterceptor = new CsrfInterceptor(null, null);

    private static String getBaseUrl() {
        if (isEmulator()) {
            return "http://10.0.2.2:8000";
        } else {
            return "http://59.66.139.41:8000";
        }
    }

    private static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.toLowerCase().contains("droid4x") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT) ||
                (Settings.Secure.getString(FancyNote.getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID) == null));
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(csrfInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
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
                .baseUrl(getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
