package com.example.myapplication.network;

import com.example.myapplication.NoteContent;

import java.util.List;

public class NoteResponse {
    private boolean success;
    private int id;
    private String title;
    private String created_at;
    private String updated_at;
    private List<NoteContent> contents;

    public boolean isSuccess() {
        return success;
    }

    public List<NoteContent> getContents() {
        return contents;
    }
}
