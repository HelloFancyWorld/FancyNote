package com.example.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.NoteListAdapter;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.Note;
import com.example.myapplication.MyDecoration;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.LogoutResponse;
import com.example.myapplication.network.SignupResponse;
import com.example.myapplication.network.UploadAvatarResponse;
import com.example.myapplication.ui.theme.CircleWithBorderTransformation;
import com.google.android.material.navigation.NavigationView;
import com.yalantis.ucrop.util.FileUtils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private Toolbar toolbar;
    private TextView tv_add,tv_list,tv_title;//右上角的添加
    private RecyclerView rcvNoteList;//备忘列表
    private DrawerLayout drawer;
    public int Tag=0;
    private NoteListAdapter mAdapter;
    private static final String TAG = "MyActivityTag";
    private SearchView searchView;
    private NavigationView navigationView;
    private ImageView iv_avatar;
    private ImageView user_info_avatar;
    private NavigationView user_info;
    private Button signup_button, login_button, logout_button, editinfo_button;
    private TextView user_info_username, user_info_motto, user_info_email;
    private SharedPreferences sharedPreferences;
    private List<Note> noteList = new ArrayList<>();

    private String username;
    private String email;
    private String avatarUrl;
    private String motto;


    // 用于头像选择
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取SharedPreferences实例
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // 检查登录状态
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);


        username = sharedPreferences.getString("username", null);
        email = sharedPreferences.getString("email", null);
        avatarUrl = sharedPreferences.getString("avatar", null);
        motto = sharedPreferences.getString("motto", null);

        if (isLoggedIn) {
            initCommonViews();
            initLoggedViews();
        } else {
            initCommonViews();
            initNotLoggedViews();
        }

        setToolbar();
    }


    @Override
    protected void onResume() {
        super.onResume();

        noteList.clear();
        queryNotes();
        mAdapter = new NoteListAdapter(this, noteList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvNoteList.setLayoutManager(manager);
        rcvNoteList.setAdapter(mAdapter);
        //添加分隔线
        rcvNoteList.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        tv_title.setText("我的简介");
    }

    private void initLoggedViews() {
        // Initialize the image picker launcher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                uploadAvatar(uri);
            }
        });
        user_info_avatar = findViewById(R.id.user_info_avatar);
        user_info_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAvatarMenu(view);
            }
        });

        user_info_username = findViewById(R.id.user_info_username);
        user_info_username.setText(username);

        user_info_motto = findViewById(R.id.user_info_motto);
        user_info_motto.setText(motto);

        LinearLayout user_info_motto_layout = findViewById(R.id.user_info_motto_layout);
        user_info_motto_layout.setVisibility(View.VISIBLE);

        user_info_email = findViewById(R.id.user_info_email);
        user_info_email.setText(email);

        LinearLayout user_info_email_layout = findViewById(R.id.user_info_email_layout);
        user_info_email_layout.setVisibility(View.VISIBLE);

        LinearLayout user_info_logged_buttons = findViewById(R.id.user_info_logged_buttons);
        user_info_logged_buttons.setVisibility(View.VISIBLE);

        Button edit_button = findViewById(R.id.edit_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUserInfo();
            }
        });

        logout_button = findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        editinfo_button = findViewById(R.id.edit_button);
        editinfo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void editUserInfo() {}
    private void logoutUser() {
        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);
        apiService.logout().enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                if (response.isSuccessful()) {
                    LogoutResponse logoutResponse = response.body();
                    if (logoutResponse.isSuccess()) {
                        // 清除保存的登录状态和用户信息
                        sharedPreferences.edit().remove("isLoggedIn").apply();
                        sharedPreferences.edit().remove("username").apply();
                        sharedPreferences.edit().remove("email").apply();
                        sharedPreferences.edit().remove("avatar").apply();
                        sharedPreferences.edit().remove("motto").apply();

                        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        // 刷新页面
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this, logoutResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initNotLoggedViews() {
        user_info_username = findViewById(R.id.user_info_username);
        user_info_username.setText("未登录");

        LinearLayout user_info_notlogged_buttons = findViewById(R.id.user_info_notlogged_buttons);
        user_info_notlogged_buttons.setVisibility(View.VISIBLE);

        signup_button = findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动 RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


        login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动 LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void initCommonViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_list=(TextView)findViewById(R.id.tv_list);
        tv_title=(TextView)findViewById(R.id.tv_title);
        drawer = findViewById(R.id.drawer_layout);

        user_info = findViewById(R.id.user_info);
        iv_avatar = findViewById(R.id.iv_avatar);
        int borderColor = Color.WHITE; // 边框颜色
        float borderWidth = 2f; // 边框宽度，单位是像素

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadImageWithGlide(avatarUrl, iv_avatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .apply(new RequestOptions().transform(new CircleWithBorderTransformation(borderColor, borderWidth)))
                    .into(iv_avatar);
        }

        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(user_info);
            }
        });

        user_info_avatar = findViewById(R.id.user_info_avatar);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadImageWithGlide(avatarUrl, user_info_avatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .apply(new RequestOptions().transform(new CircleWithBorderTransformation(borderColor, borderWidth)))
                    .into(user_info_avatar);
        }

        searchView = findViewById(R.id.tv_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 用户提交查询时调用
                Log.i(TAG, "onCreate: " + query);
                //searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 查询文本改变时调用
                return true;
            }
        });
        rcvNoteList = (RecyclerView) findViewById(R.id.rcvNoteList);

        tv_add.setOnClickListener(this);
        tv_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_first) {
                Tag=1;
                tv_title.setText("学习笔记");
                // 处理第一个菜单项的点击
            } else if (id == R.id.nav_second) {
                // 处理第二个菜单项的点击
                Tag=2;
                tv_title.setText("工作笔记");
            }
            else if (id == R.id.nav_third) {
                // 处理第二个菜单项的点击
                Tag=3;
                tv_title.setText("生活笔记");
            }
            query_current_type_Notes(Tag);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showAvatarMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.avatar_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_view_avatar) {
                viewAvatar();
                return true;
            } else if (id == R.id.action_change_avatar) {
                changeAvatar();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void viewAvatar() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_view_avatar);

        ImageView ivAvatar = dialog.findViewById(R.id.ivAvatar);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadImageWithGlide(avatarUrl, ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.default_avatar);
        }

        // 点击即关闭Dialog
        View root = dialog.findViewById(R.id.view_avatar);
        root.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void changeAvatar() {
        pickImageLauncher.launch("image/*");
    }

    private void uploadAvatar(Uri uri) {
        // Show a loading dialog or some UI indication

        // Assuming you have an ApiService and ApiClient similar to your logout functionality
        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        // Create a file from the Uri
        File file = new File(FileUtils.getPath(this, uri));
        MediaType mediaType = MediaType.parse(getContentResolver().getType(uri));
        RequestBody requestFile = RequestBody.create(file, mediaType);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        apiService.uploadAvatar(body).enqueue(new Callback<UploadAvatarResponse>() {
            @Override
            public void onResponse(Call<UploadAvatarResponse> call, Response<UploadAvatarResponse> response) {
                if (response.isSuccessful()) {
                    UploadAvatarResponse uploadResponse = response.body();
                    if (uploadResponse.isSuccess()) {
                        avatarUrl = uploadResponse.getAvatarUrl();
                        sharedPreferences.edit().putString("avatar", avatarUrl).apply();
                        loadImageWithGlide(avatarUrl, user_info_avatar);
                        Toast.makeText(MainActivity.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "头像上传失败: " + uploadResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
                }
                // Hide loading dialog or UI indication
            }

            @Override
            public void onFailure(Call<UploadAvatarResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Hide loading dialog or UI indication
            }
        });
    }

    private void setToolbar() {
        //设置导航图标要在setSupportActionBar方法之后
        Utils.initToolbar(this, toolbar, "", "", 0, null);//不设置icon

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //查询出来已经添加的备忘
    private void queryNotes() {
        Cursor cursor = writableDB.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null,null);
        while (cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TITLE));
            String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME));
            String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_URL));
            String audioPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AUDIO_URL));
            String tag=cursor.getString(cursor.getColumnIndex(DatabaseHelper.TAG));
            Note note = new Note();
            note.setId(id);
            note.setTitle(title);
            note.setTime(time);
            note.setTag(tag);
            noteList.add(note);
        }
        cursor.close();
    }
    private void query_current_type_Notes(int Tag) {
        Cursor cursor = writableDB.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null,null);
        noteList.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TITLE));
            String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME));
            String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_URL));
            String audioPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AUDIO_URL));
            String tag=cursor.getString(cursor.getColumnIndex(DatabaseHelper.TAG));
            if(String.valueOf(Tag).equals(tag)) {
                Note note = new Note();
                note.setId(id);
                note.setTitle(title);
                note.setTime(time);
                note.setTag(tag);
                noteList.add(note);
            }
        }
        cursor.close();
        mAdapter = new NoteListAdapter(this, noteList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvNoteList.setLayoutManager(manager);
        rcvNoteList.setAdapter(mAdapter);
        //添加分隔线
        rcvNoteList.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
    }
