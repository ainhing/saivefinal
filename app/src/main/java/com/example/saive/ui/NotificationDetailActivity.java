package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saive.R;
import com.example.saive.utils.ImageUtils;

public class NotificationDetailActivity extends AppCompatActivity {

    private ImageView ivDetailImage, ivSub1, ivSub2, ivSub3;
    private TextView tvDetailTitle, tvDetailTime, tvDetailContent, tvActionText;
    private View btnBack, cvAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        initViews();
        handleIntent();
        setupListeners();
    }

    private void initViews() {
        ivDetailImage = findViewById(R.id.ivDetailImage);
        ivSub1 = findViewById(R.id.ivSub1);
        ivSub2 = findViewById(R.id.ivSub2);
        ivSub3 = findViewById(R.id.ivSub3);
        
        ImageUtils.setSafeImage(ivSub1, R.drawable.atumncollection2);
        ImageUtils.setSafeImage(ivSub2, R.drawable.model1);
        ImageUtils.setSafeImage(ivSub3, R.drawable.banner2);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        tvActionText = findViewById(R.id.tvActionText);
        btnBack = findViewById(R.id.btnBack);
        cvAction = findViewById(R.id.cvAction);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String time = intent.getStringExtra("time");
            String desc = intent.getStringExtra("desc");
            String action = intent.getStringExtra("action");
            String typeStr = intent.getStringExtra("type");
            int iconRes = intent.getIntExtra("icon", R.drawable.banner1);

            if (title != null) tvDetailTitle.setText(title);
            if (time != null) {
                tvDetailTime.setText(getString(R.string.posted_time, time));
            }
            if (desc != null) {
                String fullContent = desc + "\n\n" + getString(R.string.notify_detail_description_placeholder);
                tvDetailContent.setText(fullContent);
            }
            if (action != null) tvActionText.setText(action.toUpperCase(java.util.Locale.getDefault()));
            
            // Set image based on notification type
            if (typeStr != null) {
                com.example.saive.models.Notification.Type type = com.example.saive.models.Notification.Type.valueOf(typeStr);
                switch (type) {
                    case DROP:
                        ImageUtils.setSafeImage(ivDetailImage, R.drawable.atumncollection1);
                        break;
                    case ORDER:
                        ImageUtils.setSafeImage(ivDetailImage, R.drawable.atumncollection2);
                        break;
                    case REMINDER:
                        ImageUtils.setSafeImage(ivDetailImage, R.drawable.banner3);
                        break;
                    default:
                        ImageUtils.setSafeImage(ivDetailImage, R.drawable.banner1);
                        break;
                }
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        cvAction.setOnClickListener(v -> {
            String typeStr = getIntent().getStringExtra("type");
            Intent intent;
            
            if (typeStr != null) {
                com.example.saive.models.Notification.Type type = com.example.saive.models.Notification.Type.valueOf(typeStr);
                switch (type) {
                    case REMINDER:
                        intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        break;
                    case CAPSULE:
                        intent = new Intent(this, CartActivity.class);
                        intent.putExtra("SHOW_WARDROBE", true);
                        break;
                    case ORDER:
                        intent = new Intent(this, MyOrdersActivity.class);
                        break;
                    default:
                        intent = new Intent(this, MainActivity.class);
                        intent.putExtra("SHOW_HOME", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        break;
                }
            } else {
                intent = new Intent(this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
