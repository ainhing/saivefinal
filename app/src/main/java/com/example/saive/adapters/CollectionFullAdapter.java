package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.CollectionItem;
import com.example.saive.utils.ImageUtils;

import java.util.List;

public class CollectionFullAdapter extends RecyclerView.Adapter<CollectionFullAdapter.InfoViewHolder> {

    private final List<CollectionItem> items;
    private OnCollectionClickListener onCollectionClickListener;

    public interface OnCollectionClickListener {
        void onNextClick(CollectionItem item);
    }

    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.onCollectionClickListener = listener;
    }

    public CollectionFullAdapter(List<CollectionItem> collections) {
        this.items = collections;
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_info, parent, false);
        
        // Ensure full screen height for each page
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = parent.getContext().getResources().getDisplayMetrics().heightPixels;
        view.setLayoutParams(lp);
        
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        CollectionItem col = items.get(position);
        holder.tvTag.setText(col.getTag());
        holder.tvName.setText(col.getName());

        if (col.getImages() != null && !col.getImages().isEmpty()) {
            ImageUtils.setSafeImage(holder.ivBackground, col.getImages().get(0));
        }
        
        if (onCollectionClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                onCollectionClickListener.onNextClick(col);
            });
            holder.btnNext.setOnClickListener(v -> onCollectionClickListener.onNextClick(col));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvName;
        ImageView ivBackground;
        View btnNext;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvName = itemView.findViewById(R.id.tvName);
            ivBackground = itemView.findViewById(R.id.ivBackground);
            btnNext = itemView.findViewById(R.id.btnNext);
        }
    }
}