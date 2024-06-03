package com.example.myapplication.network;

import com.example.myapplication.NoteItem;

import java.util.ArrayList;
import java.util.List;

public class NoteRequest {
    private String title;
    private String created_at;
    private String updated_at;
    private ArrayList<NoteItem> contents;

    public NoteRequest(String title, String created_at, String updated_at, ArrayList<NoteItem> contents) {
        this.title = title;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.contents = contents;
    }
}
