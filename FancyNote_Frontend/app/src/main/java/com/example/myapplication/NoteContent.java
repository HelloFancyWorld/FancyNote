package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class NoteContent {
    private int id;
    private int type;
    @SerializedName("text_content")
    private TextContent textContent;

    @SerializedName("image_content")
    private ImageContent imageContent;

    @SerializedName("audio_content")
    private AudioContent audioContent;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public ImageContent getImageContent() { return imageContent; }
    public void setImageContent(ImageContent imageContent) { this.imageContent = imageContent; }

    public AudioContent getAudioContent() { return audioContent; }
    public void setAudioContent(AudioContent audioContent) { this.audioContent = audioContent; }

    public TextContent getTextContent() {
        return textContent;
    }

    public void setTextContent(TextContent textContent) {
        this.textContent = textContent;
    }
}

