package com.example.saive.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Quản lý session đăng nhập USER dựa hoàn toàn vào Firebase Auth.
 * KHÔNG dùng SharedPreferences hay LocalStorage để lưu trạng thái đăng nhập.
 * Firebase Auth tự persist token an toàn và tự refresh.
 *
 * Thông tin user (tên, email, role...) được query từ Firebase Realtime Database.
 * Cached trong memory, sẽ mất khi app bị kill (không sao — sẽ reload từ DB lại).
 */
public class UserSession {

    // ── In-memory cache (không persist vào disk) ──────────────────────
    private String userId;       // key trong Firebase DB (vd: "U002")
    private String email;
    private String displayName;
    private String role;
    private String avatarUrl;
    private String phone;
    private boolean cacheReady = false;

    // ── Singleton ─────────────────────────────────────────────────────
    private static volatile UserSession instance;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }

    // ── Login check — dựa vào FirebaseAuth ───────────────────────────

    /** Trả về true nếu đang có phiên đăng nhập Firebase Auth hợp lệ */
    public boolean isLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    /** Email của user đang đăng nhập (từ FirebaseAuth) */
    public String getFirebaseEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    // ── Cache setter (gọi sau khi đăng nhập thành công + query DB) ───

    public void setCache(String userId, String email, String displayName,
                         String role, String avatarUrl, String phone) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.role = role != null ? role : "customer";
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.cacheReady = true;

        updateFcmTokenInDatabase();
    }

    private void updateFcmTokenInDatabase() {
        if (userId != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            com.example.saive.admin.connectors.FirebaseConnector.getDatabase()
                                    .getReference("Users")
                                    .child(userId)
                                    .child("fcmToken")
                                    .setValue(token);
                        }
                    });
        }
    }

    public void clearCache() {
        this.userId = null;
        this.email = null;
        this.displayName = null;
        this.role = null;
        this.avatarUrl = null;
        this.phone = null;
        this.cacheReady = false;
    }

    // ── Logout ────────────────────────────────────────────────────────

    public void logout(android.content.Context context) {
        // 0. Remove FCM Token from Database before signing out
        if (userId != null) {
            com.example.saive.admin.connectors.FirebaseConnector.getDatabase()
                    .getReference("Users")
                    .child(userId)
                    .child("fcmToken")
                    .removeValue();
        }

        // 1. Firebase Sign Out
        FirebaseAuth.getInstance().signOut();

        // 2. Xóa Cache RAM của UserSession
        clearCache();

        // 3. Xóa dữ liệu cá nhân trong DataManager (Orders, Cards)
        DataManager.getInstance(context).clearAllUserData();

        // 4. Xóa dữ liệu yêu thích
        FavoriteManager.getInstance(context).clearFavorites();

        // 5. Xóa địa chỉ (address_prefs)
        context.getSharedPreferences("address_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 6. Xóa thông tin giao hàng tạm thời (UserPrefs dùng trong Checkout)
        context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 7. Xóa giỏ hàng (nếu muốn giỏ hàng riêng biệt cho từng user)
        context.getSharedPreferences("saive_cart_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        CartManager.getInstance(context).clearCache();
    }

    public void logout() {
        // Fallback cho các chỗ gọi cũ không truyền context (nếu có)
        FirebaseAuth.getInstance().signOut();
        clearCache();
    }

    // ── Getters ───────────────────────────────────────────────────────

    public boolean isCacheReady() { return cacheReady && isLoggedIn(); }

    public String getUserId() { return userId; }
    public String getEmail() {
        if (email != null) return email;
        return getFirebaseEmail();
    }
    public String getDisplayName() { return displayName != null ? displayName : ""; }
    public String getRole() { return role != null ? role : "customer"; }
    public String getAvatarUrl() { return avatarUrl != null ? avatarUrl : ""; }
    public String getPhone() { return phone != null ? phone : ""; }

    private String getStringSafe(com.google.firebase.database.DataSnapshot snapshot) {
        Object val = snapshot.getValue();
        return (val == null) ? null : String.valueOf(val);
    }

    /**
     * Tải thông tin user từ Firebase DB dựa vào email.
     * Gọi sau khi xác nhận đăng nhập (Firebase Auth) thành công.
     */
    public interface OnUserLoadedCallback {
        void onSuccess();
        void onError(String message);
    }

    public void loadUserFromDatabase(String email, OnUserLoadedCallback callback) {
        com.example.saive.admin.connectors.FirebaseConnector
                .getDatabase()
                .getReference("Users")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            String dbEmail = getStringSafe(child.child("email"));
                            if (dbEmail == null)
                                dbEmail = getStringSafe(child.child("Email"));

                            if (email != null && email.equalsIgnoreCase(dbEmail)) {
                                // Kiểm tra xem user có bị block không
                                Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                                if (isActive == null)
                                    isActive = child.child("isActive").getValue(Boolean.class);

                                if (Boolean.FALSE.equals(isActive)) {
                                    logout();
                                    if (callback != null) callback.onError("Blocked");
                                    return;
                                }

                                String uid = child.getKey();
                                String name = getStringSafe(child.child("fullname"));
                                if (name == null) name = getStringSafe(child.child("DisplayName"));
                                String role = getStringSafe(child.child("role"));
                                if (role == null) role = getStringSafe(child.child("Role"));
                                String avatar = getStringSafe(child.child("avatarUrl"));
                                if (avatar == null) avatar = getStringSafe(child.child("AvatarUrl"));
                                String phone = getStringSafe(child.child("phone"));
                                if (phone == null) phone = getStringSafe(child.child("Phone"));

                                setCache(uid, dbEmail, name, role, avatar, phone);
                                if (callback != null) callback.onSuccess();
                                return;
                            }
                        }
                        // Không tìm thấy trong DB — vẫn cho đăng nhập nhưng cache rỗng
                        setCache(null, email, null, "customer", null, null);
                        if (callback != null) callback.onSuccess();
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        if (callback != null) callback.onError(error.getMessage());
                    }
                });
    }
}
