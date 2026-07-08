package com.example.saive.adapters;

import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.saive.utils.FavoriteManager;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.Product;
import com.example.saive.ui.ProductDetailActivity;
import com.example.saive.utils.PriceFormatter;
import com.example.saive.utils.ImageUtils;
import com.example.saive.utils.ToastUtils;
import com.bumptech.glide.Glide;

import java.util.List;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ViewHolder> {

    private List<Product> products;
    private Integer textColor = null;

    public ProductGridAdapter(List<Product> products) {
        this.products = products;
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged();
    }

    public void updateData(List<Product> newProducts) {
        ProductDiffCallback diffCallback = new ProductDiffCallback(this.products, newProducts);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.products = newProducts;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        // Load ảnh: ưu tiên URL từ server, fallback về local drawable
        ImageUtils.setSafeImage(holder.ivProduct, product.getImageUrl(), product.getImageResId());

        holder.tvName.setText(product.getName().toUpperCase(java.util.Locale.getDefault()));

        // Hiển thị giá gốc và giá giảm
        if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
            holder.tvOriginalPrice.setPaintFlags(
                    holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

            // Căn lề cho giá hiện tại khi có giá gốc
            android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) holder.tvPrice
                    .getLayoutParams();
            params.setMarginStart((int) (6 * holder.itemView.getContext().getResources().getDisplayMetrics().density));
            holder.tvPrice.setLayoutParams(params);

            // Show discount badge
            if (holder.tvDiscountBadge != null) {
                holder.tvDiscountBadge.setVisibility(View.VISIBLE);
                try {
                    double original = PriceFormatter.parsePrice(product.getOriginalPrice());
                    double current = PriceFormatter.parsePrice(product.getPrice());
                    int percent = (int) (100 - (current * 100 / original));
                    if (percent > 0) {
                        holder.tvDiscountBadge
                                .setText(holder.itemView.getContext().getString(R.string.discount_format, percent));
                    } else {
                        holder.tvDiscountBadge.setText(holder.itemView.getContext().getString(R.string.label_sale));
                    }
                } catch (Exception e) {
                    holder.tvDiscountBadge.setText(holder.itemView.getContext().getString(R.string.label_sale));
                }
            }
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);

            // Reset margin khi không có giá gốc
            android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) holder.tvPrice
                    .getLayoutParams();
            params.setMarginStart(0);
            holder.tvPrice.setLayoutParams(params);

            if (holder.tvDiscountBadge != null) {
                holder.tvDiscountBadge.setVisibility(View.GONE);
            }
        }
        holder.tvPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

        if (textColor != null) {
            holder.tvName.setTextColor(textColor);
            holder.tvPrice.setTextColor(textColor);
            holder.tvOriginalPrice.setTextColor(textColor);
            holder.tvOriginalPrice.setAlpha(0.5f);
        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNoirBlack));
            holder.tvPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMaroon));
            holder.tvOriginalPrice
                    .setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorGrayText));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT", product);
            v.getContext().startActivity(intent);
        });

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
    }

    private void updateFavoriteIcon(ImageButton btn, boolean isFavorite) {
        if (isFavorite) {
            btn.setImageResource(R.drawable.ic_favorite);
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.colorMaroon));
        } else {
            btn.setImageResource(R.drawable.ic_favorite);
            btn.setColorFilter(ContextCompat.getColor(btn.getContext(), R.color.colorGrey));
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvOriginalPrice, tvDiscountBadge;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscountBadge = itemView.findViewById(R.id.tvDiscountBadge);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
