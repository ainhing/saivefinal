package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword     = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etCurrentPassword  = findViewById(R.id.etCurrentPassword);
        etNewPassword      = findViewById(R.id.etNewPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnChangePassword = findViewById(R.id.btnChangePassword);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnBack.setOnClickListener(v -> finish());
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        btnChangePassword.setOnClickListener(v -> {
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String currentPass  = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
            String newPass      = etNewPassword.getText()     != null ? etNewPassword.getText().toString().trim()     : "";
            String confirmPass  = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim()  : "";

            if (TextUtils.isEmpty(currentPass)) {
                tilCurrentPassword.setError(getString(R.string.error_current_password_required));
                return;
            }
            if (TextUtils.isEmpty(newPass)) {
                tilNewPassword.setError(getString(R.string.error_new_password_required));
                return;
            }
            if (newPass.length() < 6) {
                tilNewPassword.setError(getString(R.string.error_new_password_min));
                return;
            }
            if (newPass.equals(currentPass)) {
                tilNewPassword.setError(getString(R.string.error_new_password_same));
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

            // Đổi mật khẩu qua Firebase Auth (reauthenticate + updatePassword)
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.getEmail() == null) {
                showCustomToast("Vui lòng đăng nhập lại để đổi mật khẩu.");
                return;
            }

            btnChangePassword.setEnabled(false);

            // Xác thực lại bằng mật khẩu hiện tại
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
            user.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (!reAuthTask.isSuccessful()) {
                            btnChangePassword.setEnabled(true);
                            tilCurrentPassword.setError(getString(R.string.error_current_password_incorrect));
                            return;
                        }

                        // Cập nhật mật khẩu mới
                        user.updatePassword(newPass)
                                .addOnCompleteListener(updateTask -> {
                                    btnChangePassword.setEnabled(true);
                                    if (updateTask.isSuccessful()) {
                                        // Cũng cập nhật password trong Firebase DB (để DB sync)
                                        String userId = com.example.saive.utils.UserSession.getInstance().getUserId();
                                        if (userId != null && !userId.isEmpty()) {
                                            com.example.saive.admin.connectors.FirebaseConnector
                                                    .getDatabase().getReference("Users")
                                                    .child(userId).child("password").setValue(newPass);
                                        }
                                        showCustomToast(getString(R.string.toast_password_changed));
                                        finish();
                                    } else {
                                        String msg = updateTask.getException() != null
                                                ? updateTask.getException().getMessage() : "Lỗi không xác định";
                                        showCustomToast("Đổi mật khẩu thất bại: " + msg);
                                    }
                                });
                    });
        });
    }
}