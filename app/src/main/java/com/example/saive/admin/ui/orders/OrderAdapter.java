package com.example.saive.admin.ui.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.databinding.AdminItemOrderBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<AdminOrder> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(View view, AdminOrder order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<AdminOrder> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemOrderBinding binding = AdminItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemOrderBinding binding;

        public OrderViewHolder(AdminItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminOrder order) {
            binding.tvOrderId.setText("#" + order.getOrderId());
            binding.tvCustomerName.setText(order.getFullName());
            binding.tvOrderStatus.setText(OrderStatus.getUIString(order.getStatus()));
            binding.tvOrderTotal.setText(formatPrice(order.getTotalAmount()));

            StringBuilder itemsSummary = new StringBuilder();
            if (order.getItems() != null) {
                itemsSummary.append(order.getItems().size()).append(" Items: ");
                for (int i = 0; i < order.getItems().size(); i++) {
                    itemsSummary.append(order.getItems().get(i).getProductName());
                    if (i < order.getItems().size() - 1) itemsSummary.append(", ");
                }
            }
            binding.tvOrderItems.setText(itemsSummary.toString());

            updateStatusUI(order.getStatus());

            itemView.setOnClickListener(v -> listener.onOrderClick(v, order));
        }

        private void updateStatusUI(String status) {
            int colorRes = R.color.colorSand;
            if (OrderStatus.PENDING.equals(status)) colorRes = R.color.colorMaroon;
            else if (OrderStatus.CONFIRMED.equals(status)) colorRes = android.R.color.holo_blue_dark;
            else if (OrderStatus.SHIPPING.equals(status)) colorRes = android.R.color.holo_orange_dark;
            else if (OrderStatus.DELIVERED.equals(status)) colorRes = android.R.color.holo_green_dark;
            else if (OrderStatus.CANCELLED.equals(status)) colorRes = android.R.color.holo_red_dark;

            binding.tvOrderStatus.setTextColor(binding.getRoot().getContext().getResources().getColor(colorRes));
        }

        private String formatPrice(double price) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return nf.format(price);
        }
    }
}