package com.example.saive.admin.ui.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.saive.R;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminCategory;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.databinding.AdminFragmentProductsBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý Sản phẩm - CHỈ ĐỌC (Read-only) + Lọc theo Category động từ Firebase.
 */
public class ProductsFragment extends Fragment {
    private AdminFragmentProductsBinding binding;
    private ProductsViewModel viewModel;
    private ProductAdapter adapter;
    
    private List<AdminProduct> allProducts = new ArrayList<>();
    private String selectedCategoryId = "ALL";
    private DatabaseReference categoriesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductsViewModel.class);
        categoriesRef = FirebaseConnector.getDatabase().getReference("Categories");

        setupRecyclerView();
        setupSearch();
        setupObservers();

        loadCategories();
        viewModel.loadProducts("");
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getProductId());
            Navigation.findNavController(requireView()).navigate(R.id.action_products_to_detail, bundle);
        });
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            allProducts = products != null ? products : new ArrayList<>();
            filterProducts();
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadProducts(""));
    }

    private void loadCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AdminCategory> categoriesList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AdminCategory cat = child.getValue(AdminCategory.class);
                    if (cat != null) {
                        cat.setCategoryId(child.getKey());
                        categoriesList.add(cat);
                    }
                }
                setupCategoryChips(categoriesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh mục: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupCategoryChips(List<AdminCategory> categories) {
        if (binding == null || getContext() == null) return;
        binding.chipGroupCategories.removeAllViews();

        // ALL Chip
        Chip allChip = new Chip(getContext());
        allChip.setText(getString(R.string.cat_all));
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag("ALL");
        binding.chipGroupCategories.addView(allChip);

        // Individual Category Chips
        for (AdminCategory cat : categories) {
            if (!cat.isActive()) continue;
            Chip chip = new Chip(getContext());
            chip.setText(getLocalizedCategoryName(cat));
            chip.setCheckable(true);
            chip.setTag(cat.getCategoryId());
            binding.chipGroupCategories.addView(chip);
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategoryId = "ALL";
                allChip.setChecked(true);
            } else {
                int checkedId = checkedIds.get(0);
                Chip selectedChip = group.findViewById(checkedId);
                if (selectedChip != null && selectedChip.getTag() != null) {
                    selectedCategoryId = selectedChip.getTag().toString();
                } else {
                    selectedCategoryId = "ALL";
                }
            }
            filterProducts();
        });
    }

    private String getLocalizedCategoryName(AdminCategory cat) {
        String name = cat.getCategoryName();
        if (name == null) return cat.getCategoryId();
        
        String lowercaseName = name.toLowerCase().trim();
        if (lowercaseName.equals("all")) return getString(R.string.cat_all);
        if (lowercaseName.equals("top")) return getString(R.string.cat_top);
        if (lowercaseName.equals("bottom")) return getString(R.string.cat_bottom);
        if (lowercaseName.equals("dress")) return getString(R.string.cat_dress);
        if (lowercaseName.equals("outerwear")) return getString(R.string.cat_outerwear);
        if (lowercaseName.equals("shoes")) return getString(R.string.cat_shoes);
        if (lowercaseName.equals("bag")) return getString(R.string.cat_bag);
        if (lowercaseName.equals("accessory") || lowercaseName.equals("accessories")) return getString(R.string.cat_accessory);
        
        return name;
    }

    private void filterProducts() {
        if (binding == null) return;
        List<AdminProduct> filtered = new ArrayList<>();
        String query = binding.etSearch.getText().toString().toLowerCase().trim();

        for (AdminProduct p : allProducts) {
            boolean matchesCategory = selectedCategoryId.equals("ALL") || 
                    (p.getCategoryId() != null && p.getCategoryId().equalsIgnoreCase(selectedCategoryId));
            boolean matchesSearch = query.isEmpty() || 
                    (p.getProductName() != null && p.getProductName().toLowerCase().contains(query));
            if (matchesCategory && matchesSearch) {
                filtered.add(p);
            }
        }
        adapter.setProducts(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}