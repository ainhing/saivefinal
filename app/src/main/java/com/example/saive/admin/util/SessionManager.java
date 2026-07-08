package com.example.saive.admin.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý session đăng nhập của Admin sử dụng SharedPreferences thông thường.
 * Loại bỏ EncryptedSharedPreferences để tránh lỗi Keystore / VerifyError / NoClassDefFoundError
 * trên một số dòng máy ảo (Emulator) và thiết bị Android cũ, giúp ứng dụng không bị crash khi khởi động.
 */
public class SessionManager {
    private static final String PREF_NAME = "saive_admin_session_plain";
    private static final String KEY_USER_ID = "admin_user_id";
    private static final String KEY_DISPLAY_NAME = "admin_display_name";
    private static final String KEY_ROLE = "admin_role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createLoginSession(String userId, String displayName, String role) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getDisplayName() {
        return sharedPreferences.getString(KEY_DISPLAY_NAME, "Admin");
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}