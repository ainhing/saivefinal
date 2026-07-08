package com.example.saive.admin.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminBlog;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlogRepository {
    private static final String NODE_BLOGS = "Blogs";

    private final Context context;

    public BlogRepository(Context context) {
        this.context = context;
    }

    public void getBlogs(MutableLiveData<List<AdminBlog>> blogsLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getAllItems(NODE_BLOGS, AdminBlog.class, new FirebaseListCallback<AdminBlog>() {
            @Override
            public void onSuccess(List<AdminBlog> items) {
                blogsLiveData.setValue(items);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void getBlogById(String id, MutableLiveData<AdminBlog> blogLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getSingleItemById(NODE_BLOGS, id, AdminBlog.class, new FirebaseSingleCallback<AdminBlog>() {
            @Override
            public void onSuccess(AdminBlog item) {
                item.setBlogId(id);
                blogLiveData.setValue(item);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateBlog(String id, Map<String, Object> updates, MutableLiveData<AdminBlog> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.updateFields(NODE_BLOGS, id, updates,
            () -> getBlogById(id, resultLiveData, errorLiveData),
            () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_update_failed))
        );
    }

    public void deleteBlog(String id, MutableLiveData<Boolean> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.deleteItem(NODE_BLOGS, id,
            () -> resultLiveData.setValue(true),
            () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_delete_failed))
        );
    }

    public void createBlog(AdminBlog blog, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        // Read all existing blog keys first to find the next sequential BLGxxx ID
        FirebaseConnector.getDatabase().getReference(NODE_BLOGS)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    int maxIdx = 0;
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        String k = child.getKey();
                        if (k != null && k.toUpperCase().startsWith("BLG")) {
                            try {
                                int idx = Integer.parseInt(k.substring(3));
                                if (idx > maxIdx) maxIdx = idx;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    String newId = String.format("BLG%03d", maxIdx + 1);
                    blog.setBlogId(newId);
                    FirebaseConnector.getDatabase().getReference(NODE_BLOGS).child(newId).setValue(blog)
                        .addOnSuccessListener(aVoid -> successLiveData.postValue(true))
                        .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    errorLiveData.postValue(error.getMessage());
                }
            });
    }
}
