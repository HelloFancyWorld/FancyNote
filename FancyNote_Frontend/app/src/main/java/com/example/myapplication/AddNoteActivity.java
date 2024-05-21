package com.example.myapplication;

import android.app.Activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
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
    private static final int REQUEST_PICK_AUDIO = 24;
    private static final int REQUEST_RECORD_AUDIO = 25;
    private ActivityResultLauncher<Intent> matisseLauncher;
    private List<Uri> selectedUris;
    private static final String TAG = "MyActivityTag";
    private EditText etTitle;
    private EditText etContent;//内容
    private TextView tvImage, tvCancel, tvSave,tvAudio;//图片,取消,保存
    private GridLayout ivContent;
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
    }

    private void initViews() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        etContent = (EditText) findViewById(R.id.etContent);
        tvImage = (TextView) findViewById(R.id.tvImage);
        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvSave = (TextView) findViewById(R.id.tvSave);
        tvAudio=(TextView)findViewById(R.id.tvAudio);
        ivContent = (GridLayout) findViewById(R.id.ivContent);
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
                String content = etContent.getText().toString().trim();
                if (title.length() <= 0 || content.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                insertData(title,content);
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
    private void insertData(String title,String content) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE,title);
        cv.put(DatabaseHelper.CONTENT, content);
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
            PlayerView playerView = findViewById(R.id.player_view);
            playerView.setPlayer(player);

            // 设置要播放的媒体
            MediaItem mediaItem = MediaItem.fromUri(copy);
            player.setMediaItem(mediaItem);
            //mediaPlayer.prepareAsync();
            player.prepare();
            player.play();
        }
        catch(Exception e)
        {
            Toast.makeText(getBaseContext(),e.toString(),0).show();
        }
        saveFileToLocalInternalStorage(uri);
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