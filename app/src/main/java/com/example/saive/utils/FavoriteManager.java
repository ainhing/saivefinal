package com.example.saive.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.saive.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String PREF_NAME = "saive_favorite_prefs";
    private static final String KEY_FAVORITE_ITEMS = "favorite_items";
    private static FavoriteManager instance;
    private List<Product> favoriteItems;
    private Context context;
    private Gson gson;

    private FavoriteManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.favoriteItems = loadFavoriteItems();
    }

    public static synchronized FavoriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteManager(context);
        }
        return instance;
    }

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged();
    }

    private List<OnFavoriteChangeListener> listeners = new ArrayList<>();

    public void addListener(OnFavoriteChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnFavoriteChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnFavoriteChangeListener listener : listeners) {
            listener.onFavoriteChanged();
        }
    }

    private void saveFavoriteItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(favoriteItems);
        prefs.edit().putString(KEY_FAVORITE_ITEMS, json).apply();
    }

    private List<Product> loadFavoriteItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_FAVORITE_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void toggleFavorite(Product product) {
        if (isFavorite(product)) {
            removeFavorite(product);
        } else {
            addFavorite(product);
        }
    }

    public void addFavorite(Product product) {
        if (!isFavorite(product)) {
            favoriteItems.add(product);
            saveFavoriteItems();
            notifyListeners();
        }
    }

    public void removeFavorite(Product product) {
        Product toRemove = null;
        for (Product item : favoriteItems) {
            if (item.getName().equals(product.getName())) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            favoriteItems.remove(toRemove);
            saveFavoriteItems();
            notifyListeners();
        }
    }

    public boolean isFavorite(Product product) {
        for (Product item : favoriteItems) {
            if (item.getName().equals(product.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<Product> getFavoriteItems() {
        return new ArrayList<>(favoriteItems);
    }

    /**
     * Xóa sạch danh sách yêu thích khi đăng xuất.
     */
    public void clearFavorites() {
        favoriteItems.clear();
        saveFavoriteItems();
        notifyListeners();
    }

    public int getItemCount() {
        return favoriteItems.size();
    }
}
