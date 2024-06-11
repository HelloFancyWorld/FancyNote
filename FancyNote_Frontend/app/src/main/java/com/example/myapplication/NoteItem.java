package com.example.myapplication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NoteItem implements Serializable {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2;

    private int id = -1;  // Default to -1 indicating no ID
    private int type;
    private String content;

    public NoteItem(int id, int type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    public NoteItem(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // id是否存在
    public boolean hasId() {
        return id != -1;
    }

    // 自定义映射
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (hasId()) {
            map.put("id", id);
        }
        map.put("type", type);
        map.put("content", content);
        return map;
    }
}


