package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.NoteRequest;
import com.example.myapplication.network.NoteResponse;
import com.example.myapplication.network.UploadAvatarResponse;
import com.example.myapplication.network.UploadFileResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yalantis.ucrop.util.FileUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddNoteActivity extends BaseActivity {

    private static final int IMAGE_PICKER = 1001;
    //private static final int VIDEO_PICKER = 1002;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private ArrayList<NoteItem> noteItemList = new ArrayList<>();
    List<String> imageList = new ArrayList<>();
    List<String> audioList = new ArrayList<>();
    private static final int REQUEST_PICK_AUDIO = 24;
    private static final int REQUEST_RECORD_AUDIO = 25;
    private ActivityResultLauncher<Intent> matisseLauncher;
    private List<Uri> selectedUris;
    private static final String TAG = "MyActivityTag";
    private EditText etTitle;
    private EditText etContent;//内容
    private TextView tvImage, tvCancel, tvSave,tvAudio, tvComplete;//图片,取消,保存,完成
    private LinearLayout ivContent;
    private ScrollView scrollView;
    private VideoView vvContent;
    private Uri audioUrl;
    private ExoPlayer player;
    private String Tag;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    // 选择的视图，用于删除时判断
    private View selectedView = null;
    private Drawable originalBackground;

    String soundpath="";

    //int resid=0;
    // 将 dp 值转换为 px
    int marginInDp = 16;
    float scale;
    int marginInPx;

    //录音
    private MediaRecorder mr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Tag = intent.getStringExtra("key");
        SharedPreferences pref = getSharedPreferences("author",MODE_PRIVATE);
        setContentView(R.layout.activity_add_note);
        initViews();
        addEditText();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setTitle("");

        etTitle = (EditText) findViewById(R.id.etTitle);
        //etContent = (EditText) findViewById(R.id.etContent);
        tvImage = (TextView) findViewById(R.id.tvImage);
        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvSave = (TextView) findViewById(R.id.tvSave);
        tvAudio=(TextView)findViewById(R.id.tvAudio);
        ivContent = (LinearLayout) findViewById(R.id.linearLayout);
        scrollView = findViewById(R.id.scroll);
        ivContent = findViewById(R.id.linearLayout);
        tvComplete = (TextView)findViewById(R.id.tvComplete);
        tvImage.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvAudio.setOnClickListener(this);
        tvComplete.setOnClickListener(this);

        scale = getResources().getDisplayMetrics().density;
        marginInPx = (int) (marginInDp * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
            if (v.getId() == R.id.tvCancel) { // 取消该备忘
                getOnBackPressedDispatcher().onBackPressed();
            }
            else if(v.getId()==R.id.tvImage){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 200);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                }
                Matisse.from(this)
                        .choose(MimeType.ofAll())
                        .countable(true)
                        .capture(true)
                        .captureStrategy(
                                new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                        .maxSelectable(9)
                        .thumbnailScale(0.85f)
                        .imageEngine(new GlideEngine())
                        .showPreview(false) // Default is `true`
                        .forResult(REQUEST_CODE_CHOOSE);
            }
            else if(v.getId()==R.id.tvComplete) {
                String title = etTitle.getText().toString().trim();
                ViewGroup containerLayout = (ViewGroup) scrollView.getChildAt(0);

                traverseViews(containerLayout);
                if (title.length() <= 0 || noteItemList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                insertData(title);

                // 打印noteItemList
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                String json = gson.toJson(noteItemList);
//                Log.d("noteitemlist", json);

                editNoteRequest();

                Toast.makeText(getApplicationContext(), "保存成功!", Toast.LENGTH_SHORT).show();
            }
            else if(v.getId()==R.id.tvAudio){
                checkPermission();
                new AlertDialog.Builder(this)
                        .setTitle("Select Action")
                        .setItems(new String[]{"Add Audio", "Record Audio"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    // 用户选择添加音频
                                    selectAudioFromDevice();
                                } else {
                                    // 用户选择录音
                                    startAudioRecording();
                                }
                            }
                        })
                        .show();
            }
    }

    public void editNoteRequest() {
        // Show a loading dialog or some UI indication


        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        // 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        // Create EditNoteRequest object
        String title = etTitle.getText().toString().trim();
        NoteRequest noteRequest = new NoteRequest(title, currentTime, currentTime, noteItemList);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(noteRequest);
        Log.d("noteitemlist", json);
        // Send the request
        apiService.createNote(noteRequest).enqueue(new Callback<NoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<NoteResponse> call, @NonNull Response<NoteResponse> response) {
                // Hide loading dialog or UI indication
                if (response.isSuccessful()) {
                    NoteResponse noteResponse = response.body();
                    handleNoteContents(noteResponse.getContents());
                    Toast.makeText(AddNoteActivity.this, "Note created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Failed to edit note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NoteResponse> call, Throwable t) {
                // Hide loading dialog or UI indication
                Toast.makeText(AddNoteActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNoteContents(List<NoteContent> contents) {
        for (NoteContent content : contents) {
            int contentId = content.getId();
            int type = content.getType();

            switch (type) {
                case NoteItem.TYPE_IMAGE:
                    if (content.getImageContent() != null) {
                        String imagePath = content.getImageContent().getLocal_path();
                        Uri imageUri = Uri.parse(imagePath);
                        uploadFile(imageUri, contentId, NoteItem.TYPE_IMAGE);
                    }
                    break;

                case NoteItem.TYPE_AUDIO:
                    if (content.getAudioContent() != null) {
                        String audioPath = content.getAudioContent().getLocal_path();
                        Uri audioUri = Uri.parse(audioPath);
                        uploadFile(audioUri, contentId, NoteItem.TYPE_AUDIO);
                    }
                    break;

                default:
                    break;
            }
        }
    }


    private void uploadFile(Uri uri, int contentId, int type) {
        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        // Create a file from the Uri
        File file = new File(FileUtils.getPath(this, uri));
        MediaType mediaType = MediaType.parse(getContentResolver().getType(uri));
        RequestBody requestFile = RequestBody.create(file, mediaType);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        RequestBody content_id = RequestBody.create(String.valueOf(contentId), MediaType.parse("multipart/form-data"));
        RequestBody type0 = RequestBody.create(String.valueOf(type), MediaType.parse("multipart/form-data"));

        apiService.uploadFile(partFile, content_id, type0).enqueue(new Callback<UploadFileResponse>() {
            @Override
            public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
                if (response.isSuccessful()) {
                    UploadFileResponse uploadResponse = response.body();
                    if (uploadResponse.isSuccess()) {
                        Toast.makeText(AddNoteActivity.this, "文件上传成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddNoteActivity.this, "文件上传失败: " + uploadResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddNoteActivity.this, "文件上传失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                Toast.makeText(AddNoteActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void traverseViews(ViewGroup parent) {
        int audio_index = 0;
        int image_index = 0;
        for (int i = 1; i < parent.getChildCount(); i++) { //从1开始不计标题
            View child = parent.getChildAt(i);
            if (child instanceof PlayerView) {
                Object tag = child.getTag();
                if (tag instanceof Uri) {
                    noteItemList.add(new NoteItem(NoteItem.TYPE_AUDIO, ((Uri) tag).toString()));
                }
                audio_index++;
            } else if (child instanceof EditText) {
                EditText editText = (EditText) child;
                String text = editText.getText().toString().trim();
                NoteItem new_noteitem = new NoteItem(NoteItem.TYPE_TEXT, text);
                noteItemList.add(new_noteitem);
            } else if (child instanceof ImageView) {
                Object tag = child.getTag();
                if (tag instanceof Uri) {
                    noteItemList.add(new NoteItem(NoteItem.TYPE_AUDIO, ((Uri) tag).toString()));
                }
                image_index++;
            }
        }
    }

    private void addEditText() {
        // 创建新的 EditText
        EditText editText = new EditText(this);

        // 设置字体大小，以 sp 为单位
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setBackground(null);

        // 设置 EditText 的布局参数
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(marginInPx,0,marginInPx,0);
        editText.setLayoutParams(layoutParams);

        // 添加 TextWatcher 和 OnKeyListener
        editText.addTextChangedListener(new CustomTextWatcher());
        editText.setOnKeyListener(new CustomOnKeyListener(editText));

        // 将 EditText 添加到 LinearLayout
        ivContent.addView(editText);
        // 将光标聚焦到新创建的 EditText
        editText.requestFocus();
    }

    private void addEditTextWithText(String text) {
        EditText editText = new EditText(this);
        editText.setText(text);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setBackground(null);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
        editText.setLayoutParams(layoutParams);

        editText.addTextChangedListener(new CustomTextWatcher());
        editText.setOnKeyListener(new CustomOnKeyListener(editText));

        ivContent.addView(editText);
        editText.requestFocus();
        editText.setSelection(0); // 设置光标在EditText的最前面
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            selectedUris = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + getApplicationContext().getCacheDir().getAbsolutePath());
            // 使用返回的 Uri 处理图像
            for(int i=0;i<selectedUris.size();i++){
                View focusedView = getCurrentFocus();//当前edittext
                if (focusedView instanceof EditText) {
                    EditText focusedEditText = (EditText) focusedView;
                    int cursorPosition = focusedEditText.getSelectionStart();

                    if (focusedEditText.getText().toString().isEmpty()) { //当前edit为空，先删除
                        ivContent.removeView(focusedEditText);
                        ImageView imageView = new ImageView(this);
                        ;
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageURI(selectedUris.get(i));
                        imageView.setTag(selectedUris.get(i));
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(5, 5, 5, 5);

                        // 创建并设置布局参数
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
                        imageView.setLayoutParams(layoutParams);

                        imageList.add(selectedUris.get(i).toString());
                        Log.i(TAG, "onCreate: " + selectedUris.get(i));
                        ivContent.addView(imageView);
                        addEditText();
                    } else if (cursorPosition == 0) {
                        // 光标在开头,在当前
                        int position = ivContent.indexOfChild(focusedEditText);
                        ImageView imageView = new ImageView(this);
                        ;
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageURI(selectedUris.get(i));
                        imageView.setTag(selectedUris.get(i));
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(5, 5, 5, 5);

                        // 创建并设置布局参数
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
                        imageView.setLayoutParams(layoutParams);

                        imageList.add(selectedUris.get(i).toString());
                        Log.i(TAG, "onCreate: " + selectedUris.get(i));
                        ivContent.addView(imageView, position);
                    } else if (cursorPosition == focusedEditText.getText().length()) {
                        // 光标在结尾
                        ImageView imageView = new ImageView(this);
                        ;
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageURI(selectedUris.get(i));
                        imageView.setTag(selectedUris.get(i));
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(5, 5, 5, 5);

                        // 创建并设置布局参数
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
                        imageView.setLayoutParams(layoutParams);

                        imageList.add(selectedUris.get(i).toString());
                        Log.i(TAG, "onCreate: " + selectedUris.get(i));
                        ivContent.addView(imageView);
                        addEditText();
                    } else {
                        // 光标在中间，分裂edittext，图片加在中间
                        String textBeforeCursor = focusedEditText.getText().toString().substring(0, cursorPosition);
                        String textAfterCursor = focusedEditText.getText().toString().substring(cursorPosition);
                        focusedEditText.setText(textBeforeCursor);
                        ImageView imageView = new ImageView(this);
                        ;
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageURI(selectedUris.get(i));
                        imageView.setTag(selectedUris.get(i));
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(5, 5, 5, 5);

                        // 创建并设置布局参数
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
                        imageView.setLayoutParams(layoutParams);

                        imageList.add(selectedUris.get(i).toString());
                        Log.i(TAG, "onCreate: " + selectedUris.get(i));
                        ivContent.addView(imageView);
                        addEditTextWithText(textAfterCursor);
                    }
                }
                else {
                    ImageView imageView=new ImageView(this);;
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(selectedUris.get(i));
                    imageView.setTag(selectedUris.get(i));
                    imageView.setAdjustViewBounds(true);
                    imageView.setPadding(5,5,5,5);

                    // 创建并设置布局参数
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

                    layoutParams.setMargins(marginInPx,0,marginInPx,0);
                    imageView.setLayoutParams(layoutParams);

                    imageList.add(selectedUris.get(i).toString());
                    Log.i(TAG, "onCreate: " + selectedUris.get(i));
                    ivContent.addView(imageView);
                    addEditText();
                }
            }
        }

        else if (resultCode == RESULT_OK && requestCode == REQUEST_PICK_AUDIO) {
            audioUrl = data.getData();
                // 处理选择的音频
            Log.i(TAG, "onCreate: " + audioUrl);
            playAudio(audioUrl);
            addEditText();
        }
    }

    //插入数据
    private void insertData(String title) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE,title);
        cv.put(DatabaseHelper.TIME, Utils.getTimeStr());
        cv.put(DatabaseHelper.TAG,Tag);
        Gson gson = new Gson();
        String structArrayJson = gson.toJson(noteItemList);
        cv.put(DatabaseHelper.CONTENT,structArrayJson);
        writableDB.insert(DatabaseHelper.TABLE_NAME, null, cv);
    }

    private void selectAudioFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_PICK_AUDIO);
    }

    private void startAudioRecording() {
        // 确保包含录音和文件操作的逻辑
        // 示例使用系统录音机进行录音
        AlertDialog.Builder builder=new AlertDialog.Builder(AddNoteActivity.this);

        builder.setTitle("录音提示").setMessage("当前正在录音!").setPositiveButton("完成",new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface in,int it)
            {
                stopRecord();
                playAudio(audioUrl);
            }
        });

        builder.create().show();

        //开始录音
        startRecord();

        //设置可见
    }
    private void playAudio(Uri uri) {
        Uri copy=uri;
        player = new ExoPlayer.Builder(this).build();
        try {
            PlayerView playerView = new PlayerView(this);
            playerView.setPlayer(player);
            playerView.setTag(copy);

            // 设置要播放的媒体=====
            MediaItem mediaItem = MediaItem.fromUri(copy);
            player.setMediaItem(mediaItem);
            //mediaPlayer.prepareAsync();
            player.prepare();
            player.play();
            ivContent.addView(playerView);
        }
        catch(Exception e)
        {
            Toast.makeText(getBaseContext(),e.toString(), Toast.LENGTH_SHORT).show();
        }
        saveFileToLocalInternalStorage(uri);
        audioList.add(audioUrl.toString());
    }
    private void stopRecord(){
        if(mr != null){
            mr.stop();
            mr.release();
            mr = null;
        }
    }
    private void startRecord(){
        if(mr == null){
            checkPermission();
            File dir = new File(Environment.getExternalStorageDirectory(),"Music");
            if(!dir.exists()){
                dir.mkdirs();
            }
            String datetime=System.currentTimeMillis()+"";
            File soundFile = new File(dir,"abner"+datetime+".wav");//存储到SD卡当然也可上传到服务器
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            soundpath=soundFile.getAbsolutePath();
            Log.i(TAG, "onCreate: " + soundpath);
            MediaRecorder recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // 保存录音文件的位置
            recorder.setOutputFile(soundpath);
            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            audioUrl=Uri.fromFile(new File(soundpath));
        }
    }
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }
    private void saveFileToLocalInternalStorage(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = openFileOutput("filename", MODE_PRIVATE)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            audioUrl = Uri.fromFile(new File(getFilesDir(), "filename"));
            // 这里的internalUri是内部存储中文件的URI
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            EditText focusedEditText = getCurrentFocus() instanceof EditText ? (EditText) getCurrentFocus() : null;
            if (focusedEditText != null && focusedEditText.getText().toString().isEmpty() && selectedView != null) {
                removeSelection();
            }
        }
    }

    private class CustomOnKeyListener implements View.OnKeyListener {
        private EditText editText;

        CustomOnKeyListener(EditText editText) {
            this.editText = editText;
            this.editText.addTextChangedListener(new CustomTextWatcher());
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.getSelectionStart() == 0) {
                        int index = ivContent.indexOfChild(editText);
                        if (index > 1)  //顶部标题不删
                        {
                            View viewToBeSelected = ivContent.getChildAt(index - 1);
                            if (selectedView == viewToBeSelected) {
                                // 删除视图
                                ivContent.removeView(viewToBeSelected);
                                removeSelection();
                                // 合并相邻的 EditText
                                if (index - 1 > 1 && ivContent.getChildAt(index - 2) instanceof EditText) {
                                    EditText previousEditText = (EditText) ivContent.getChildAt(index - 2);
                                    previousEditText.append("\n" + editText.getText().toString());
                                    ivContent.removeView(editText);
                                    previousEditText.requestFocus();
                                    previousEditText.setSelection(previousEditText.getText().length());
                                }
                            } else {
                                // 选中视图
                                selectView(viewToBeSelected);
                            }
                        }
                        return true;
                    }
                } else {
                    removeSelection();
                }
            }
            return false;
        }

        private class CustomTextWatcher implements TextWatcher {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                removeSelection();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        }
    }

    private void selectView(View view) {
        if (selectedView != null) {
            removeSelection();
        }
        selectedView = view;
        originalBackground = selectedView.getBackground();

        // 设置背景为获取的Drawable对象
        selectedView.setBackgroundResource(R.drawable.image_border);
    }

    private void removeSelection() {
        if (selectedView != null) {
            selectedView.setBackground(originalBackground);
            selectedView = null;
        }
    }
}