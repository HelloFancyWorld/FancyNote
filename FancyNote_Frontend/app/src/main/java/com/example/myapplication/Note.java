package com.example.myapplication;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Note implements Serializable {

    private int id;
    private String title;
    private String Tag;
    private ArrayList<NoteItem> content;
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
        return Tag;
    }

    public void setTag(String Tag) {
        this.Tag = Tag;
    }

    public ArrayList<NoteItem> getContent() {
        return content;
    }

    public void setContent(ArrayList<NoteItem> content) {
        this.content = content;
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
        for (NoteItem item : content) {
            if (item.getType() == NoteItem.TYPE_TEXT) {
                if(Objects.equals(item.getContent(), ""))
                    continue;
                Abstract = item.getContent();
                return Abstract;
            }
        }
        for (NoteItem item : content) {
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