package com.example.saive.admin.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminProduct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductRepository {
    private static final String NODE_PRODUCTS = "Products";

    private final Context context;

    public ProductRepository(Context context) {
        this.context = context;
    }

    public void getProducts(String query, MutableLiveData<List<AdminProduct>> productsLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getAllItems(NODE_PRODUCTS, AdminProduct.class, new FirebaseListCallback<AdminProduct>() {
            @Override
            public void onSuccess(List<AdminProduct> items) {
                List<AdminProduct> filtered = items;
                if (query != null && !query.isEmpty()) {
                    String q = query.toLowerCase();
                    filtered = filtered.stream()
                            .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(q))
                            .collect(Collectors.toList());
                }
                productsLiveData.setValue(filtered);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void getProductById(String id, MutableLiveData<AdminProduct> productLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getSingleItemById(NODE_PRODUCTS, id, AdminProduct.class, new FirebaseSingleCallback<AdminProduct>() {
            @Override
            public void onSuccess(AdminProduct item) {
                item.setProductId(id);
                productLiveData.setValue(item);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateProduct(String id, Map<String, Object> updates, MutableLiveData<AdminProduct> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.updateFields(NODE_PRODUCTS, id, updates,
            () -> getProductById(id, resultLiveData, errorLiveData),
            () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_update_failed))
        );
    }
    
    public void deleteProduct(String id, MutableLiveData<Boolean> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.deleteItem(NODE_PRODUCTS, id,
            () -> resultLiveData.setValue(true),
            () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_delete_failed))
        );
    }
}
