package com.example.myapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.List;

/**
 * 备忘详情页面
 */
public class NoteDetailActivity extends BaseActivity implements View.OnTouchListener {

    private static final int IMAGE_PICKER = 1001;
    private TextView tvTitle;
    private LinearLayout tvContent;//内容
    private static final String TAG = "MyActivityTag";
    private ArrayList<Item> itemList;
    private TextView tvEdite,tvDelete,tvChange, tvReturn;//取消,保存\
    List<String> imageList = new ArrayList<>();
    List<String> mediaList = new ArrayList<>();
    //private TextView tvPlay, tvPause;//播放,暂停
    private GridLayout ivContent;//图片内容
    // private VideoView vvContent;
    //private LinearLayout llVideoPlayer;//视频播放器布局
    private PlayerView playerView;
    private ExoPlayer player;
    private ScrollView scrollView;

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
        scrollView = findViewById(R.id.scroll);
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
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvContent = (LinearLayout) findViewById(R.id.linearLayout);
        tvEdite = (TextView) findViewById(R.id.tvEdite);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        tvReturn = (TextView) findViewById(R.id.tvReturn);

        tvEdite.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        tvReturn.setOnClickListener(this);
        //tvChange.setOnClickListener(this);
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

    private void setDataToView() {
        tvTitle.setText(note.getTitle());
        for (int i = 0; i < note.getContent().size(); i++) {
            Item item = note.getContent().get(i);
            if(item.getType()=="Text"){
                EditText editText = new EditText(this);

                // 设置 EditText 的布局参数
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                editText.setLayoutParams(layoutParams);
                editText.setText(item.getContent());

                // 设置一些属性（可选）

                // 将 EditText 添加到 LinearLayout
                tvContent.addView(editText);
            }
            // 处理value
            else if(item.getType()=="Image"){
                ImageView imageView=new ImageView(this);;
                imageView.setVisibility(View.VISIBLE);
                imageList.add(item.getContent());
                Uri uri = Uri.parse(item.getContent());
                imageView.setImageURI(uri);
                tvContent.addView(imageView);
            }
            else if(item.getType()=="Media"){
                player = new ExoPlayer.Builder(this).build();
                try {
                    playerView.setPlayer(player);

                    // 设置要播放的媒体
                    mediaList.add(item.getContent());
                    Uri uri = Uri.parse(item.getContent());
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
                                ViewGroup containerLayout = (ViewGroup) scrollView.getChildAt(0);

                                traverseViews(containerLayout);
                                //String content = tvContent.getText().toString().trim();
                                if (title.length() <= 0 || itemList.size() <= 0) {
                                    Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateDate(title, Utils.getTimeStr());
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
    private void updateDate(String title, String time) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE,title);
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
}