//    private void searchNotes(String text) {
//        // 假设你使用Room数据库
//        List<Note> tmpList = new ArrayList<>();
//        for(int i=0;i<noteList.size();i++) {
//                String content = noteList.get(i).getContent();
//                if (content.contains(text)) {
//                    Note note = noteList.get(i);
//                    tmpList.add(note);
//                }
//        }
//        mAdapter = new NoteListAdapter(this, tmpList);
//        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setOrientation(LinearLayoutManager.VERTICAL);
//        rcvNoteList.setLayoutManager(manager);
//        rcvNoteList.setAdapter(mAdapter);
//        //添加分隔线
//        rcvNoteList.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
//    }



    @Override
    public void onClick(View v) {
        showAddMenuDialog();
    }

    //显示添加新备忘的对话框
    private void showAddMenuDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.show();
        View view = View.inflate(this, R.layout.alert_add_menu, null);
        TextView tv_text = (TextView) view.findViewById(R.id.tv_text);//添加新备忘
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);//取消

        tv_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Tag==0){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("请选择笔记类型")
                            .setItems(new String[]{"学习笔记", "工作笔记","生活笔记"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        // 用户选择添加音频
                                        Tag=1;
                                        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                                        intent.putExtra("key", String.valueOf(Tag));
                                        Tag=0;
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                    } else if(which==1){
                                        // 用户选择录音
                                        Tag=2;
                                        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                                        intent.putExtra("key", String.valueOf(Tag));
                                        Tag=0;
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                    }else if(which==2) {
                                        Tag=3;
                                        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                                        intent.putExtra("key", String.valueOf(Tag));
                                        Tag=0;
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                    }
                                }
                            })
                            .show();
                }
                else {
                    Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                    intent.putExtra("key", Tag);
                    startActivity(intent);
                    alertDialog.dismiss();
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        Window window = alertDialog.getWindow();
        window.setContentView(view);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        //window.setWindowAnimations(R.style.DialogBottomStyle);//添加动画

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
    }

    // 后端返回的是相对 URL
    private void loadImageWithGlide(String relativeUrl, ImageView imageView) {
        // 构建完整的 URL
        String fullUrl = BASE_URL + relativeUrl;

        Glide.with(this)
                .load(fullUrl)
                .apply(new RequestOptions().transform(new CircleWithBorderTransformation(0xFFFFD700, 2f)))
                .into(imageView);
    }
}