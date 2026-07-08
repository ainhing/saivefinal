package com.example.saive.adapters;

import android.annotation.SuppressLint;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.Product;
import com.example.saive.utils.ImageUtils;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<Product> cartItems;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onRemove(int position);
        void onQuantityChanged(Product product);
        void onVariantClick(int position, Product product);
    }

    public CartAdapter(List<Product> cartItems, OnCartChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateItems(List<Product> newItems) {
        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(new ProductDiffCallback(this.cartItems, newItems));
        this.cartItems = newItems;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Reset view states for recycling
        holder.itemView.setAlpha(1.0f);
        holder.itemView.setScaleX(1.0f);
        holder.itemView.setScaleY(1.0f);
        holder.swipeForeground.setTranslationX(0);
        holder.ivDeleteIcon.setAlpha(0f);
        holder.ivDeleteIcon.setScaleX(0.5f);
        holder.ivDeleteIcon.setScaleY(0.5f);

        Product product = cartItems.get(position);
        
        try {
            com.example.saive.utils.ImageUtils.setSafeImage(holder.ivProduct, product.getImageUrl(), product.getImageResId());
        } catch (Exception ignored) {
        }

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText(com.example.saive.utils.PriceFormatter.formatPrice(product.getPrice()));
        
        // Handle Flash Sale Price (Original vs Current)
        if (product.getOriginalPrice() != null) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(com.example.saive.utils.PriceFormatter.formatPrice(product.getOriginalPrice()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvPrice.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMaroon));
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvPrice.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNoirBlack));
        }

        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        // Display Variant (Size or Color)
        if (product.getCategory() != null && product.getCategory().toLowerCase(java.util.Locale.ROOT).contains("glasses")) {
            String color = product.getSelectedColor() != null ? product.getSelectedColor() : "Black";
            holder.tvVariantLabel.setText(holder.itemView.getContext().getString(R.string.label_color_format, color));
        } else {
            String size = product.getSelectedSize() != null ? product.getSelectedSize() : "M";
            holder.tvVariantLabel.setText(holder.itemView.getContext().getString(R.string.label_size_format, size));
        }

        holder.variantContainer.setOnClickListener(v -> {
            // Logic to change size/color will be implemented via BottomSheet in Activity
            if (listener != null) listener.onVariantClick(position, product);
        });

        // Initial state of minus button
        holder.btnMinus.setEnabled(product.getQuantity() > 1);
        holder.btnMinus.setAlpha(product.getQuantity() > 1 ? 1.0f : 0.3f);

        holder.btnPlus.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            int newQty = product.getQuantity() + 1;
            product.setQuantity(newQty);
            animateQuantityChange(holder.tvQuantity, newQty);
            holder.btnMinus.setEnabled(true);
            holder.btnMinus.setAlpha(1.0f);
            if (listener != null) listener.onQuantityChanged(product);
        });

        holder.btnMinus.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (product.getQuantity() > 1) {
                int newQty = product.getQuantity() - 1;
                product.setQuantity(newQty);
                animateQuantityChange(holder.tvQuantity, newQty);
                if (newQty == 1) {
                    holder.btnMinus.setEnabled(false);
                    holder.btnMinus.setAlpha(0.3f);
                }
                if (listener != null) listener.onQuantityChanged(product);
            }
        });

        // Swipe to Delete Logic (Cinematic)
        final float maxSwipe = -280f; 
        final PathInterpolator premiumInterpolator = new PathInterpolator(0.22f, 1f, 0.36f, 1f);
        final int touchSlop = ViewConfiguration.get(holder.itemView.getContext()).getScaledTouchSlop();

        holder.swipeForeground.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private float initialTranslationX;
            private boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        initialTranslationX = v.getTranslationX();
                        isSwiping = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float diffX = event.getRawX() - startX;
                        float diffY = event.getRawY() - startY;

                        if (!isSwiping && Math.abs(diffX) > touchSlop && Math.abs(diffX) > Math.abs(diffY)) {
                            isSwiping = true;
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        }

                        if (isSwiping) {
                            float translation = initialTranslationX + diffX;
                            if (translation > 0) translation = translation * 0.2f; // Rubber band effect
                            v.setTranslationX(translation);

                            // Progress for icon animation
                            float progress = Math.abs(translation) / Math.abs(maxSwipe);
                            holder.ivDeleteIcon.setAlpha(Math.min(1.0f, progress));
                            float scale = 0.5f + (Math.min(1.0f, progress) * 0.5f);
                            holder.ivDeleteIcon.setScaleX(scale);
                            holder.ivDeleteIcon.setScaleY(scale);
                        }
                        return isSwiping;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isSwiping) {
                            float currentX = v.getTranslationX();
                            if (currentX < maxSwipe * 0.5f) {
                                // Settle to revealed
                                v.animate()
                                        .translationX(maxSwipe)
                                        .setDuration(500)
                                        .setInterpolator(premiumInterpolator)
                                        .start();
                                holder.ivDeleteIcon.animate()
                                        .alpha(1f)
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(500)
                                        .setInterpolator(premiumInterpolator)
                                        .start();
                            } else {
                                // Reset
                                v.animate()
                                        .translationX(0)
                                        .setDuration(500)
                                        .setInterpolator(premiumInterpolator)
                                        .start();
                                holder.ivDeleteIcon.animate()
                                        .alpha(0f)
                                        .scaleX(0.5f)
                                        .scaleY(0.5f)
                                        .setDuration(500)
                                        .setInterpolator(premiumInterpolator)
                                        .start();
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(event.getRawX() - startX) < touchSlop) {
                            v.performClick();
                        }
                        return isSwiping;
                }
                return false;
            }
        });

        holder.ivDeleteIcon.setOnClickListener(v -> triggerDelete(holder));
    }

    private void triggerDelete(ViewHolder holder) {
        holder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (listener != null) {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onRemove(pos);
            }
        }
    }

    public void animateRemoval(int position, Runnable endAction) {
        // Find the view holder for this position to run the animation
        // Note: This is a bit tricky with notifyItemRemoved, 
        // usually it's better to let RecyclerView handle the animation.
        // We'll just call the endAction which will trigger notifyItemRemoved.
        endAction.run();
    }

    private void animateQuantityChange(TextView tv, int newQty) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(150);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
        // No-op
    }
            @Override public void onAnimationRepeat(Animation animation) {
        // No-op
    }
            @Override public void onAnimationEnd(Animation animation) {
                tv.setText(String.valueOf(newQty));
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(150);
                tv.startAnimation(fadeIn);
            }
        });
        tv.startAnimation(fadeOut);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivDeleteIcon;
        TextView tvName, tvCategory, tvPrice, tvOriginalPrice, tvQuantity, tvVariantLabel;
        ImageButton btnMinus, btnPlus;
        View swipeForeground, swipeBackground, variantContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvVariantLabel = itemView.findViewById(R.id.tvVariantLabel);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            ivDeleteIcon = itemView.findViewById(R.id.ivDeleteIcon);
            swipeForeground = itemView.findViewById(R.id.swipeForeground);
            swipeBackground = itemView.findViewById(R.id.swipeBackground);
            variantContainer = itemView.findViewById(R.id.variantContainer);
        }
    }
}
