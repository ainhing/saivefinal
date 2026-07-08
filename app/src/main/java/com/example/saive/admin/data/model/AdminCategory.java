package com.example.saive.admin.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class AdminCategory {
    private String categoryId; // Firebase node key

    @PropertyName("CategoryName")
    private String categoryName;

    @PropertyName("Description")
    private String description;

    @PropertyName("IsActive")
    private boolean isActive = true;

    public AdminCategory() {
        // Required for Firebase
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @PropertyName("CategoryName")
    public String getCategoryName() {
        return categoryName;
    }

    @PropertyName("CategoryName")
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @PropertyName("Description")
    public String getDescription() {
        return description;
    }

    @PropertyName("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("IsActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("IsActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
