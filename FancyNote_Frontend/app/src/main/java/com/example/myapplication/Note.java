package com.example.myapplication;
import java.io.Serializable;
public class Note implements Serializable {

    private int id;

    private String title;
    private String content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getImagePath() {
        return imagePath;
    }
    public String getAudioPath() {
        return audioPath;
    }

    public String getTag() {
        return tag;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }



}