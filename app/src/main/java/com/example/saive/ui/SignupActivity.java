package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.databinding.ActivitySignupBinding;
import com.example.saive.utils.ImageUtils;

public class SignupActivity extends BaseActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageUtils.setSafeImage(binding.ivLogo, R.drawable.saive_logo);


        if (getWindow() != null) {
            getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorAuthBg));
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            if (isDarkMode) {
                getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        binding.btnSignup.setOnClickListener(v -> {
            if (validateInput()) {
                performSignup();
            }
        });

        binding.tvTermsLink.setOnClickListener(v -> showTermsPopup());

        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private boolean validateInput() {
        String name            = binding.etName.getText().toString().trim();
        String email           = binding.etEmail.getText().toString().trim();
        String password        = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String phone           = binding.etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError(getString(R.string.error_name_required));
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError(getString(R.string.error_email_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.error_invalid_email));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError(getString(R.string.error_password_required));
            return false;
        }
        if (password.length() < 6) {
            binding.etPassword.setError(getString(R.string.error_password_short));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError(getString(R.string.error_phone_required));
            return false;
        }
        if (!binding.cbTerms.isChecked()) {
            showCustomToast(getString(R.string.error_agree_terms));
            return false;
        }
        return true;
    }

    private void showTermsPopup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        com.example.saive.databinding.DialogTermsBinding dialogBinding =
                com.example.saive.databinding.DialogTermsBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialogBinding.btnCloseTerms.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setLoading(boolean loading) {
        binding.btnSignup.setEnabled(!loading);
        binding.btnSignup.setAlpha(loading ? 0.6f : 1f);
    }

    // ──────────────────────────────────────────────────────────────────
    // ĐĂNG KÝ QUA FIREBASE AUTH + REALTIME DATABASE
    // ──────────────────────────────────────────────────────────────────
    private void performSignup() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String name     = binding.etName.getText().toString().trim();
        String phone    = binding.etPhone.getText().toString().trim();

        setLoading(true);
        android.util.Log.d("SAIVE_DEBUG", "Bắt đầu đăng ký: " + email);

        com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    android.util.Log.d("SAIVE_DEBUG", "Auth thành công, UID: " + userId);
                    
                    // Lấy reference từ DatabaseURL chính xác (asia-southeast1)
                    com.google.firebase.database.DatabaseReference usersRef = 
                        com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Users");

                    // ── LOGIC TẠO ID TỰ TĂNG (USRxxx) ──
                    usersRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            int maxNum = 0;
                            for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                                String key = child.getKey();
                                if (key != null && key.startsWith("USR")) {
                                    try {
                                        int num = Integer.parseInt(key.substring(3));
                                        if (num > maxNum) maxNum = num;
                                    } catch (NumberFormatException e) {
                                        // Bỏ qua các ID không đúng định dạng
                                    }
                                }
                            }
                            
                            String nextId = String.format("USR%03d", maxNum + 1);
                            
                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                            userMap.put("UserId", nextId); // Vẫn lưu UID auth để đối soát nếu cần
                            userMap.put("AuthUid", userId);
                            userMap.put("Email", email);
                            userMap.put("Password", password); 
                            userMap.put("DisplayName", name);
                            userMap.put("Phone", phone);
                            userMap.put("Role", "user");
                            userMap.put("IsActive", true);
                            userMap.put("CreatedAt", com.google.firebase.database.ServerValue.TIMESTAMP);

                            usersRef.child(nextId).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    setLoading(false);
                                    if (dbTask.isSuccessful()) {
                                        android.util.Log.d("SAIVE_DEBUG", "Lưu Database với ID " + nextId + " thành công!");
                                        showCustomToast(getString(R.string.toast_signup_success));
                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String dbErr = dbTask.getException() != null ? dbTask.getException().getMessage() : "Lỗi không xác định";
                                        showCustomToast("Lỗi Database: " + dbErr);
                                    }
                                });
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            setLoading(false);
                            showCustomToast("Lỗi truy vấn: " + error.getMessage());
                        }
                    });
                } else {
                    setLoading(false);
                    String authErr = task.getException() != null ? task.getException().getMessage() : "Không thể tạo tài khoản";
                    android.util.Log.e("SAIVE_DEBUG", "Lỗi Auth: " + authErr);
                    showCustomToast("Đăng ký thất bại: " + authErr);
                }
            });
    }
}