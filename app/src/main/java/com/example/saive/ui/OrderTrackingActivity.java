package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.saive.R;
import com.example.saive.base.BaseActivity;

import android.widget.TextView;
import java.util.List;
import com.example.saive.models.AdminOrder;
import com.example.saive.models.OrderItem;
import com.example.saive.utils.DataManager;

public class OrderTrackingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorHeaderBg));
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (isDarkMode) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        String idFromIntent = getIntent().getStringExtra("ORDER_ID");
        if (idFromIntent == null) {
            idFromIntent = getIntent().getStringExtra("orderId");
        }
        final String orderId = idFromIntent;
        if (orderId != null) {
            String userIdRaw = com.example.saive.utils.UserSession.getInstance().getUserId();
            if (userIdRaw == null) userIdRaw = "";
            final String userId = userIdRaw;
            AdminOrder order = DataManager.getInstance(this).getOrderById(orderId, userId);
            if (order != null) {
                TextView tvOrderId = findViewById(R.id.orderId);
                TextView tvPaymentMethod = findViewById(R.id.paymentMethod);
                TextView tvShippingAddress = findViewById(R.id.shippingAddress);
                LinearLayout itemsContainer = findViewById(R.id.itemsContainer);

                tvOrderId.setText(order.getOrderId());
                tvPaymentMethod.setText(order.getPaymentMethod());
                tvShippingAddress.setText(order.getShippingAddress());

                // Cập nhật ngày dự kiến (ví dụ: 3 ngày sau khi đặt)
                TextView tvExpectedDelivery = findViewById(R.id.expectedDelivery);
                if (tvExpectedDelivery != null) {
                    tvExpectedDelivery.setText(getString(R.string.format_expected_delivery, (order.getTimeAgo().equals("Just now") ? getString(R.string.status_in_3_days) : getString(R.string.status_in_transit))));
                }

                // Cập nhật thời gian trong timeline
                TextView tvTimePlaced = findViewById(R.id.textPlaced);
                if (tvTimePlaced != null && tvTimePlaced.getParent() instanceof LinearLayout) {
                    LinearLayout parent = (LinearLayout) tvTimePlaced.getParent();
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        if (child instanceof TextView && child != tvTimePlaced) {
                            ((TextView) child).setText(order.getTimeAgo().equals("Just now") ? getString(R.string.label_today) : order.getTimeAgo());
                        }
                    }
                }

                itemsContainer.removeAllViews();
                TextView tvSeeMore = findViewById(R.id.tvSeeMore);

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    List<OrderItem> items = order.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        OrderItem item = items.get(i);
                        View itemView = getLayoutInflater().inflate(R.layout.item_order_detail, itemsContainer, false);
                        
                        ImageView ivItem = itemView.findViewById(R.id.ivItemImage);
                        TextView tvName = itemView.findViewById(R.id.tvItemName);
                        TextView tvAttributes = itemView.findViewById(R.id.tvItemAttributes);
                        TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

                        tvName.setText(item.getName());
                        tvPrice.setText(item.getPrice());
                        String attributes;
                        if (item.getColor() != null && !item.getColor().isEmpty() && !item.getColor().equals("—") && !item.getColor().equals("Default")) {
                            attributes = getString(R.string.format_order_attributes_with_color, item.getSize(), item.getColor(), item.getQuantity());
                        } else {
                            attributes = getString(R.string.format_order_attributes, item.getSize(), item.getQuantity());
                        }
                        
                        tvAttributes.setText(attributes);
                        com.example.saive.utils.ImageUtils.setSafeImage(ivItem, item.getImageUrl(), item.getImageResId());

                        String status = order.getStatus() != null ? order.getStatus().toLowerCase(java.util.Locale.ROOT) : "";
                        if (status.equals("delivered") || status.equals("completed")) {
                            if (!order.isReviewed()) {
                                itemView.setOnClickListener(v -> {
                                    Intent intent = new Intent(OrderTrackingActivity.this, LeaveReviewActivity.class);
                                    intent.putExtra("productName", item.getName());
                                    intent.putExtra("productId", item.getProductId());
                                    intent.putExtra("orderPrice", item.getPrice());
                                    intent.putExtra("orderId", order.getOrderId());
                                    startActivity(intent);
                                });
                            }
                        }

                        if (i > 0) {
                            itemView.setVisibility(View.GONE);
                        }
                        itemsContainer.addView(itemView);
                    }

                    if (items.size() > 1) {
                        tvSeeMore.setVisibility(View.VISIBLE);
                        tvSeeMore.setText(getString(R.string.format_order_see_more, items.size() - 1));
                        tvSeeMore.setOnClickListener(v -> {
                            boolean isExpanded = itemsContainer.getChildAt(1).getVisibility() == View.VISIBLE;
                            if (isExpanded) {
                                // Thu gọn
                                for (int i = 1; i < itemsContainer.getChildCount(); i++) {
                                    itemsContainer.getChildAt(i).setVisibility(View.GONE);
                                }
                                tvSeeMore.setText(getString(R.string.format_order_see_more, items.size() - 1));
                            } else {
                                // Mở rộng
                                for (int i = 1; i < itemsContainer.getChildCount(); i++) {
                                    itemsContainer.getChildAt(i).setVisibility(View.VISIBLE);
                                }
                                tvSeeMore.setText(getString(R.string.btn_collapse));
                            }
                        });
                    } else {
                        tvSeeMore.setVisibility(View.GONE);
                    }
                } else {
                    tvSeeMore.setVisibility(View.GONE);
                    // Fallback for legacy orders without items list
                    View itemView = getLayoutInflater().inflate(R.layout.item_order_detail, itemsContainer, false);
                    ImageView ivItem = itemView.findViewById(R.id.ivItemImage);
                    TextView tvName = itemView.findViewById(R.id.tvItemName);
                    TextView tvAttributes = itemView.findViewById(R.id.tvItemAttributes);
                    TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

                    tvName.setText(order.getItemsSummary());
                    tvPrice.setText(order.getTotalAmount());
                    String attributes;
                    if (order.getColor() != null && !order.getColor().isEmpty()) {
                        attributes = getString(R.string.format_order_attributes_with_color, order.getSize(), order.getColor(), order.getQuantity());
                    } else {
                        attributes = getString(R.string.format_order_attributes, order.getSize(), order.getQuantity());
                    }
                    
                    tvAttributes.setText(attributes);
                    com.example.saive.utils.ImageUtils.setSafeImage(ivItem, order.getProductImageResId());
                    
                    itemsContainer.addView(itemView);
                }

                // Update timeline UI based on status
                updateTimeline(order.getStatus());
                
                // Real-time listener for status updates from Firebase
                String firebaseId = orderId.replace("#", "");
                com.google.firebase.database.FirebaseDatabase.getInstance("https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Orders")
                        .child(firebaseId)
                        .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                            @Override
                            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String status = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("Status"));
                                    if (!status.isEmpty()) {
                                        updateTimeline(status);
                                        // Update local cache so MyOrdersActivity is also updated
                                        com.example.saive.utils.DataManager.getInstance(OrderTrackingActivity.this)
                                                .updateOrderStatus(orderId, AdminOrder.normalizeStatus(status), userId);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                                // Ignore or log error
                            }
                        });
            }
        }
    }

    private void updateTimeline(String status) {
        if (status == null) return;
        String s = status.toLowerCase(java.util.Locale.ROOT);

        ImageView circlePlaced = findViewById(R.id.circlePlaced);
        View linePlaced = findViewById(R.id.linePlaced);
        TextView textPlaced = findViewById(R.id.textPlaced);

        ImageView circleProgress = findViewById(R.id.circleProgress);
        View lineProgress = findViewById(R.id.lineProgress);
        TextView textProgress = findViewById(R.id.textProgress);

        ImageView circleShipped = findViewById(R.id.circleShipped);
        View lineShipped = findViewById(R.id.lineShipped);
        TextView textShipped = findViewById(R.id.textShipped);

        ImageView circleDelivered = findViewById(R.id.circleDelivered);
        TextView textDelivered = findViewById(R.id.textDelivered);

        int activeColor = androidx.core.content.ContextCompat.getColor(this, R.color.colorAccentBrand);
        int inactiveColor = androidx.core.content.ContextCompat.getColor(this, R.color.colorLightGray);
        int activeTextColor = androidx.core.content.ContextCompat.getColor(this, R.color.colorNoirBlack);
        int inactiveTextColor = androidx.core.content.ContextCompat.getColor(this, R.color.colorGrayText);

        // Reset all to inactive
        circlePlaced.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        linePlaced.setBackgroundColor(inactiveColor);
        textPlaced.setTextColor(inactiveTextColor);

        circleProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        lineProgress.setBackgroundColor(inactiveColor);
        textProgress.setTextColor(inactiveTextColor);

        circleShipped.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        lineShipped.setBackgroundColor(inactiveColor);
        textShipped.setTextColor(inactiveTextColor);

        circleDelivered.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        textDelivered.setTextColor(inactiveTextColor);

        // Set active based on status keys matching Firebase
        // Keys: pending, confirmed, shipping, delivered, cancelled
        
        // Stage 1: Placed (Always active if order exists and not cancelled)
        if (!s.equals("cancelled") && !s.equals("canceled")) {
            circlePlaced.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            textPlaced.setTextColor(activeTextColor);
        }

        // Stage 2: Processing/Confirmed
        if (s.equals("confirmed") || s.equals("shipping") || s.equals("delivered") || s.equals("completed")) {
            linePlaced.setBackgroundColor(activeColor);
            circleProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            textProgress.setTextColor(activeTextColor);
        }

        // Stage 3: Shipped/Shipping
        if (s.equals("shipping") || s.equals("delivered") || s.equals("completed")) {
            lineProgress.setBackgroundColor(activeColor);
            circleShipped.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            textShipped.setTextColor(activeTextColor);
        }

        // Stage 4: Delivered/Completed
        if (s.equals("delivered") || s.equals("completed")) {
            lineShipped.setBackgroundColor(activeColor);
            circleDelivered.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            textDelivered.setTextColor(activeTextColor);
        }
        
        // Handle Cancelled case
        if (s.equals("cancelled") || s.equals("canceled")) {
            int cancelColor = androidx.core.content.ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error);
            circlePlaced.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cancelColor));
            textPlaced.setText(R.string.status_cancelled);
            textPlaced.setTextColor(cancelColor);
        }
    }
}
