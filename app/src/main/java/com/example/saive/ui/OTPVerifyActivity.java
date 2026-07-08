package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;

public class OTPVerifyActivity extends BaseActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private String correctOtp;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        correctOtp = getIntent().getStringExtra("otp");
        email = getIntent().getStringExtra("email");

        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupOtpInputs();

        View btnVerify = findViewById(R.id.btnVerify);
        TextView tvEmailHint = findViewById(R.id.tvEmailHint);
        TextView tvResend = findViewById(R.id.tvResend);

        // Cập nhật hint hiển thị email nhận OTP
        if (email != null && !email.isEmpty()) {
            tvEmailHint.setText(getString(R.string.label_detail_verify_otp_format, email));
        }

        btnVerify.setOnClickListener(v -> {
            String entered = getEnteredOtp();

            if (TextUtils.isEmpty(entered)) {
                showCustomToast(getString(R.string.error_enter_otp));
                return;
            }
            if (entered.length() < 6) {
                showCustomToast(getString(R.string.error_otp_length));
                return;
            }
            if (!entered.equals(correctOtp)) {
                showCustomToast(getString(R.string.error_invalid_otp));
                return;
            }

            Intent intent = new Intent(OTPVerifyActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });

        tvResend.setOnClickListener(v -> {
            String newOtp = String.valueOf((int)(Math.random() * 900000) + 100000);
            correctOtp = newOtp;
            clearOtpFields();
            showCustomToast(getString(R.string.toast_new_otp, newOtp));
        });
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, etOtp5));
        etOtp5.addTextChangedListener(new OtpTextWatcher(etOtp5, etOtp6));
        etOtp6.addTextChangedListener(new OtpTextWatcher(etOtp6, null));

        etOtp2.setOnKeyListener(new OtpKeyListener(etOtp2, etOtp1));
        etOtp3.setOnKeyListener(new OtpKeyListener(etOtp3, etOtp2));
        etOtp4.setOnKeyListener(new OtpKeyListener(etOtp4, etOtp3));
        etOtp5.setOnKeyListener(new OtpKeyListener(etOtp5, etOtp4));
        etOtp6.setOnKeyListener(new OtpKeyListener(etOtp6, etOtp5));
    }

    private String getEnteredOtp() {
        return etOtp1.getText().toString().trim() +
                etOtp2.getText().toString().trim() +
                etOtp3.getText().toString().trim() +
                etOtp4.getText().toString().trim() +
                etOtp5.getText().toString().trim() +
                etOtp6.getText().toString().trim();
    }

    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private static class OtpTextWatcher implements TextWatcher {
        private final View currentView;
        private final View nextView;

        public OtpTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No-op
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }

    private static class OtpKeyListener implements View.OnKeyListener {
        private final EditText currentView;
        private final EditText previousView;

        public OtpKeyListener(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                    && currentView.getText().toString().isEmpty() && previousView != null) {
                previousView.requestFocus();
                return true;
            }
            return false;
        }
    }
}