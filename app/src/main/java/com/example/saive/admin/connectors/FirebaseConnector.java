package com.example.saive.admin.connectors;

import androidx.annotation.NonNull;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.data.model.AdminBlog;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.model.AdminProduct;
import com.example.saive.admin.data.model.AdminReview;
import com.example.saive.admin.data.model.AdminUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FirebaseConnector {
    private static final String DATABASE_URL = "https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app";

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
    }

    /** Stamps the node key onto the model's id field, since the key is not part of the JSON value itself. */
    private static <T> void applyKeyAsId(T item, String key) {
        if (item instanceof AdminOrder) {
            ((AdminOrder) item).setOrderId(key);
        } else if (item instanceof AdminProduct) {
            ((AdminProduct) item).setProductId(key);
        } else if (item instanceof AdminUser) {
            ((AdminUser) item).setUserId(key);
        } else if (item instanceof AdminBlog) {
            ((AdminBlog) item).setBlogId(key);
        } else if (item instanceof AdminReview) {
            ((AdminReview) item).setReviewId(key);
        }
    }

    public static <T> void getSingleItemById(String node, String itemId, Class<T> clazz, FirebaseSingleCallback<T> callback) {
        getDatabase().getReference(node).child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                T item = snapshot.getValue(clazz);
                if (item != null) {
                    applyKeyAsId(item, itemId);
                    callback.onSuccess(item);
                } else {
                    callback.onFailure("Item not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public static <T> ValueEventListener listenToAllItems(String node, Class<T> clazz, FirebaseListCallback<T> callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    T item = child.getValue(clazz);
                    if (item != null) {
                        applyKeyAsId(item, child.getKey());
                        items.add(item);
                    }
                }
                callback.onSuccess(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };
        getDatabase().getReference(node).addValueEventListener(listener);
        return listener;
    }

    public static void removeListener(String node, ValueEventListener listener) {
        getDatabase().getReference(node).removeEventListener(listener);
    }

    public static <T> ValueEventListener listenToQuery(com.google.firebase.database.Query query, Class<T> clazz, FirebaseListCallback<T> callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    T item = child.getValue(clazz);
                    if (item != null) {
                        applyKeyAsId(item, child.getKey());
                        items.add(item);
                    }
                }
                callback.onSuccess(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };
        query.addValueEventListener(listener);
        return listener;
    }

    public static <T> void getAllItems(String node, Class<T> clazz, FirebaseListCallback<T> callback) {
        getDatabase().getReference(node).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    T item = child.getValue(clazz);
                    if (item != null) {
                        applyKeyAsId(item, child.getKey());
                        items.add(item);
                    }
                }
                callback.onSuccess(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public static void updateFields(String node, String itemId, Map<String, Object> updates, Runnable onSuccess, Runnable onFailure) {
        getDatabase().getReference(node).child(itemId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    public static void deleteItem(String node, String itemId, Runnable onSuccess, Runnable onFailure) {
        getDatabase().getReference(node).child(itemId).removeValue()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }
}
