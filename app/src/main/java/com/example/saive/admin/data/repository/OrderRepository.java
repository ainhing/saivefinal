package com.example.saive.admin.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.model.AdminOrderItem;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final String NODE_ORDERS = "Orders";

    private final Context context;

    public OrderRepository(Context context) {
        this.context = context;
    }

    public void getOrders(String status, String query, MutableLiveData<List<AdminOrder>> ordersLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.listenToAllItems(NODE_ORDERS, AdminOrder.class, new FirebaseListCallback<AdminOrder>() {
            @Override
            public void onSuccess(List<AdminOrder> items) {
                List<AdminOrder> filtered = items;
                if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                    filtered = filtered.stream()
                            .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase(status))
                            .collect(Collectors.toList());
                }
                if (query != null && !query.isEmpty()) {
                    String q = query.toLowerCase();
                    filtered = filtered.stream()
                            .filter(o -> o.getFullName() != null && o.getFullName().toLowerCase().contains(q))
                            .collect(Collectors.toList());
                }
                ordersLiveData.setValue(filtered);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void getOrderById(String id, MutableLiveData<AdminOrder> orderLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getSingleItemById(NODE_ORDERS, id, AdminOrder.class, new FirebaseSingleCallback<AdminOrder>() {
            @Override
            public void onSuccess(AdminOrder item) {
                item.setOrderId(id);
                orderLiveData.setValue(item);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateOrder(String id, Map<String, Object> updates, MutableLiveData<AdminOrder> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.updateFields(NODE_ORDERS, id, updates,
                () -> getOrderById(id, resultLiveData, errorLiveData),
                () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_update_failed))
        );
    }

    public void deleteOrder(String id, MutableLiveData<Boolean> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.deleteItem(NODE_ORDERS, id,
                () -> resultLiveData.setValue(true),
                () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_delete_failed))
        );
    }

    public void getOrderDetails(String orderId, MutableLiveData<List<AdminOrderItem>> itemsLiveData, MutableLiveData<String> errorLiveData) {
        com.google.firebase.database.Query query = FirebaseConnector.getDatabase()
                .getReference("OrderDetails")
                .orderByChild("OrderId")
                .equalTo(orderId);

        FirebaseConnector.listenToQuery(query, AdminOrderItem.class, new FirebaseListCallback<AdminOrderItem>() {
            @Override
            public void onSuccess(List<AdminOrderItem> items) {
                itemsLiveData.setValue(items);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
}