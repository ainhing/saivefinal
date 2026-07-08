package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.AdminOrder;
import java.util.List;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder> {

    private List<AdminOrder> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(AdminOrder order);
        void onActionClick(AdminOrder order);
    }

    public UserOrderAdapter(List<AdminOrder> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public void updateData(List<AdminOrder> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AdminOrder order = orderList.get(position);
        holder.tvTotal.setText(com.example.saive.utils.PriceFormatter.formatPrice(order.getTotalAmount()));
        
        // Set action button text based on status
        String status = order.getStatus() != null ? order.getStatus().toLowerCase(java.util.Locale.ROOT) : "pending";
        holder.btnAction.setVisibility(View.VISIBLE);
        holder.btnAction.setEnabled(true);
        if (status.equals("completed") || status.equals("delivered")) {
            if (order.isReviewed()) {
                holder.btnAction.setVisibility(View.GONE);
            } else {
                holder.btnAction.setText(R.string.btn_leave_review);
            }
        } else if (status.equals("cancelled")) {
            holder.btnAction.setText(R.string.btn_reorder);
        } else {
            holder.btnAction.setText(R.string.btn_track_order);
        }

        // Populate items
        holder.itemsContainer.removeAllViews();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<com.example.saive.models.OrderItem> items = order.getItems();
            LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
            
            for (int i = 0; i < items.size(); i++) {
                com.example.saive.models.OrderItem item = items.get(i);
                View itemView = inflater.inflate(R.layout.item_order_detail, holder.itemsContainer, false);
                
                ImageView ivItem = itemView.findViewById(R.id.ivItemImage);
                TextView tvName = itemView.findViewById(R.id.tvItemName);
                TextView tvAttributes = itemView.findViewById(R.id.tvItemAttributes);
                TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

                tvName.setText(item.getName());
                tvPrice.setText(com.example.saive.utils.PriceFormatter.formatPrice(item.getPrice()));
                if (item.getColor() != null && !item.getColor().isEmpty() && !item.getColor().equals("—") && !item.getColor().equals("Default")) {
                    tvAttributes.setText(holder.itemView.getContext().getString(R.string.format_order_attributes_with_color, item.getSize(), item.getColor(), item.getQuantity()));
                } else {
                    tvAttributes.setText(holder.itemView.getContext().getString(R.string.format_order_attributes, item.getSize(), item.getQuantity()));
                }
                
                // Sử dụng setSafeImage hỗ trợ cả URL và Resource ID
                com.example.saive.utils.ImageUtils.setSafeImage(ivItem, item.getImageUrl(), item.getImageResId());

                if (i > 0) {
                    itemView.setVisibility(View.GONE);
                }
                holder.itemsContainer.addView(itemView);
            }

            if (items.size() > 1) {
                holder.tvSeeMore.setVisibility(View.VISIBLE);
                holder.tvSeeMore.setText(holder.itemView.getContext().getString(R.string.format_order_see_more, items.size() - 1));
                holder.tvSeeMore.setOnClickListener(v -> {
                    boolean isExpanded = holder.itemsContainer.getChildAt(1).getVisibility() == View.VISIBLE;
                    if (isExpanded) {
                        // Collapse
                        for (int i = 1; i < holder.itemsContainer.getChildCount(); i++) {
                            holder.itemsContainer.getChildAt(i).setVisibility(View.GONE);
                        }
                        holder.tvSeeMore.setText(holder.itemView.getContext().getString(R.string.format_order_see_more, items.size() - 1));
                    } else {
                        // Expand
                        for (int i = 1; i < holder.itemsContainer.getChildCount(); i++) {
                            holder.itemsContainer.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                        holder.tvSeeMore.setText(holder.itemView.getContext().getString(R.string.btn_collapse));
                    }
                });
            } else {
                holder.tvSeeMore.setVisibility(View.GONE);
            }
        } else {
            // Fallback for legacy orders
            holder.tvSeeMore.setVisibility(View.GONE);
            View itemView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_order_detail, holder.itemsContainer, false);
            ImageView ivItem = itemView.findViewById(R.id.ivItemImage);
            TextView tvName = itemView.findViewById(R.id.tvItemName);
            TextView tvAttributes = itemView.findViewById(R.id.tvItemAttributes);
            TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

            tvName.setText(order.getItemsSummary());
            tvPrice.setText(com.example.saive.utils.PriceFormatter.formatPrice(order.getTotalAmount()));
            tvAttributes.setText(holder.itemView.getContext().getString(R.string.format_order_attributes, order.getSize(), order.getQuantity()));
            
            // Fallback cho đơn hàng cũ
            com.example.saive.utils.ImageUtils.setSafeImage(ivItem, order.getImageUrl(), order.getProductImageResId());
            holder.itemsContainer.addView(itemView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotal, tvSeeMore;
        android.widget.LinearLayout itemsContainer;
        Button btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTotal = itemView.findViewById(R.id.tvUserOrderTotal);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            btnAction = itemView.findViewById(R.id.btnOrderAction);
        }
    }
}