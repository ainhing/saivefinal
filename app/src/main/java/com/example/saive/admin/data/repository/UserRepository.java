package com.example.saive.admin.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminUser;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepository {
    private static final String NODE_USERS = "Users";

    private final Context context;

    public UserRepository(Context context) {
        this.context = context;
    }

    /**
     * Load users from Firebase.
     * @param onDone optional callback fired on both success and failure so the caller can stop a loading indicator.
     */
    public void getUsers(String query,
                         MutableLiveData<List<AdminUser>> usersLiveData,
                         MutableLiveData<String> errorLiveData,
                         Runnable onDone) {
        FirebaseConnector.getAllItems(NODE_USERS, AdminUser.class, new FirebaseListCallback<AdminUser>() {
            @Override
            public void onSuccess(List<AdminUser> items) {
                try {
                    List<AdminUser> filtered = items;
                    if (query != null && !query.isEmpty()) {
                        String q = query.toLowerCase();
                        filtered = filtered.stream()
                                .filter(u -> (u.getFullname() != null && u.getFullname().toLowerCase().contains(q)) ||
                                             (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                                .collect(Collectors.toList());
                    }
                    usersLiveData.postValue(filtered);
                } catch (Exception e) {
                    errorLiveData.postValue("Error filtering users: " + e.getMessage());
                } finally {
                    if (onDone != null) onDone.run();
                }
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.postValue(error);
                if (onDone != null) onDone.run();
            }
        });
    }

    /** Overload without onDone for backward compatibility */
    public void getUsers(String query,
                         MutableLiveData<List<AdminUser>> usersLiveData,
                         MutableLiveData<String> errorLiveData) {
        getUsers(query, usersLiveData, errorLiveData, null);
    }

    public void getUserById(String id,
                            MutableLiveData<AdminUser> userLiveData,
                            MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getSingleItemById(NODE_USERS, id, AdminUser.class, new FirebaseSingleCallback<AdminUser>() {
            @Override
            public void onSuccess(AdminUser item) {
                item.setUserId(id);
                userLiveData.setValue(item);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
}