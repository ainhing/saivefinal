package com.example.saive.admin.ui.orders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.repository.OrderRepository;
import java.util.List;

public class OrdersViewModel extends AndroidViewModel {
    private OrderRepository repository;
    private MutableLiveData<List<AdminOrder>> orders = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<List<AdminOrder>> getOrders() { return orders; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadOrders(String status, String query) {
        isLoading.setValue(true);
        repository.getOrders(status, query, orders, errorMessage);
        // We'll need to update repository to toggle isLoading, but for now simple:
        // Better way: Repository returns LiveData or we pass observers
    }
}
