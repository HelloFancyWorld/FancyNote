package com.example.myapplication.network;

import com.example.myapplication.NoteRemote;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NoteListResponse {
    private List<NoteRemote> notes;

    public List<NoteRemote> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteRemote> notes) {
        this.notes = notes;
    }
}
