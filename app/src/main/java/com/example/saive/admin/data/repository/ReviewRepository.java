package com.example.saive.admin.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.callback.FirebaseListCallback;
import com.example.saive.admin.callback.FirebaseSingleCallback;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminReview;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReviewRepository {
    private static final String NODE_REVIEWS = "Reviews";

    private final Context context;

    public ReviewRepository(Context context) {
        this.context = context;
    }

    public void getReviews(String status, String query, MutableLiveData<List<AdminReview>> reviewsLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.listenToAllItems(NODE_REVIEWS, AdminReview.class, new FirebaseListCallback<AdminReview>() {
            @Override
            public void onSuccess(List<AdminReview> items) {
                List<AdminReview> filtered = items;
                if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                    filtered = filtered.stream()
                            .filter(r -> r.getEffectiveStatus().equalsIgnoreCase(status))
                            .collect(Collectors.toList());
                }
                if (query != null && !query.isEmpty()) {
                    String q = query.toLowerCase();
                    filtered = filtered.stream()
                            .filter(r -> (r.getContent() != null && r.getContent().toLowerCase().contains(q)) ||
                                    (r.getCustomerId() != null && r.getCustomerId().toLowerCase().contains(q)))
                            .collect(Collectors.toList());
                }
                reviewsLiveData.setValue(filtered);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void getReviewById(String id, MutableLiveData<AdminReview> reviewLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.getSingleItemById(NODE_REVIEWS, id, AdminReview.class, new FirebaseSingleCallback<AdminReview>() {
            @Override
            public void onSuccess(AdminReview item) {
                item.setReviewId(id);
                reviewLiveData.setValue(item);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateReview(String id, Map<String, Object> updates, MutableLiveData<AdminReview> resultLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseConnector.updateFields(NODE_REVIEWS, id, updates,
                () -> getReviewById(id, resultLiveData, errorLiveData),
                () -> errorLiveData.setValue(context.getString(com.example.saive.R.string.admin_error_update_failed))
        );
    }
}