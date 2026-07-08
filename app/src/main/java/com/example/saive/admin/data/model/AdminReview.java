package com.example.saive.admin.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

@IgnoreExtraProperties
public class AdminReview {
    private String reviewId; // populated from key

    public String ProductId;
    public String ProductName;
    public String CustomerId;
    public String UserName;
    public Object Rating;
    public String Content;
    public String Comment;
    public Object Likecount;
    public String Created_at;
    public String Date;
    public Object IsApproved;
    public String Status;

    public AdminReview() {
        // Required for Firebase
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    @Exclude
    public String getProductId() { return ProductId; }

    @Exclude
    public String getProductName() {
        if (ProductName != null && !ProductName.isEmpty()) return ProductName;
        return ProductId;
    }

    @Exclude
    public String getCustomerId() {
        if (CustomerId != null && !CustomerId.isEmpty()) return CustomerId;
        return UserName;
    }

    @Exclude
    public float getRating() {
        if (Rating instanceof Number) {
            return ((Number) Rating).floatValue();
        } else if (Rating instanceof String) {
            try {
                return Float.parseFloat((String) Rating);
            } catch (Exception e) {
                return 0.0f;
            }
        }
        return 0.0f;
    }

    @Exclude
    public String getContent() {
        if (Content != null && !Content.isEmpty()) return Content;
        return Comment;
    }

    @Exclude
    public int getLikecount() {
        if (Likecount instanceof Number) {
            return ((Number) Likecount).intValue();
        } else if (Likecount instanceof String) {
            try {
                return Integer.parseInt((String) Likecount);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    @Exclude
    public String getCreatedAt() {
        if (Created_at != null && !Created_at.isEmpty()) return Created_at;
        return Date;
    }

    @Exclude
    public String getEffectiveStatus() {
        if (Status != null && !Status.trim().isEmpty()) {
            return Status;
        }
        if (IsApproved instanceof Boolean) {
            return ((Boolean) IsApproved) ? "approved" : "pending";
        } else if (IsApproved instanceof String) {
            return "true".equalsIgnoreCase((String) IsApproved) ? "approved" : "pending";
        }
        return "pending";
    }
}