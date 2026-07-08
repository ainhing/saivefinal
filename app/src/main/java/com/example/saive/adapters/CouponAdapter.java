package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.Coupon;
import java.util.ArrayList;
import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private List<Coupon> coupons;
    private List<Coupon> couponsFull;
    private OnCouponClickListener listener;

    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon);
    }

    public CouponAdapter(List<Coupon> coupons, OnCouponClickListener listener) {
        this.coupons = coupons;
        this.couponsFull = new ArrayList<>(coupons);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);
        holder.tvDiscountValue.setText(coupon.getDiscount());
        holder.tvCouponTitle.setText(coupon.getTitle());
        holder.tvCouponDesc.setText(coupon.getDescription());
        holder.tvExpiry.setText(holder.itemView.getContext().getString(R.string.coupon_valid_until, coupon.getExpiryDate()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCouponClick(coupon);
            }
        });

        holder.btnCopy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCouponClick(coupon);
            }
        });
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        coupons.clear();
        if (text.isEmpty()) {
            coupons.addAll(couponsFull);
        } else {
            text = text.toLowerCase(java.util.Locale.getDefault());
            for (Coupon item : couponsFull) {
                if (item.getTitle().toLowerCase(java.util.Locale.getDefault()).contains(text) ||
                    item.getDescription().toLowerCase(java.util.Locale.getDefault()).contains(text) ||
                    item.getCode().toLowerCase(java.util.Locale.getDefault()).contains(text)) {
                    coupons.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return coupons.size();
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiscountValue, tvCouponTitle, tvCouponDesc, tvExpiry, btnCopy;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
            tvCouponTitle = itemView.findViewById(R.id.tvCouponTitle);
            tvCouponDesc = itemView.findViewById(R.id.tvCouponDesc);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnCopy = itemView.findViewById(R.id.btnApply);
        }
    }
}
