package com.example.myapplication.network;

public class UploadFileResponse {
    private boolean success;
    private String new_file_url;
    private String message;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAvatarUrl() {
        return new_file_url;
    }

    public void setAvatarUrl(String fileUrl) {
        this.new_file_url = fileUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
