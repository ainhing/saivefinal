package com.example.saive.admin.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Maps 1:1 to the real "Users" node in Firebase Realtime Database.
 * DB Fields (PascalCase): AuthUid, CreatedAt, DisplayName, Email, IsActive, Password, Phone, Role, UserId
 */
@IgnoreExtraProperties
public class AdminUser {
    private String userId; // Populated from key

    @PropertyName("AuthUid")
    private String authUid;
    
    @PropertyName("CreatedAt")
    private String createdAt;
    
    @PropertyName("DisplayName")
    private String displayName;
    
    @PropertyName("Email")
    private String email;
    
    @PropertyName("IsActive")
    private Object isActive;
    
    @PropertyName("Password")
    private String password;
    
    @PropertyName("Phone")
    private String phone;
    
    @PropertyName("Role")
    private String role;
    
    @PropertyName("AvatarUrl")
    private String avatarUrl;

    public AdminUser() {
        // Required for Firebase
    }

    @Exclude
    public String getUserId() { return userId; }
    @Exclude
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("AuthUid")
    public String getAuthUid() { return authUid; }
    @PropertyName("AuthUid")
    public void setAuthUid(String authUid) { this.authUid = authUid; }

    @PropertyName("CreatedAt")
    public String getCreatedAt() { return createdAt; }
    @PropertyName("CreatedAt")
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @PropertyName("DisplayName")
    public String getDisplayName() { return displayName; }
    @PropertyName("DisplayName")
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @Exclude
    public String getFullname() { 
        return displayName != null ? displayName : ""; 
    }

    @PropertyName("Email")
    public String getEmail() { return email; }
    @PropertyName("Email")
    public void setEmail(String email) { this.email = email; }

    @PropertyName("IsActive")
    public Object getIsActiveObj() { return isActive; }
    @PropertyName("IsActive")
    public void setIsActive(Object isActive) { this.isActive = isActive; }

    @Exclude
    public boolean isActive() {
        if (isActive instanceof Boolean) return (Boolean) isActive;
        if (isActive instanceof String) return "true".equalsIgnoreCase((String) isActive);
        return true; // default
    }

    @PropertyName("Password")
    public String getPassword() { return password; }
    @PropertyName("Password")
    public void setPassword(String password) { this.password = password; }

    @PropertyName("Phone")
    public String getPhone() { return phone; }
    @PropertyName("Phone")
    public void setPhone(String phone) { this.phone = phone; }

    @PropertyName("Role")
    public String getRole() { return role; }
    @PropertyName("Role")
    public void setRole(String role) { this.role = role; }

    @PropertyName("AvatarUrl")
    public String getAvatarUrl() { return avatarUrl; }
    @PropertyName("AvatarUrl")
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @Exclude
    public String getProvider() { return authUid; }

    @Exclude
    public boolean isAdmin() {
        return role != null && role.equalsIgnoreCase("admin");
    }
}
