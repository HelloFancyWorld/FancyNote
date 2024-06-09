package com.example.myapplication;

import android.app.Application;
import android.content.Context;

public class FancyNote extends Application {
    private static FancyNote instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static FancyNote getInstance() {
        return instance;
    }


    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
