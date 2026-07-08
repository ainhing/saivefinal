package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.utils.ImageUtils;
import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    private List<String> imageUrls;
    private List<Integer> imageResIds;

    public ProductImageAdapter(List<String> imageUrls, List<Integer> imageResIds) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.imageResIds = imageResIds != null ? imageResIds : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!imageUrls.isEmpty() && position < imageUrls.size()) {
            ImageUtils.setSafeImage(holder.imageView, imageUrls.get(position), 0);
        } else if (!imageResIds.isEmpty()) {
            int resIdIndex = imageUrls.isEmpty() ? position : position - imageUrls.size();
            if (resIdIndex >= 0 && resIdIndex < imageResIds.size()) {
                ImageUtils.setSafeImage(holder.imageView, imageResIds.get(resIdIndex));
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = imageUrls.size() + imageResIds.size();
        return count > 0 ? count : 1; // Show at least one placeholder or default if empty
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
