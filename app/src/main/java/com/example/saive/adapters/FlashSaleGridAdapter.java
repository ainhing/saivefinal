package com.example.saive.adapters;

import android.graphics.Paint;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.Product;
import com.example.saive.ui.ProductDetailActivity;
import com.example.saive.utils.FavoriteManager;
import com.example.saive.utils.PriceFormatter;
import com.example.saive.utils.ImageUtils;
import com.example.saive.utils.ToastUtils;
import java.util.List;

public class FlashSaleGridAdapter extends RecyclerView.Adapter<FlashSaleGridAdapter.ViewHolder> {

    private List<Product> productList;
    private int textColor = -1;

    public FlashSaleGridAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged();
    }

    public void updateList(List<Product> newList) {
        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = 
            androidx.recyclerview.widget.DiffUtil.calculateDiff(new ProductDiffCallback(this.productList, newList));
        this.productList = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flash_sale_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName().toUpperCase(java.util.Locale.getDefault()));
        holder.tvPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

        if (product.getOriginalPrice() != null) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }

        if (textColor != -1) {
            holder.tvName.setTextColor(textColor);
            holder.tvPrice.setTextColor(textColor);
            holder.tvOriginalPrice.setTextColor(textColor);
            holder.tvOriginalPrice.setAlpha(0.6f);
        }
        
        // Discount badge logic
        if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
            try {
                double original = PriceFormatter.parsePrice(product.getOriginalPrice());
                double current = PriceFormatter.parsePrice(product.getPrice());
                if (original > current) {
                    int percent = (int) Math.round(100 - (current * 100 / original));
                    holder.tvDiscount.setText(holder.itemView.getContext().getString(R.string.discount_format, percent));
                    holder.tvDiscount.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDiscount.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                holder.tvDiscount.setVisibility(View.GONE);
            }
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Cập nhật logic hiển thị ảnh để hỗ trợ cả URL từ Firebase
        ImageUtils.setSafeImage(holder.ivProduct, product.getImageUrl(), product.getImageResId());

        boolean isFavorite = FavoriteManager.getInstance(holder.itemView.getContext()).isFavorite(product);
        updateFavoriteIcon(holder.btnFavorite, isFavorite);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT", product);
            v.getContext().startActivity(intent);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            FavoriteManager favoriteManager = FavoriteManager.getInstance(v.getContext());
            boolean newState = !favoriteManager.isFavorite(product);
            if (newState) {
                favoriteManager.addFavorite(product);
                ToastUtils.showCustomToast(v.getContext(), v.getContext().getString(R.string.toast_added_favorites));
            } else {
                favoriteManager.removeFavorite(product);
                ToastUtils.showCustomToast(v.getContext(), v.getContext().getString(R.string.toast_removed_favorites));
            }
            updateFavoriteIcon(holder.btnFavorite, newState);
        });
    }

    private void updateFavoriteIcon(ImageButton btn, boolean isFavorite) {
        if (isFavorite) {
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.colorMaroon));
        } else {
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvOriginalPrice, tvDiscount;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscountBadge);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
