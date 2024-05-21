package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

public class CsrfTokenResponse {
    @SerializedName("csrfToken")
    private String csrfToken;

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }
}
