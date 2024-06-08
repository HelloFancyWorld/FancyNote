package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "my_notepad1";//表名
    public static final String USERID="user";
    public static final String ID = "_id";//id
    public static final String TIME = "time";//时间
    public static final String TITLE = "title";//标题
    public static final String CONTENT = "content";//文字内容
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String AUDIO_URL = "audio_url";
    public static final String TAG = "tag";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TIME + " TEXT NOT NULL," +
                TITLE + " TEXT NOT NULL,"+
                USERID + " INTEGER,"+
                CONTENT + " TEXT NOT NULL,"  +
                TAG + " TEXT NOT NULL"+
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
