package com.example.myapplication.network;

import java.util.List;

private class Note {
    private String title;
    private List<TextContent> textcontent_set;
    private List<ImageContent> imagecontent_set;
    private List<AudioContent> audiocontent_set;

    // getters and setters
}

private class TextContent {
    private int order;
    private String text;

    // getters and setters
}

private class ImageContent {
    private int order;
    private String image;

    // getters and setters
}

class AudioContent {
    private int order;
    private String audio;

    // getters and setters
}

class NoteResponse {
    // Define fields for the response if necessary
    // getters and setters
}
