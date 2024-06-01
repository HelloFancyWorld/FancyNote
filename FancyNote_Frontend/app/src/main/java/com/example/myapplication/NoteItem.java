package com.example.myapplication;

public class NoteItem {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2;

    private int type;
    private String content;
    private String imagePath;
    private String audioPath;

    public NoteItem(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public NoteItem(int type, String content, String imagePath) {
        this.type = type;
        this.content = content;
        this.imagePath = imagePath;
    }

    public NoteItem(int type, String content, String imagePath, String audioPath) {
        this.type = type;
        this.content = content;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}

