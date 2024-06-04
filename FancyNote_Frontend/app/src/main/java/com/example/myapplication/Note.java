package com.example.myapplication;
import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {

    private int id;
    private int account;
    private String title;
    private String Abstract;
    private ArrayList<NoteItem> content;
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
    public int getUserid(){ return account; }
    public void setUserid(int user){this.account=user;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<NoteItem> getContent() {
        return content;
    }

    public void setContent(ArrayList<NoteItem> content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getAbstract() {
        return Abstract;
    }

    public void setAbstract(String Abstract) {
        this.Abstract = Abstract;
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }



}