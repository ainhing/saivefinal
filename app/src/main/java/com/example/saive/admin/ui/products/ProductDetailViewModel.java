package com.example.saive.admin.ui.products;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.admin.data.repository.ProductRepository;

/**
 * ViewModel cho màn Chi tiết sản phẩm (read-only). Không có hàm update/delete.
 */
public class ProductDetailViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<AdminProduct> product = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<AdminProduct> getProduct() { return product; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadProduct(String id) {
        repository.getProductById(id, product, errorMessage);
    }
}