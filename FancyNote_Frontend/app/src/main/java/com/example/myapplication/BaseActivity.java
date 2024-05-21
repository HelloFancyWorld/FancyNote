package com.example.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.example.myapplication.DatabaseHelper;



public class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    protected DatabaseHelper myNoteDBHelper;
    protected SQLiteDatabase writableDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myNoteDBHelper = new DatabaseHelper(this);
        writableDB = myNoteDBHelper.getWritableDatabase();
    }

    @Override
    public void onClick(View v) {

    }
}