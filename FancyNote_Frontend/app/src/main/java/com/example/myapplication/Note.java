package com.example.myapplication;
import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {

    private int id;
    private int account;
    private String title;
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
        //摘要：第一个edittext的前两行
        String Abstract = "<空>";
        for (NoteItem item : content) {
            if (item.getType() == NoteItem.TYPE_TEXT) {
                String textContent = item.getcontent();
                // Return the first ten characters or the entire string if it's shorter
                Abstract = textContent;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }



}