package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.adapters.UserOrderAdapter;
import com.example.saive.models.AdminOrder;
import com.example.saive.models.OrderItem;
import com.example.saive.utils.DataManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyOrdersActivity extends BaseActivity {

    private RecyclerView rvOrders;
    private View emptyState;
    private View progressBar;
    private TabLayout tabLayout;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private List<AdminOrder> allOrders = new ArrayList<>();
    private UserOrderAdapter adapter;
    private final android.os.Handler tabHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable filterRunnable;
    private com.google.firebase.database.ValueEventListener ordersListener;
    private com.google.firebase.database.Query currentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMaroon));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvOrders = findViewById(R.id.rvMyOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        
        // Khởi tạo adapter một lần duy nhất
        adapter = new UserOrderAdapter(new ArrayList<>(), new UserOrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(AdminOrder order) {
                Intent intent = new Intent(MyOrdersActivity.this, OrderTrackingActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                startActivity(intent);
            }

            @Override
            public void onActionClick(AdminOrder order) {
                String s = order.getStatus() != null ? order.getStatus().toUpperCase(java.util.Locale.ROOT) : "PENDING";
                if (s.equals("COMPLETED") || s.equals("DELIVERED")) {
                    Intent intent = new Intent(MyOrdersActivity.this, LeaveReviewActivity.class);
                    if (order.getItems() != null && !order.getItems().isEmpty()) {
                        OrderItem firstItem = order.getItems().get(0);
                        intent.putExtra("productName", firstItem.getName());
                        intent.putExtra("productId", firstItem.getProductId());
                    } else {
                        // Fallback cho đơn hàng cũ không có danh sách item chi tiết
                        intent.putExtra("productName", order.getItemsSummary());
                    }
                    intent.putExtra("orderPrice", order.getTotalAmount());
                    intent.putExtra("orderId", order.getOrderId());
                    startActivity(intent);
                }
            }
        });
        rvOrders.setAdapter(adapter);

        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.colorMaroon);
            swipeRefresh.setOnRefreshListener(this::loadOrders);
        }

        setupTabs();
        // loadOrders() will be called in onResume() for initial and fresh data
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_active));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_completed));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_cancelled));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Debounce tab selection to optimize performance and UI transitions
                if (filterRunnable != null) tabHandler.removeCallbacks(filterRunnable);
                filterRunnable = () -> filterOrders(tab.getPosition());
                tabHandler.postDelayed(filterRunnable, 100);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startListeningOrders();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeningOrders();
    }

    private void startListeningOrders() {
        if (ordersListener != null && currentQuery != null) {
            currentQuery.removeEventListener(ordersListener);
        }

        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String userIdRaw = com.example.saive.utils.UserSession.getInstance().getUserId();
        if (userIdRaw == null) userIdRaw = "";
        final String userId = userIdRaw;

        com.google.firebase.database.DatabaseReference ordersRef = 
            com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Orders");
        
        if (!userId.isEmpty()) {
            currentQuery = ordersRef.orderByChild("UserId").equalTo(userId);
        } else {
            // Thử tìm theo email
            String userEmail = com.example.saive.utils.UserSession.getInstance().getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                currentQuery = ordersRef.orderByChild("Email").equalTo(userEmail);
            } else {
                currentQuery = ordersRef;
            }
        }

        ordersListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                
                List<AdminOrder> remoteOrders = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    AdminOrder order = parseOrder(child);
                    if (order != null) {
                        remoteOrders.add(order);
                    }
                }

                allOrders.clear();
                allOrders.addAll(remoteOrders);
                
                allOrders.sort((o1, o2) -> {
                    Object t1 = o1.getTimeAgo(); // Trong AdminOrder (customer model), timeAgo lưu giá trị thô
                    Object t2 = o2.getTimeAgo();
                    
                    if (t1 instanceof Long && t2 instanceof Long) {
                        return ((Long) t2).compareTo((Long) t1);
                    }
                    String s1 = t1 != null ? t1.toString() : "";
                    String s2 = t2 != null ? t2.toString() : "";
                    return s2.compareTo(s1);
                });

                DataManager.getInstance(MyOrdersActivity.this).saveOrders(allOrders, userId);
                filterOrders(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                loadLocalOrdersFallback(userId);
            }
        };

        currentQuery.addValueEventListener(ordersListener);
    }

    private void stopListeningOrders() {
        if (ordersListener != null && currentQuery != null) {
            currentQuery.removeEventListener(ordersListener);
            ordersListener = null;
        }
    }

    private AdminOrder parseOrder(com.google.firebase.database.DataSnapshot child) {
        try {
            String orderId = getStringSafe(child.child("OrderId"));
            if (orderId == null || orderId.isEmpty()) orderId = child.getKey();
            
            String fullName = getStringSafe(child.child("FullName"));
            Object totalAmountObj = child.child("TotalAmount").getValue();
            String status = getStringSafe(child.child("Status"));
            Object createdAtObj = child.child("CreatedAt").getValue();
            String createdAt = "Just now";
            if (createdAtObj instanceof Long) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                createdAt = sdf.format(new java.util.Date((Long) createdAtObj));
            } else if (createdAtObj instanceof String) {
                createdAt = (String) createdAtObj;
            }
            String paymentMethod = getStringSafe(child.child("PaymentMethod"));
            String shippingAddress = getStringSafe(child.child("ShippingAddress"));

            String totalAmount = "0";
            if (totalAmountObj instanceof Number) {
                totalAmount = String.valueOf(((Number) totalAmountObj).doubleValue());
            } else if (totalAmountObj instanceof String) {
                totalAmount = (String) totalAmountObj;
            }

            AdminOrder order = new AdminOrder(
                    orderId, fullName, "",
                    totalAmount,
                    AdminOrder.normalizeStatus(status),
                    createdAt
            );
            
            try {
                Object isRev = child.child("IsReviewed").getValue();
                if (isRev instanceof Boolean) {
                    order.setReviewed((Boolean) isRev);
                } else if (isRev instanceof String) {
                    order.setReviewed(Boolean.parseBoolean((String) isRev));
                }
            } catch (Exception e) {
                // Ignore isReviewed parsing errors
            }

            // Lưu lại giá trị gốc của CreatedAt vào timeAgo để sorting chính xác hơn nếu cần
            if (createdAtObj != null) {
                order.setTimeAgo(createdAtObj.toString());
            }
            order.setPaymentMethod(paymentMethod);
            order.setShippingAddress(shippingAddress);

            List<OrderItem> items = new ArrayList<>();
            StringBuilder summary = new StringBuilder();
            com.google.firebase.database.DataSnapshot itemsSnapshot = child.child("Items");
            if (itemsSnapshot.exists()) {
                int i = 0;
                long totalItemsCount = itemsSnapshot.getChildrenCount();
                for (com.google.firebase.database.DataSnapshot itemChild : itemsSnapshot.getChildren()) {
                    String prodId = getStringSafe(itemChild.child("ProductId"));
                    String prodName = getStringSafe(itemChild.child("ProductName"));
                    Integer qty = itemChild.child("Quantity").getValue(Integer.class);
                    if (qty == null) qty = 1;
                    String size = getStringSafe(itemChild.child("SelectedSize"));
                    if (size == null || size.isEmpty()) size = getStringSafe(itemChild.child("size"));
                    String color = getStringSafe(itemChild.child("SelectedColor"));
                    if (color == null || color.isEmpty()) color = getStringSafe(itemChild.child("color"));
                    
                    Object priceObj = itemChild.child("Price").getValue();
                    String img = getStringSafe(itemChild.child("Image"));

                    summary.append(qty).append("x ").append(prodName);
                    if (i < totalItemsCount - 1) summary.append(", ");

                    OrderItem orderItem = new OrderItem(prodName, size != null && !size.isEmpty() ? size : "M", color != null && !color.isEmpty() ? color : "—", qty, String.valueOf(priceObj), R.drawable.tshirt1, img);
                    orderItem.setProductId(prodId != null && !prodId.isEmpty() ? prodId : null);
                    items.add(orderItem);
                    i++;
                }
            }
            order.setItems(items);
            order.setItemsSummary(summary.toString());
            if (!items.isEmpty()) {
                order.setImageUrl(items.get(0).getImageUrl());
            }
            return order;
        } catch (Exception e) {
            android.util.Log.e("MyOrdersActivity", "Error parsing order: " + child.getKey(), e);
            return null;
        }
    }

    private String getStringSafe(com.google.firebase.database.DataSnapshot snapshot) {
        if (snapshot == null || snapshot.getValue() == null) return "";
        return String.valueOf(snapshot.getValue());
    }


    private void loadOrders() {
        startListeningOrders();
    }

    private void loadLocalOrdersFallback(String userId) {
        progressBar.setVisibility(View.GONE);
        allOrders = DataManager.getInstance(this).getOrders(userId);
        if (allOrders == null) allOrders = new ArrayList<>();
        filterOrders(tabLayout.getSelectedTabPosition());
    }

    private void filterOrders(int tabPosition) {
        if (allOrders == null) return;

        List<AdminOrder> filteredList = allOrders.stream()
                .filter(o -> {
                    String s = o.getStatus() != null ? o.getStatus().toUpperCase(java.util.Locale.ROOT) : "PENDING";
                    if (tabPosition == 0) {
                        // Trạng thái đang xử lý/giao hàng
                        return s.equals("PENDING") || s.equals("CONFIRMED") || s.equals("SHIPPING") || s.equals("IN PROGRESS");
                    }
                    if (tabPosition == 1) {
                        // Trạng thái đã hoàn thành
                        return s.equals("COMPLETED") || s.equals("DELIVERED");
                    }
                    // Trạng thái đã hủy
                    return s.equals("CANCELLED");
                })
                .collect(Collectors.toList());

        updateUI(filteredList);
    }

    private void updateUI(List<AdminOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            
            if (adapter != null) {
                adapter.updateData(orders);
            }
        }
    }
}
