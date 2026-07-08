package com.example.saive.admin.ui.blogs;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminBlog;
import com.example.saive.admin.data.repository.BlogRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel cho danh sách Blog - CHỈ ĐỌC (Read-only).
 */
public class BlogsViewModel extends AndroidViewModel {
    private final BlogRepository repository;
    private final MutableLiveData<List<AdminBlog>> blogs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private List<AdminBlog> allBlogs;

    public BlogsViewModel(@NonNull Application application) {
        super(application);
        repository = new BlogRepository(application);
    }

    public LiveData<List<AdminBlog>> getBlogs() { return blogs; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadBlogs() {
        isLoading.setValue(true);
        repository.getBlogs(new MutableLiveData<List<AdminBlog>>() {
            @Override
            public void setValue(List<AdminBlog> value) {
                super.setValue(value);
                allBlogs = value;
                isLoading.setValue(false);
                blogs.setValue(value);
            }
        }, errorMessage);
    }

    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getCreateSuccess() { return createSuccess; }

    public void clearCreateSuccess() { createSuccess.setValue(false); }

    public void createBlog(AdminBlog blog) {
        isLoading.setValue(true);
        repository.createBlog(blog, createSuccess, errorMessage);
    }

    public void search(String query) {
        if (allBlogs == null) return;
        if (query == null || query.isEmpty()) {
            blogs.setValue(allBlogs);
            return;
        }
        String q = query.toLowerCase();
        blogs.setValue(allBlogs.stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) ||
                        (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q)))
                .collect(Collectors.toList()));
    }
}