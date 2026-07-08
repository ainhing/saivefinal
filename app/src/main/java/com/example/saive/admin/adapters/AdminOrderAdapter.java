package com.example.saive.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.model.AdminOrderItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private List<AdminOrder> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(AdminOrder order);
    }

    public AdminOrderAdapter(List<AdminOrder> orderList, OnOrderClickListener listener) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<AdminOrder> newList) {
        this.orderList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminOrder order = orderList.get(position);
        
        String id = order.getOrderId();
        if (id == null) id = "Unknown";
        String shortId = id.length() > 8 ? id.substring(id.length() - 8) : id;
        holder.tvOrderId.setText("#" + shortId.toUpperCase());
        holder.tvCustomerName.setText(order.getFullName());
        
        StringBuilder summary = new StringBuilder();
        List<AdminOrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                summary.append(items.get(i).getQuantity()).append("x ").append(items.get(i).getProductName());
                if (i < items.size() - 1) summary.append(", ");
            }
        } else {
            summary.append("No items");
        }
        holder.tvOrderItems.setText(summary.toString());
        holder.tvOrderTotal.setText(formatPrice(order.getTotalAmount()));
        holder.tvStatus.setText(order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING");
        
        updateStatusStyle(holder.tvStatus, order.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    private void updateStatusStyle(TextView tvStatus, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "pending":
                tvStatus.setTextColor(0xFFE65100); // Orange
                break;
            case "delivered":
            case "completed":
                tvStatus.setTextColor(0xFF2E7D32); // Green
                break;
            case "cancelled":
                tvStatus.setTextColor(0xFFC62828); // Red
                break;
            default:
                tvStatus.setTextColor(0xFF3E2723); 
                break;
        }
    }

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvOrderItems, tvOrderTotal, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}