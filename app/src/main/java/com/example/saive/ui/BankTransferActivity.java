package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.saive.R;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class BankTransferActivity extends AppCompatActivity {

    private TextView tvExpiry, tvRegenerate;
    private ImageView ivQrCode;
    private MaterialCardView tvConfirm;
    private CountDownTimer countDownTimer;
    private static final long QR_DURATION_MS = 4 * 60 * 1000 + 57 * 1000; // 4:57

    private boolean isVietQR = false;
    private String orderId = "";
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bank_transfer);

        loadIntentData();
        setupWindowInsets();
        initViews();
        loadQrCode();
        startCountdown();
        setupListeners();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            isVietQR = intent.getBooleanExtra("isVietQR", false);
            orderId = intent.getStringExtra("order_id");
            totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        }
    }

    private void initViews() {
        tvExpiry = findViewById(R.id.tvExpiry);
        tvConfirm = findViewById(R.id.tvConfirm);
        tvRegenerate = findViewById(R.id.tvRegenerate);
        ivQrCode = findViewById(R.id.ivQrCode);

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null && isVietQR) {
            tvTitle.setText(R.string.payment_vietqr);
        }
    }

    private void loadQrCode() {
        if (isVietQR) {
            // VietQR API: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<ACCOUNT_NAME>
            // Replace with your actual bank details
            String bankId = "vcb"; // Vietcombank
            String accountNo = "1023456789";
            String accountName = "CONG TY SAIVE";
            String description = "Thanh toan don hang " + (orderId != null ? orderId : "");
            
            String qrUrl = String.format(Locale.US, 
                "https://img.vietqr.io/image/%s-%s-compact.png?amount=%.0f&addInfo=%s&accountName=%s",
                bankId, accountNo, totalAmount, description.replace(" ", "%20"), accountName.replace(" ", "%20"));
            
            Glide.with(this)
                 .load(qrUrl)
                 .placeholder(R.drawable.ic_qr_code)
                 .into(ivQrCode);
        } else {
            // Static bank icon or pre-generated QR for normal bank transfer
            ivQrCode.setImageResource(R.drawable.ic_qr_code);
        }
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(QR_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvExpiry.setText(getString(R.string.desc_qr_code_expired_format, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvExpiry.setText(getString(R.string.desc_qr_code_expired));
            }
        }.start();
    }

    private void setupListeners() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Regenerate QR
        tvRegenerate.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            loadQrCode();
            startCountdown();
        });

        // Confirm Payment
        tvConfirm.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            Intent intent = new Intent(BankTransferActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
            finish();
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
