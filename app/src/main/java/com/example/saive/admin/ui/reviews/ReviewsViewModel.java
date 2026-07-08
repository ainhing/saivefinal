package com.example.saive.admin.ui.reviews;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.data.model.AdminReview;
import com.example.saive.admin.data.repository.ReviewRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel cho Reviews - R (danh sách theo tab status) + U (đổi Status qua swipe).
 */
public class ReviewsViewModel extends AndroidViewModel {
    private final ReviewRepository repository;
    private final MutableLiveData<List<AdminReview>> reviews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ReviewsViewModel(@NonNull Application application) {
        super(application);
        repository = new ReviewRepository(application);
    }

    public LiveData<List<AdminReview>> getReviews() { return reviews; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadReviews(String status) {
        isLoading.setValue(true);
        repository.getReviews(status, "", reviews, errorMessage);
    }

    /** Đổi Status của 1 review (approved / rejected / pending) rồi tải lại danh sách hiện tại. */
    public void updateStatus(String reviewId, String newStatus, String currentTabStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Status", newStatus);
        updates.put("IsApproved", "approved".equals(newStatus));
        repository.updateReview(reviewId, updates, new MutableLiveData<>(), errorMessage);
        loadReviews(currentTabStatus);
    }
}