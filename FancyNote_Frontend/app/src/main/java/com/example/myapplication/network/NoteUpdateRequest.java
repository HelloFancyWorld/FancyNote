package com.example.myapplication.network;

import com.example.myapplication.NoteItem;

import java.util.ArrayList;

public class NoteUpdateRequest {
    private int id;
    private String title;
    private String created_at;
    private String updated_at;
    private ArrayList<NoteItem> contents;

    public NoteUpdateRequest(int id, String title, String created_at, String updated_at, ArrayList<NoteItem> contents) {
        this.id = id;
        this.title = title;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.contents = contents;
    }
}