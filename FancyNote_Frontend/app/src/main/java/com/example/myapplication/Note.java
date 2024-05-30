package com.example.myapplication;
import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {

    private int id;

    private String title;
    private ArrayList<Item> content;
    private String time;
    private String imagePath;
    private String audioPath;
    private String tag;


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

    public ArrayList<Item> getContent() {
        return content;
    }

    public void setContent(ArrayList<Item> content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }



}