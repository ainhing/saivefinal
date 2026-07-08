package com.example.saive.admin.ui.users;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminUser;
import com.example.saive.admin.data.repository.UserRepository;
import java.util.List;

/**
 * ViewModel cho danh sách người dùng - CHỈ ĐỌC (Read-only).
 */
public class UsersViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<List<AdminUser>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UsersViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<List<AdminUser>> getUsers() { return users; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadUsers(String query) {
        isLoading.setValue(true);
        repository.getUsers(query, users, errorMessage, () -> isLoading.postValue(false));
    }
}