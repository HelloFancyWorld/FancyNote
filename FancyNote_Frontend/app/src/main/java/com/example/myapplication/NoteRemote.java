package com.example.myapplication;
import java.io.Serializable;
import java.util.ArrayList;

public class NoteRemote implements Serializable {

    private int id;
    private String title;
    private String tag;
    private ArrayList<NoteContent> contents;
    private String created_at;
    private String updated_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<NoteContent> getContents() {
        return contents;
    }

    public void setContents(ArrayList<NoteContent> contents) {
        this.contents = contents;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getAbstract() {
        //摘要：第一个edittext的前两行
        String Abstract = "<空>";
        for (NoteContent item : contents) {
            if (item.getType() == NoteItem.TYPE_TEXT) {
                String textContent = item.getTextContent().getText();
                // Return the first ten characters or the entire string if it's shorter
                Abstract = textContent;
                return Abstract;
            }
        }
        for (NoteContent item : contents) {
            if (item.getType() == NoteItem.TYPE_IMAGE) {
                Abstract = "<图片>";
                return Abstract;
            }
            else {
                Abstract = "<音频>";
                return Abstract;
            }
        }
        return Abstract;
    }

}