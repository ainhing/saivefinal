package com.example.saive.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.saive.R;
import com.example.saive.models.Product;
import com.example.saive.ui.ProductDetailActivity;
import com.example.saive.utils.PriceFormatter;

import java.util.List;
import java.util.Locale;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final List<Product> recommendedProducts;
    private final List<String[]> reasonList; // each entry: [reasonTitle, reasonDetail]

    public RecommendationAdapter(List<Product> products, List<String[]> reasons) {
        this.recommendedProducts = products;
        this.reasonList = reasons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = recommendedProducts.get(position);
        String reasonTitle = (reasonList != null && position < reasonList.size()) ? reasonList.get(position)[0] : "";
        String reasonDetail = (reasonList != null && position < reasonList.size()) ? reasonList.get(position)[1] : "";
        holder.bind(product, reasonTitle, reasonDetail);
    }

    @Override
    public int getItemCount() {
        return recommendedProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProduct;
        private final TextView tvCategoryBadge;
        private final TextView tvProductName;
        private final TextView tvProductPrice;
        private final TextView tvOriginalPrice;
        private final TextView tvReasonTitle;
        private final TextView tvReasonDetail;
        private final TextView tvViewDetail;
        private final View cardRecommend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvReasonTitle = itemView.findViewById(R.id.tvReasonTitle);
            tvReasonDetail = itemView.findViewById(R.id.tvReasonDetail);
            tvViewDetail = itemView.findViewById(R.id.tvViewDetail);
            cardRecommend = itemView.findViewById(R.id.cardRecommend);
        }

        public void bind(Product product, String reasonTitle, String reasonDetail) {
            Context context = itemView.getContext();

            // Name & Price
            tvProductName.setText(product.getName().toUpperCase(Locale.ROOT));
            tvProductPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

            // Discount price
            if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
                tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
            }

            // Category badge
            String group = product.getTagTypeGroup() != null ? product.getTagTypeGroup().toLowerCase(Locale.ROOT) : "";
            String badgeText;
            if (group.contains("bottom") || group.contains("pant") || group.contains("skirt") || group.contains("jean")) {
                badgeText = "BOTTOM";
            } else if (group.contains("shoe") || group.contains("boot") || group.contains("sneaker")) {
                badgeText = "SHOES";
            } else if (group.contains("bag") || group.contains("purse")) {
                badgeText = "BAG";
            } else if (group.contains("glasses") || group.contains("eyewear")) {
                badgeText = "EYEWEAR";
            } else if (group.contains("belt")) {
                badgeText = "BELT";
            } else {
                badgeText = "ACCESSORY";
            }
            tvCategoryBadge.setText(badgeText);

            // Reason block
            if (tvReasonTitle != null) {
                if (reasonTitle != null && !reasonTitle.isEmpty()) {
                    tvReasonTitle.setText(reasonTitle.toUpperCase(Locale.ROOT));
                    tvReasonTitle.setVisibility(View.VISIBLE);
                } else {
                    tvReasonTitle.setVisibility(View.GONE);
                }
            }
            if (tvReasonDetail != null) {
                if (reasonDetail != null && !reasonDetail.isEmpty()) {
                    tvReasonDetail.setText(reasonDetail);
                    tvReasonDetail.setVisibility(View.VISIBLE);
                } else {
                    tvReasonDetail.setVisibility(View.GONE);
                }
            }

            // Image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.color.colorCotton)
                        .error(R.color.colorCotton)
                        .into(ivProduct);
            } else if (product.getImageResId() != 0) {
                ivProduct.setImageResource(product.getImageResId());
            } else {
                ivProduct.setImageResource(R.drawable.model1);
            }

            // Click → ProductDetailActivity
            View.OnClickListener clickListener = v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("PRODUCT", product);
                context.startActivity(intent);
            };
            cardRecommend.setOnClickListener(clickListener);
            if (tvViewDetail != null) tvViewDetail.setOnClickListener(clickListener);
        }
    }
}
