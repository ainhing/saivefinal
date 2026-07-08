package com.example.saive.admin.ui.orders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.model.AdminOrderItem;
import com.example.saive.admin.data.repository.OrderRepository;
import java.util.List;
import java.util.Map;

public class OrderDetailViewModel extends AndroidViewModel {
    private final OrderRepository repository;
    private final MutableLiveData<AdminOrder> order = new MutableLiveData<>();
    private final MutableLiveData<List<AdminOrderItem>> orderItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<AdminOrder> getOrder() { return order; }
    public LiveData<List<AdminOrderItem>> getOrderItems() { return orderItems; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadOrder(String id) {
        repository.getOrderById(id, order, errorMessage);
    }

    public void loadOrderDetails(String orderId) {
        repository.getOrderDetails(orderId, orderItems, errorMessage);
    }

    public void updateOrder(String id, Map<String, Object> updates) {
        repository.updateOrder(id, updates, order, errorMessage);
        updateSuccess.setValue(true);
    }

    public void deleteOrder(String id) {
        repository.deleteOrder(id, deleteSuccess, errorMessage);
    }

    public void clearUpdateSuccess() {
        updateSuccess.setValue(false);
    }
}
