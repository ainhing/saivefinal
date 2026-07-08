package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        View btnSend = findViewById(R.id.btnSend);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText() != null
                    ? etEmail.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                tilEmail.setError(getString(R.string.login_error_email_req));
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.login_error_email_invalid));
                return;
            }
            tilEmail.setError(null);

            btnSend.setEnabled(false);
            showCustomToast("Please wait / Vui lòng chờ...");
            com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Users")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            btnSend.setEnabled(true);
                            boolean emailExists = false;
                            for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                                String dbEmail = com.example.saive.utils.DataManager.getStringSafe(child.child("email"));
                                if (dbEmail.isEmpty()) dbEmail = com.example.saive.utils.DataManager.getStringSafe(child.child("Email"));
                                if (email.equalsIgnoreCase(dbEmail)) {
                                    emailExists = true;
                                    break;
                                }
                            }
                            if (emailExists) {
                                // Sinh OTP giả lập (demo — không có backend)
                                String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
                                showCustomToast(getString(R.string.toast_new_otp, otp));

                                Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerifyActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("otp", otp);
                                startActivity(intent);
                            } else {
                                tilEmail.setError("Email không tồn tại trong hệ thống."); 
                            }
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            btnSend.setEnabled(true);
                            showCustomToast("Lỗi kiểm tra email");
                        }
                    });
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }
}