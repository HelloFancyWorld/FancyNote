package com.example.myapplication.network;

public class UploadAvatarResponse {
    private boolean success;
    private String new_avatar_url;
    private String message;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAvatarUrl() {
        return new_avatar_url;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.new_avatar_url = avatarUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
