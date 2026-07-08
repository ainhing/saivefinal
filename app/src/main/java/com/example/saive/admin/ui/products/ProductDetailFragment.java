package com.example.saive.admin.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.databinding.AdminFragmentProductDetailBinding;
import com.example.saive.utils.ImageUtils;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Xem chi tiết sản phẩm - CHỈ ĐỌC (Read-only).
 * Không có chức năng Lưu / Xóa - admin chỉ được xem thông tin.
 */
public class ProductDetailFragment extends Fragment {
    private AdminFragmentProductDetailBinding binding;
    private ProductDetailViewModel viewModel;
    private String productId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        viewModel.getProduct().observe(getViewLifecycleOwner(), this::bindProductData);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        if (productId != null) {
            viewModel.loadProduct(productId);
        }
    }

    private void bindProductData(AdminProduct product) {
        if (product == null) return;

        binding.tvName.setText(product.getProductName());
        binding.tvCategoryId.setText(product.getCategoryId());
        binding.tvDescription.setText(product.getDescription());
        binding.tvOriginalPrice.setText(formatPrice(product.getOriginalPrice()));
        binding.tvSalePrice.setText(formatPrice(product.getPrice()));
        
        String stockInfo = product.getStockBreakdown();
        if (stockInfo.length() > 0) {
            binding.tvStock.setText(getString(R.string.inventory_stock_format, product.getTotalStock()) + ": " + stockInfo);
        } else {
            binding.tvStock.setText(getString(R.string.inventory_stock_format, product.getTotalStock()));
        }

        binding.tvNumBuy.setText(String.valueOf(product.getNumBuy()));
        binding.tvRating.setText(String.valueOf(product.getRating()));
        binding.tvActiveStatus.setText(product.isActive() ? getString(R.string.status_active) : getString(R.string.status_inactive));
        binding.tvFeaturedStatus.setText(product.isFeatured() ? getString(R.string.yes) : getString(R.string.no));

        if (product.getTagStyle() != null || product.getTagType() != null) {
            String tags = (product.getTagStyle() != null ? product.getTagStyle() : "") +
                    (product.getTagType() != null ? " • " + product.getTagType() : "");
            binding.tvTags.setText(tags);
        }

        ImageUtils.setSafeImage(binding.ivProduct, product.getFirstImage(), R.drawable.model1);
    }

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}