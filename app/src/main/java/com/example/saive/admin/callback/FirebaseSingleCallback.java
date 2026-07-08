package com.example.saive.admin.callback;

public interface FirebaseSingleCallback<T> {
    void onSuccess(T item);
    void onFailure(String error);
}
