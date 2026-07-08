package com.example.saive.admin.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.databinding.AdminFragmentProductEditBinding;
import com.example.saive.utils.ImageUtils;
import java.util.HashMap;
import java.util.Map;

public class ProductEditFragment extends Fragment {
    private AdminFragmentProductEditBinding binding;
    private ProductsViewModel viewModel;
    private String productId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentProductEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductsViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        setupToolbar();
        setupObservers();

        if (productId != null) {
            binding.toolbar.setTitle(R.string.address_edit_title);
            // In a real app, load product detail. Here we might need a ProductDetailViewModel 
            // but for simplicity we use the list viewModel if it had a getProductById
            // Let's assume we can fetch it.
            fetchProductData(productId);
        } else {
            binding.toolbar.setTitle(R.string.address_add_title);
            binding.btnDelete.setVisibility(View.GONE);
        }

        binding.btnSave.setOnClickListener(v -> saveProduct());
        binding.btnDelete.setOnClickListener(v -> deleteProduct());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void fetchProductData(String id) {
        // Simple mock/fetch logic
        com.example.saive.admin.data.repository.ProductRepository repo = new com.example.saive.admin.data.repository.ProductRepository(requireContext());
        repo.getProductById(id, new androidx.lifecycle.MutableLiveData<AdminProduct>() {{
            observe(getViewLifecycleOwner(), product -> {
                if (product != null) bindProductData(product);
            });
        }}, new androidx.lifecycle.MutableLiveData<>());
    }

    private void bindProductData(AdminProduct product) {
        binding.etName.setText(product.getProductName());
        binding.etCategory.setText(product.getCategoryId());
        binding.etOriginalPrice.setText(String.valueOf(product.getOriginalPrice()));
        binding.etSalePrice.setText(String.valueOf(product.getPrice()));
        binding.etStock.setText(String.valueOf(product.getTotalStock()));
        binding.swActive.setChecked(product.isActive());
        binding.swFeatured.setChecked(product.isFeatured());
        ImageUtils.setSafeImage(binding.ivProduct, product.getFirstImage(), R.drawable.model1);
    }

    private void saveProduct() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("ProductName", binding.etName.getText().toString());
        updates.put("CategoryId", binding.etCategory.getText().toString());
        try {
            updates.put("OriginalPrice", Double.parseDouble(binding.etOriginalPrice.getText().toString()));
            updates.put("Price", Double.parseDouble(binding.etSalePrice.getText().toString()));
            // AdminProduct maps 'Stock' to the DB node. 
            // Warning: this will overwrite the nested Map structure if it exists.
            updates.put("Stock", Integer.parseInt(binding.etStock.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            return;
        }
        updates.put("IsActive", binding.swActive.isChecked());
        updates.put("IsFeatured", binding.swFeatured.isChecked());

        com.example.saive.admin.data.repository.ProductRepository repo = new com.example.saive.admin.data.repository.ProductRepository(requireContext());
        if (productId != null) {
            repo.updateProduct(productId, updates, new androidx.lifecycle.MutableLiveData<>(), new androidx.lifecycle.MutableLiveData<>());
            Toast.makeText(getContext(), R.string.toast_update_success, Toast.LENGTH_SHORT).show();
        } else {
            // Add new logic if repo supports it
            Toast.makeText(getContext(), "Tính năng thêm mới sẽ được cập nhật", Toast.LENGTH_SHORT).show();
        }
        Navigation.findNavController(requireView()).popBackStack();
    }

    private void deleteProduct() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton(R.string.dialog_delete_confirm, (dialog, which) -> {
                    com.example.saive.admin.data.repository.ProductRepository repo = new com.example.saive.admin.data.repository.ProductRepository(requireContext());
                    repo.deleteProduct(productId, new androidx.lifecycle.MutableLiveData<>(), new androidx.lifecycle.MutableLiveData<>());
                    Toast.makeText(getContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void setupObservers() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}