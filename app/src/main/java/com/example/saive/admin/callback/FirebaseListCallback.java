package com.example.saive.admin.callback;

import java.util.List;

public interface FirebaseListCallback<T> {
    void onSuccess(List<T> items);
    void onFailure(String error);
}
