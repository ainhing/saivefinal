package com.example.saive.adapters;

import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.Product;
import com.example.saive.ui.ProductDetailActivity;
import com.example.saive.utils.PriceFormatter;
import com.example.saive.utils.ImageUtils;
import com.example.saive.utils.FavoriteManager;
import com.example.saive.utils.ToastUtils;
import androidx.core.content.ContextCompat;

import java.util.List;

public class FlashProductAdapter extends RecyclerView.Adapter<FlashProductAdapter.ViewHolder> {

    private List<Product> productList;
    private int textColor = -1;

    public FlashProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flash_sale_product, parent, false);
        // Set width for horizontal scrolling
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = (int) (parent.getContext().getResources().getDisplayMetrics().widthPixels * 0.45);
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        
        ImageUtils.setSafeImage(holder.ivProduct, product.getImageUrl(), product.getImageResId());

        holder.tvName.setText(product.getName().toUpperCase(java.util.Locale.getDefault()));
        
        if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
            holder.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            
            holder.tvPrice.setText(PriceFormatter.formatPrice(product.getPrice()));
            holder.tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);

            // Hiển thị Badge giảm giá
            TextView badge = holder.itemView.findViewById(R.id.tvDiscountBadge);
            if (badge != null) {
                try {
                    double original = PriceFormatter.parsePrice(product.getOriginalPrice());
                    double current = PriceFormatter.parsePrice(product.getPrice());
                    if (original > current) {
                        int percent = (int) Math.round(100 - (current * 100 / original));
                        badge.setText(holder.itemView.getContext().getString(R.string.discount_format, percent));
                        badge.setVisibility(View.VISIBLE);
                    } else {
                        badge.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    badge.setVisibility(View.GONE);
                }
            }
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvPrice.setText(PriceFormatter.formatPrice(product.getPrice()));
            holder.tvPrice.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            View badge = holder.itemView.findViewById(R.id.tvDiscountBadge);
            if (badge != null) badge.setVisibility(View.GONE);
        }

        if (textColor != -1) {
            holder.tvName.setTextColor(textColor);
            holder.tvPrice.setTextColor(textColor);
            holder.tvOriginalPrice.setTextColor(textColor);
        }

        boolean isFavorite = FavoriteManager.getInstance(holder.itemView.getContext()).isFavorite(product);
        updateFavoriteIcon(holder.btnFavorite, isFavorite);

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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT", product);
            v.getContext().startActivity(intent);
        });
    }

    private void updateFavoriteIcon(ImageButton btn, boolean isFavorite) {
        if (isFavorite) {
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.colorMaroon));
        } else {
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.colorAlwaysWhite));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvOriginalPrice;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
