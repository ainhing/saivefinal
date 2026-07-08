package com.example.saive.admin.ui.products;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.admin.data.repository.ProductRepository;
import java.util.List;

/**
 * ViewModel cho danh sách sản phẩm - CHỈ ĐỌC (Read-only).
 */
public class ProductsViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<List<AdminProduct>> products = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductsViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<AdminProduct>> getProducts() { return products; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadProducts(String query) {
        isLoading.setValue(true);
        repository.getProducts(query, products, errorMessage);
    }
}