package com.example.myapplication;

public class Item {
    private int order;
    private String type;
    private String content;

    // Constructor
    public Item(int order, String type, String content) {
        this.order = order;
        this.type = type;
        this.content = content;
    }

    // Getters and Setters
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

