package com.example.saive.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import java.util.List;

public class SelectedPhotoAdapter extends RecyclerView.Adapter<SelectedPhotoAdapter.PhotoViewHolder> {

    private final List<Uri> photoUris;
    private final OnPhotoRemoveListener removeListener;

    public interface OnPhotoRemoveListener {
        void onRemove(int position);
    }

    public SelectedPhotoAdapter(List<Uri> photoUris, OnPhotoRemoveListener removeListener) {
        this.photoUris = photoUris;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri uri = photoUris.get(position);
        try {
            com.example.saive.utils.ImageUtils.setSafeImage(holder.ivPhoto, uri);
        } catch (Exception ignored) {
        }
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}