package com.example.myapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.Note;

import java.io.File;
import java.util.ArrayList;

/**
 * 备忘详情页面
 */
public class NoteDetailActivity extends BaseActivity implements View.OnTouchListener {

    private static final int IMAGE_PICKER = 1001;
    private TextView tvTitle,tvContent;//内容
    private static final String TAG = "MyActivityTag";
    private TextView tvEdite,tvDelete,tvChange, tvReturn;//取消,保存
    //private TextView tvPlay, tvPause;//播放,暂停
    private GridLayout ivContent;//图片内容
    // private VideoView vvContent;
    //private LinearLayout llVideoPlayer;//视频播放器布局
    private PlayerView playerView;
    private ExoPlayer player;

    private Note note;//备忘对象
    //private String id;

    GestureDetector mGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        note = (Note) getIntent().getSerializableExtra("note");

        initViews();
        setDataToView();
    }

    private void initViews() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvEdite = (TextView) findViewById(R.id.tvEdite);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        tvReturn = (TextView) findViewById(R.id.tvReturn);
        ivContent = (GridLayout) findViewById(R.id.ivContent);
        playerView=(PlayerView)findViewById(R.id.player_view);

        tvEdite.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        tvReturn.setOnClickListener(this);
        //tvChange.setOnClickListener(this);
    }

    private void setDataToView() {
        tvTitle.setText(note.getTitle());
        tvContent.setText(note.getContent());
        if (!TextUtils.isEmpty(note.getImagePath())) {
            Log.i(TAG, "onCreate: " + note.getImagePath());
            ivContent.setVisibility(View.VISIBLE);
            String[] parts = note.getImagePath().split(" ");
            for(int i=0;i<parts.length;i++){
                ImageView imageView=new ImageView(this);;
                imageView.setVisibility(View.VISIBLE);
                Log.i(TAG, "onCreate: " + parts[i]);
                Uri uri = Uri.parse(parts[i]);
                imageView.setImageURI(uri);
                ivContent.addView(imageView);
            }
        }
        if (!TextUtils.isEmpty(note.getAudioPath())) {
            player = new ExoPlayer.Builder(this).build();
            try {
                playerView.setPlayer(player);

                // 设置要播放的媒体
                Uri uri = Uri.parse(note.getAudioPath());
                Log.i(TAG, "onCreate: " + note.getAudioPath());
                MediaItem mediaItem = MediaItem.fromUri(uri);
                player.setMediaItem(mediaItem);
                //mediaPlayer.prepareAsync();
                player.prepare();
                player.play();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), 0).show();
            }
        }
    }
    public void onClick(View v) {
            if(v.getId()==R.id.tvReturn) {
                onBackPressed();
            }
            else if(v.getId()==R.id.tvDelete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog alertDialog = builder.setTitle("提示").setMessage("是否删除该备忘?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNote();
                                Toast.makeText(getApplicationContext(), "删除成功!", Toast.LENGTH_SHORT).show();
                                finish();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", null).create();
                alertDialog.show();
            }
            else if(v.getId()==R.id.tvEdite) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                AlertDialog alertDialog1 = builder1.setTitle("提示").setMessage("是否保存修改该备忘?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String title = tvTitle.getText().toString().trim();
                                String content = tvContent.getText().toString().trim();
                                if (title.length() <= 0 || content.length() <= 0) {
                                    Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateDate(title, content, Utils.getTimeStr());
                                Toast.makeText(getApplicationContext(), "保存成功!", Toast.LENGTH_SHORT).show();
                                finish();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", null).create();
                alertDialog1.show();
            }
    }

    private void deleteNote() {
        writableDB.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.ID + "=" + note.getId(), null);
    }
    private void updateDate(String title, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE,title);
        cv.put(DatabaseHelper.CONTENT, content);
        cv.put(DatabaseHelper.TIME, Utils.getTimeStr());
        writableDB.update(DatabaseHelper.TABLE_NAME,cv,DatabaseHelper.ID + "=" + note.getId(),null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGesture == null) {
            mGesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }
            });
            mGesture.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }
            });
        }

        return mGesture.onTouchEvent(event);
    }
}
