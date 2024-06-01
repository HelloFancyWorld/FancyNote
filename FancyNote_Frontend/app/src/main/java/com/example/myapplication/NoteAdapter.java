package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<NoteItem> noteItems;
    private final Context context;

    public NoteAdapter(Context context, ArrayList<NoteItem> noteItems) {
        this.context = context;
        this.noteItems = noteItems;
    }

    @Override
    public int getItemViewType(int position) {
        return noteItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NoteItem.TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false);
            return new TextViewHolder(view);
        } else if (viewType == NoteItem.TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
            return new AudioViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteItem noteItem = noteItems.get(position);
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).editText.setText(noteItem.getContent());
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).imageView.setImageURI(Uri.parse(noteItem.getImagePath()));
        } else if (holder instanceof AudioViewHolder) {
            ((AudioViewHolder) holder).videoView.setVideoURI(Uri.parse(noteItem.getAudioPath()));
            ((AudioViewHolder) holder).videoView.setMediaController(new MediaController(context));
            ((AudioViewHolder) holder).videoView.start();
        }
    }

    @Override
    public int getItemCount() {
        return noteItems.size();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        EditText editText;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.edit_text);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
        }
    }
}