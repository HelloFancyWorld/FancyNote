package com.example.myapplication.network;

import com.example.myapplication.NoteContent;

import java.util.List;

public class NoteResponse {
    private int id;
    private String tag;
    private String title;
    private String created_at;
    private String updated_at;
    private List<NoteContent> contents;

    public List<NoteContent> getContents() {
        return contents;
    }

    public String getTag() {
        return tag;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
