package com.example.myapplication;

import android.app.Activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.bumptech.glide.Glide;
import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.NoteListAdapter;
import com.example.myapplication.DatabaseHelper;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;


public class AddNoteActivity extends BaseActivity {

    private static final int IMAGE_PICKER = 1001;
    //private static final int VIDEO_PICKER = 1002;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private ArrayList<Item> itemList;
    List<String> imageList = new ArrayList<>();
    List<String> mediaList = new ArrayList<>();
    private static final int REQUEST_PICK_AUDIO = 24;
    private static final int REQUEST_RECORD_AUDIO = 25;
    private ActivityResultLauncher<Intent> matisseLauncher;
    private List<Uri> selectedUris;
    private static final String TAG = "MyActivityTag";
    private EditText etTitle;
    private EditText etContent;//内容
    private TextView tvImage, tvCancel, tvSave,tvAudio;//图片,取消,保存
    private LinearLayout ivContent;
    private ScrollView scrollView;
    private VideoView vvContent;
    private Uri audioUrl;
    private ExoPlayer player;
    private String Tag;


    String soundpath="";

    //int resid=0;

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
        scrollView = findViewById(R.id.scroll);
        ivContent = findViewById(R.id.linearLayout);

        // 设置触摸监听器
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 获取点击位置
                    float x = event.getX();
                    float y = event.getY();

                    // 添加新的 EditText
                    if (!isViewAtPosition(x, y)) {
                        addEditText(x, y);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void initViews() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        //etContent = (EditText) findViewById(R.id.etContent);
        tvImage = (TextView) findViewById(R.id.tvImage);
        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvSave = (TextView) findViewById(R.id.tvSave);
        tvAudio=(TextView)findViewById(R.id.tvAudio);
        ivContent = (LinearLayout) findViewById(R.id.linearLayout);
        tvImage.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvAudio.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
            if(v.getId()==R.id.tvCancel) {//取消该备忘
                onBackPressed();
            }
            else if(v.getId()==R.id.tvImage){
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
            else if(v.getId()==R.id.tvSave) {
                String title = etTitle.getText().toString().trim();
                ViewGroup containerLayout = (ViewGroup) scrollView.getChildAt(0);

                traverseViews(containerLayout);
                String content = etContent.getText().toString().trim();
                if (title.length() <= 0 || itemList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                insertData(title);
                Toast.makeText(getApplicationContext(), "保存成功!", Toast.LENGTH_SHORT).show();
                finish();
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
    private void traverseViews(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int order=0;
            int j=0;
            int k=0;
            // 处理子组件
            if (child instanceof PlayerView) {
                itemList.add(new Item(order, "Media", mediaList.get(k)));
                k++;
                order++;
                // 处理 TextView
            } else if (child instanceof EditText) {
                EditText editText = (EditText) child;
                if(editText.getText().toString().trim()!=null){
                    itemList.add(new Item(order, "Text", editText.getText().toString().trim()));
                    order++;
                }
                // 处理 EditText
                System.out.println("EditText hint: " + editText.getHint());
            } else if (child instanceof ImageView) {
                itemList.add(new Item(order, "Image", imageList.get(j)));
                j++;
                order++;
                // 处理 Button
            } else if (child instanceof ViewGroup) {
                // 如果是 ViewGroup，递归遍历其子组件
                traverseViews((ViewGroup) child);
            }
        }
    }
    private boolean isViewAtPosition(float x, float y) {
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            View child = scrollView.getChildAt(i);
            Rect rect = new Rect();
            child.getHitRect(rect);
            if (rect.contains((int) x, (int) y)) {
                return true;
            }
        }
        return false;
    }
    private void addEditText(float x, float y) {
        // 创建新的 EditText
        EditText editText = new EditText(this);

        // 设置 EditText 的布局参数
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(layoutParams);

        // 设置一些属性（可选）

        // 将 EditText 添加到 LinearLayout
        ivContent.addView(editText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            selectedUris = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + getApplicationContext().getCacheDir().getAbsolutePath());
            // 使用返回的 Uri 处理图像
            for(int i=0;i<selectedUris.size();i++){
                ImageView imageView=new ImageView(this);;
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(selectedUris.get(i));
                imageList.add(selectedUris.get(i).toString());
                Log.i(TAG, "onCreate: " + selectedUris.get(i));
                ivContent.addView(imageView);
            }
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_PICK_AUDIO) {
            audioUrl = data.getData();
                // 处理选择的音频
            Log.i(TAG, "onCreate: " + audioUrl);
            playAudio(audioUrl);
        }
    }

    //插入数据
    private void insertData(String title) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE,title);
        cv.put(DatabaseHelper.TIME, Utils.getTimeStr());
        cv.put(DatabaseHelper.TAG,Tag);
        String all_photos;
        if (null != selectedUris && selectedUris.size() > 0) {
            //存储图片路径
            ContentValues values = new ContentValues();
            all_photos=selectedUris.get(0).toString();
            for(int i=1;i<selectedUris.size();i++){
                all_photos+=" ";
                all_photos+=selectedUris.get(i).toString();
            }
            cv.put(DatabaseHelper.COLUMN_IMAGE_URL, all_photos);
        }
        if (null != audioUrl) {
            //存储图片路径
            cv.put(DatabaseHelper.AUDIO_URL, audioUrl.toString());
        }
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

            // 设置要播放的媒体
            MediaItem mediaItem = MediaItem.fromUri(copy);
            player.setMediaItem(mediaItem);
            //mediaPlayer.prepareAsync();
            player.prepare();
            player.play();
            ivContent.addView(playerView);
        }
        catch(Exception e)
        {
            Toast.makeText(getBaseContext(),e.toString(),0).show();
        }
        saveFileToLocalInternalStorage(uri);
        mediaList.add(audioUrl.toString());
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
}