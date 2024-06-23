package com.example.myapplication.network;

public class AIRequest {
    private String text;

    public AIRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
