package com.example.saive.admin.ui.blogs;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminBlog;
import com.example.saive.admin.data.repository.BlogRepository;

import java.util.Map;

/**
 * ViewModel cho màn Chi tiết bài viết.
 */
public class BlogDetailViewModel extends AndroidViewModel {
    private final BlogRepository repository;
    private final MutableLiveData<AdminBlog> blog = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BlogDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new BlogRepository(application);
    }

    public LiveData<AdminBlog> getBlog() { return blog; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadBlog(String id) {
        repository.getBlogById(id, blog, errorMessage);
    }

    public void updateBlog(String id, Map<String, Object> updates) {
        repository.updateBlog(id, updates, blog, errorMessage);
        updateSuccess.setValue(true);
    }

    public void deleteBlog(String id) {
        repository.deleteBlog(id, deleteSuccess, errorMessage);
    }

    public void clearUpdateSuccess() {
        updateSuccess.setValue(false);
    }
}