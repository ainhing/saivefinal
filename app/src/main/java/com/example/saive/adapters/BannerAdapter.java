package com.example.saive.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.ui.CollectionsListActivity;
import com.example.saive.utils.ImageUtils;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Integer> bannerImages;

    public BannerAdapter(List<Integer> bannerImages) {
        this.bannerImages = bannerImages;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        ImageUtils.setSafeImage(holder.ivBanner, bannerImages.get(position));
        
        // Thêm tiêu đề nghệ thuật cho từng banner
        String[] titles = {"SAIVE", "NEW SEASON", "ARCHIVE '24", "ESSENTIALS", "LIMITED"};
        holder.tvBannerTitle.setText(titles[position % titles.length]);

        holder.btnViewCollection.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CollectionsListActivity.class);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bannerImages.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvBannerTitle;
        TextView btnViewCollection;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            tvBannerTitle = itemView.findViewById(R.id.tvBannerTitle);
            btnViewCollection = itemView.findViewById(R.id.btnViewCollection);
        }
    }
}
