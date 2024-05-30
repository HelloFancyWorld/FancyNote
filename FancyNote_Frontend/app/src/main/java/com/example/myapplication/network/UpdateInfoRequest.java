package com.example.myapplication.network;

public class UpdateInfoRequest {
    private String nickname;
    private String email;
    private String motto;

    public UpdateInfoRequest(String nickname, String email, String motto) {
        this.nickname = nickname;
        this.email = email;
        this.motto = motto;
    }

    // Getters and setters
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }
}
