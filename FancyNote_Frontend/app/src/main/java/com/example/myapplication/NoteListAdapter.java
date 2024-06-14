package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteListViewHolder> {

    private Activity activity;
    private List<Note> noteList;

    public NoteListAdapter(Activity activity, List<Note> noteList) {
        this.activity = activity;
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteListViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_note_list, null));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(NoteListViewHolder holder, int position) {
        final Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getAbstract());
        holder.tvTime.setText(note.getUpdated_at());
        // if (!TextUtils.isEmpty(note.getVideoPath())){
        // holder.ivPic.setImageBitmap(getVideoImage(note.getVideoPath(),100,100,
        // MediaStore.Images.Thumbnails.MICRO_KIND));
        // }

        // 点击Item的时候执行
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, NoteDetailActivity.class);
                intent.putExtra("note", note);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == noteList ? 0 : noteList.size();
    }

    class NoteListViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;

        public NoteListViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvContent = (TextView) itemView.findViewById(R.id.tvContent);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
        }
    }
}
