package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;


import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends BaseActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextInputLayout tilNewPassword, tilConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        View btnReset = findViewById(R.id.btnReset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnReset.setOnClickListener(v -> {
            String newPass = etNewPassword.getText() != null
                    ? etNewPassword.getText().toString().trim() : "";
            String confirmPass = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString().trim() : "";

            // Reset errors trước
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            if (TextUtils.isEmpty(newPass)) {
                tilNewPassword.setError(getString(R.string.error_new_password_required));
                return;
            }
            if (newPass.length() < 6) {
                tilNewPassword.setError(getString(R.string.error_new_password_min));
                return;
            }
            if (TextUtils.isEmpty(confirmPass)) {
                tilConfirmPassword.setError(getString(R.string.error_confirm_password_required));
                return;
            }
            if (!newPass.equals(confirmPass)) {
                tilConfirmPassword.setError(getString(R.string.error_passwords_not_match));
                return;
            }

            String email = getIntent().getStringExtra("email");
            if (email == null || email.isEmpty()) {
                showCustomToast("Lỗi: Không tìm thấy email.");
                return;
            }

            btnReset.setEnabled(false);
            showCustomToast("Updating password...");

            com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Users")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            btnReset.setEnabled(true);
                            boolean updated = false;
                            for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                                String dbEmail = com.example.saive.utils.DataManager.getStringSafe(child.child("email"));
                                if (dbEmail.isEmpty()) dbEmail = com.example.saive.utils.DataManager.getStringSafe(child.child("Email"));
                                if (email.equalsIgnoreCase(dbEmail)) {
                                    String oldPass = com.example.saive.utils.DataManager.getStringSafe(child.child("password"));
                                    if (oldPass.isEmpty()) oldPass = com.example.saive.utils.DataManager.getStringSafe(child.child("Password"));

                                    child.getRef().child("password").setValue(newPass);
                                    if (child.hasChild("Password")) {
                                        child.getRef().child("Password").setValue(newPass);
                                    }

                                    if (oldPass != null && !oldPass.isEmpty()) {
                                        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
                                        auth.signInWithEmailAndPassword(email, oldPass)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                                                        task.getResult().getUser().updatePassword(newPass);
                                                    }
                                                });
                                    }
                                    updated = true;
                                    break;
                                }
                            }
                            if (updated) {
                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit().putString("user_password", newPass).apply();

                                showCustomToast(getString(R.string.toast_pwd_reset_success));

                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                showCustomToast("Không tìm thấy user để cập nhật mật khẩu.");
                            }
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            btnReset.setEnabled(true);
                            showCustomToast("Lỗi kết nối cơ sở dữ liệu");
                        }
                    });
        });

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}