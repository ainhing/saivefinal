package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.saive.R;
import com.example.saive.admin.ui.MainActivity;
import com.example.saive.base.BaseActivity;

public class LanguageLoadingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Match status bar with cream background
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorCotton));

        setContentView(R.layout.activity_language_loading);

        TextView tvLoading = findViewById(R.id.tvLoadingMessage);
        tvLoading.setText(R.string.updating_language);

        // Giả lập thời gian chờ để "chuyển đổi" xong
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isAdmin = getIntent().getBooleanExtra("is_admin", false);
            Intent intent;
            if (isAdmin) {
                intent = new Intent(LanguageLoadingActivity.this, com.example.saive.admin.ui.MainActivity.class);
            } else {
                intent = new Intent(LanguageLoadingActivity.this, com.example.saive.ui.MainActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
            finish();
        }, 1500); // 1.5 giây để tạo cảm giác chuyên nghiệp
    }
}