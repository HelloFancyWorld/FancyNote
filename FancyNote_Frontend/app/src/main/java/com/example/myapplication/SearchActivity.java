package com.example.myapplication;

import static com.example.myapplication.NoteItem.TYPE_TEXT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.ui.theme.CircleWithBorderTransformation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {
    private NoteListAdapter mAdapter;
    private RecyclerView rcvNoteList;//备忘列表
    private SearchView searchView;
    private SharedPreferences sharedPreferences;
    private String Tag="";
    private EditText tv_edit;
    private List<Note> noteList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);// 添加默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
        Intent intent = getIntent();
        Tag = intent.getStringExtra("note");

        initCommonViews();
    }
    @Override
    protected void onResume() {
        super.onResume();

        noteList.clear();
        queryNotes();
    }
    private void initCommonViews() {

        searchView=findViewById(R.id.tv_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户提交搜索查询时调用此方法
                searchNotes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 当搜索查询文本改变时调用此方法
                // 这里可以实现实时搜索
                return false;
            }
        });

        rcvNoteList = (RecyclerView) findViewById(R.id.searchNoteList);
    }
    private void searchNotes(String text) {
        // 假设你使用Room数据库
        List<Note> tmpList = new ArrayList<>();
        for(int i=0;i<noteList.size();i++) {
            ArrayList<NoteItem> content = noteList.get(i).getContent();
            for (int j = 0; j < content.size(); j++) {
                NoteItem noteItem = content.get(j);
                if (noteItem.getType() == TYPE_TEXT) {
                    if(noteItem.getContent().contains(text)){
                        tmpList.add(noteList.get(i));
                        break;
                    }
                }
            }
        }
        mAdapter = new NoteListAdapter(this, tmpList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvNoteList.setLayoutManager(manager);
        rcvNoteList.setAdapter(mAdapter);
        //添加分隔线
        rcvNoteList.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
    }
    private void queryNotes() {
        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        apiService.getNoteList().enqueue(new Callback<List<NoteRemote>>() {
            @Override
            public void onResponse(@NonNull Call<List<NoteRemote>> call, @NonNull Response<List<NoteRemote>> response) {
                Log.d("API_RESPONSE", "Response received");
                // Hide loading dialog or UI indication
                if (response.isSuccessful()) {
                    List<NoteRemote> notes = response.body();
                    //类型转换
                    for(NoteRemote noteRemote: notes) {
                        Note note = new Note();
                        note.setId(noteRemote.getId());
                        note.setTitle(noteRemote.getTitle());
                        note.setUpdated_at(noteRemote.getUpdated_at());
                        ArrayList<NoteContent> noteContents = new ArrayList<>();
                        noteContents = noteRemote.getContents();
                        ArrayList<NoteItem> noteItems = new ArrayList<>();
                        for(NoteContent noteContent: noteContents) {
                            int id = noteContent.getId();
                            int type = noteContent.getType();
                            String contentValue = "";
                            switch (type) {
                                case 0:
                                    if (noteContent.getTextContent() != null) {
                                        contentValue = noteContent.getTextContent().getText();
                                    }
                                    break;
                                case 1:
                                    if (noteContent.getImageContent() != null) {
                                        contentValue = noteContent.getImageContent().getImageUrl();
                                    }
                                    break;
                                case 2:
                                    if (noteContent.getAudioContent() != null) {
                                        contentValue = noteContent.getAudioContent().getAudioUrl();
                                    }
                                    break;
                                default:
                                    break;
                            }
                            noteItems.add(new NoteItem(id, type, contentValue));
                        }
                        note.setContent(noteItems);
                        noteList.add(note);
                    }

                    //加入到页面中
                }
            }

            @Override
            public void onFailure(Call<List<NoteRemote>> call, Throwable t) {
                // Hide loading dialog or UI indication
                Toast.makeText(SearchActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
