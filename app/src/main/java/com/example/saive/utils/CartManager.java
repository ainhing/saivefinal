package com.example.saive.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.saive.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "saive_cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";

    private static CartManager instance;
    private List<Product> cartItems;
    private Context context;
    private Gson gson;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.cartItems = loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    private List<OnCartChangeListener> listeners = new ArrayList<>();

    public void addListener(OnCartChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnCartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnCartChangeListener listener : listeners) {
            listener.onCartChanged();
        }
    }

    private void saveCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(cartItems);
        prefs.edit().putString(KEY_CART_ITEMS, json).apply();
    }

    private List<Product> loadCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CART_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void addProduct(Product product) {
        addProduct(product, 1);
    }

    public void addProduct(Product product, int quantity) {
        boolean exists = false;
        for (Product item : cartItems) {
            // Kiểm tra trùng cả Tên, Size và Màu sắc
            if (isSameVariant(item, product)) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }
        if (!exists) {
            // Tạo một bản sao để tránh tham chiếu đến đối tượng cũ
            Product newProduct = new Product(product.getName(), product.getPrice(), product.getOriginalPrice(),
                    product.getImageResId(), product.getCategory());
            newProduct.setImageUrl(product.getImageUrl());
            newProduct.setImageUrls(product.getImageUrls());
            newProduct.setImageResIds(product.getImageResIds());
            newProduct.setProductId(product.getProductId()); // QUAN TRỌNG: Giữ lại ID sản phẩm
            newProduct.setDescription(product.getDescription());
            newProduct.setSelectedSize(product.getSelectedSize());
            newProduct.setSelectedColor(product.getSelectedColor());
            newProduct.setQuantity(quantity);
            cartItems.add(newProduct);
        }
        saveCartItems();
        notifyListeners();
    }

    public void removeProduct(Product product) {
        Product itemToRemove = null;
        for (Product item : cartItems) {
            if (isSameVariant(item, product)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            cartItems.remove(itemToRemove);
            saveCartItems();
            notifyListeners();
        }
    }

    public boolean isProductInCart(Product product) {
        for (Product item : cartItems) {
            if (isSameVariant(item, product)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameVariant(Product p1, Product p2) {
        if (p1 == null || p2 == null)
            return false;

        boolean sameName = p1.getName().equals(p2.getName());
        boolean sameSize = (p1.getSelectedSize() == null && p2.getSelectedSize() == null) ||
                (p1.getSelectedSize() != null && p1.getSelectedSize().equals(p2.getSelectedSize()));
        boolean sameColor = (p1.getSelectedColor() == null && p2.getSelectedColor() == null) ||
                (p1.getSelectedColor() != null && p1.getSelectedColor().equals(p2.getSelectedColor()));

        return sameName && sameSize && sameColor;
    }

    public void updateQuantity(Product product, int newQuantity) {
        for (Product item : cartItems) {
            if (isSameVariant(item, product)) {
                if (newQuantity <= 0) {
                    removeProduct(item);
                } else {
                    item.setQuantity(newQuantity);
                    saveCartItems();
                    notifyListeners();
                }
                break;
            }
        }
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Product item : cartItems) {
            total += PriceFormatter.parsePrice(item.getPrice()) * item.getQuantity();
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (Product item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
        notifyListeners();
    }

    /**
     * Clear RAM cache and notify listeners.
     */
    public void clearCache() {
        cartItems.clear();
        notifyListeners();
    }
}
