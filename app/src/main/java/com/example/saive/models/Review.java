package com.example.saive.models;

import java.io.Serializable;
import java.util.List;

public class Review implements Serializable {
    // Schema chuẩn (review submit từ app)
    private String userName;
    private float rating;
    private String comment;
    private String date;
    private List<String> imageUrls;
    private String productName;
    private String productId;
    private boolean isApproved;

    // Schema cũ / seed data (Admin format)
    private String legacyContent;
    private String legacyCreatedAt;
    private String legacyCustomerId;
    private String legacyUserName;

    public Review() {}

    public Review(String productName, String userName, float rating, String comment, String date, List<String> imageUrls) {
        this.productName = productName;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.imageUrls = imageUrls;
        this.isApproved = false;
    }

    public String getUserName() {
        if (userName != null && !userName.isEmpty()) return userName;
        if (legacyUserName != null && !legacyUserName.isEmpty()) return legacyUserName;
        if (legacyCustomerId != null && !legacyCustomerId.isEmpty()) return legacyCustomerId;
        return "Khách hàng";
    }

    public float getRating() { return rating; }

    public String getComment() {
        if (comment != null && !comment.isEmpty()) return comment;
        if (legacyContent != null && !legacyContent.isEmpty()) return legacyContent;
        return "";
    }

    public String getDate() {
        if (date != null && !date.isEmpty()) return date;
        if (legacyCreatedAt != null && !legacyCreatedAt.isEmpty()) return legacyCreatedAt;
        return "";
    }

    public String getProductName() { return productName; }
    public String getProductId() { return productId; }
    public void setProductId(Object productId) {
        this.productId = productId == null ? null : String.valueOf(productId);
    }
    public List<String> getImageUrls() { return imageUrls; }

    @com.google.firebase.database.PropertyName("IsApproved")
    public boolean isApproved() { return isApproved; }

    @com.google.firebase.database.PropertyName("IsApproved")
    public void setApproved(boolean approved) { isApproved = approved; }

    // --- Bind tường minh với key gốc trên Firebase (bắt buộc để khớp chính xác chữ hoa) ---

    @com.google.firebase.database.PropertyName("Content")
    public String getLegacyContent() { return legacyContent; }
    @com.google.firebase.database.PropertyName("Content")
    public void setLegacyContent(String v) { legacyContent = v; }

    @com.google.firebase.database.PropertyName("Created_at")
    public String getLegacyCreatedAt() { return legacyCreatedAt; }
    @com.google.firebase.database.PropertyName("Created_at")
    public void setLegacyCreatedAt(String v) { legacyCreatedAt = v; }

    @com.google.firebase.database.PropertyName("CustomerId")
    public String getLegacyCustomerId() { return legacyCustomerId; }
    @com.google.firebase.database.PropertyName("CustomerId")
    public void setLegacyCustomerId(String v) { legacyCustomerId = v; }

    @com.google.firebase.database.PropertyName("UserName")
    public String getLegacyUserName() { return legacyUserName; }
    @com.google.firebase.database.PropertyName("UserName")
    public void setLegacyUserName(String v) { legacyUserName = v; }
}