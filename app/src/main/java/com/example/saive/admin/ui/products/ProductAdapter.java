package com.example.saive.admin.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.R;
import com.example.saive.databinding.AdminItemProductBinding;
import com.example.saive.utils.ImageUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<AdminProduct> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(AdminProduct product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<AdminProduct> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemProductBinding binding = AdminItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemProductBinding binding;

        public ProductViewHolder(AdminItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminProduct product) {
            binding.tvProductName.setText(product.getProductName());
            binding.tvProductPrice.setText(formatPrice(product.getPrice()));
            binding.tvStock.setText(binding.getRoot().getContext().getString(R.string.inventory_stock_format, product.getTotalStock()));

            ImageUtils.setSafeImage(binding.ivProduct, product.getFirstImage(), R.drawable.model1);

            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }

        private String formatPrice(double price) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return nf.format(price);
        }
    }
}