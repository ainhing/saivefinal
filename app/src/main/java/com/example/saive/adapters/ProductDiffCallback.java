package com.example.saive.adapters;

import androidx.recyclerview.widget.DiffUtil;
import com.example.saive.models.Product;
import java.util.List;

public class ProductDiffCallback extends DiffUtil.Callback {

    private final List<Product> oldList;
    private final List<Product> newList;

    public ProductDiffCallback(List<Product> oldList, List<Product> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Product oldProduct = oldList.get(oldItemPosition);
        Product newProduct = newList.get(newItemPosition);
        // Assuming name is a unique identifier for simplicity in this app
        return oldProduct.getName().equals(newProduct.getName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Product oldProduct = oldList.get(oldItemPosition);
        Product newProduct = newList.get(newItemPosition);
        return oldProduct.equals(newProduct) &&
                oldProduct.getPrice().equals(newProduct.getPrice()) &&
                oldProduct.getImageResId() == newProduct.getImageResId() &&
                oldProduct.getQuantity() == newProduct.getQuantity();
    }
}
