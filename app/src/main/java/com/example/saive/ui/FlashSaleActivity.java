package com.example.saive.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.FlashSaleGridAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.utils.CartManager;

import com.example.saive.utils.DataManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FlashSaleActivity extends BaseActivity implements DataManager.OnProductChangeListener {

    private TextView tvHour, tvMinute, tvSecond;
    private RecyclerView rvProducts;
    private CountDownTimer countDownTimer;

    private FlashSaleGridAdapter adapter;
    private List<Product> allProducts;
    private TextView tvCartBadge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);

        initViews();
        setupTimer();
        setupProducts();

        DataManager.getInstance(this).addProductListener(this);
        setupCartBadge();
        setupNavigation();
        applyWindowInsets();
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (getWindow() != null) {
            boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) 
                    == Configuration.UI_MODE_NIGHT_YES;
            
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorCotton));
            
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (isDarkMode) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.flashSaleRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            View topBar = findViewById(R.id.topBar);
            if (topBar != null) {
                int paddingHorizontal = (int) (24 * getResources().getDisplayMetrics().density);
                int paddingVertical = (int) (12 * getResources().getDisplayMetrics().density);
                topBar.setPadding(paddingHorizontal, 
                        systemBars.top + paddingVertical,
                        paddingHorizontal, 
                        paddingVertical);
            }

            View bottomNav = findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });
    }

    private void setupNavigation() {
        View bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        View navFavorite = findViewById(R.id.navFavorite);
        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> navigateToMain("SHOW_FAVORITES"));
        }

        View navWardrobe = findViewById(R.id.navWardrobe);
        if (navWardrobe != null) {
            navWardrobe.setOnClickListener(v -> navigateToMain("SHOW_WARDROBE"));
        }

        View navNotify = findViewById(R.id.navNotify);
        if (navNotify != null) {
            navNotify.setOnClickListener(v -> navigateToMain("SHOW_NOTIFICATIONS"));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View centerActionButton = findViewById(R.id.centerActionButton);
        if (centerActionButton != null) {
            centerActionButton.setOnClickListener(v -> navigateToMain("SHOW_HOME"));
        }
    }

    private void setupCartBadge() {
        tvCartBadge = findViewById(R.id.tvCartBadge);
        updateCartBadge();
        CartManager.getInstance(this).addListener(this::updateCartBadge);
    }

    private void updateCartBadge() {
        if (tvCartBadge == null) return;
        int count = CartManager.getInstance(this).getItemCount();
        if (count > 0) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        tvHour = findViewById(R.id.tvHour);
        tvMinute = findViewById(R.id.tvMinute);
        tvSecond = findViewById(R.id.tvSecond);
        rvProducts = findViewById(R.id.rvFlashSaleProducts);

        View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
            });
        }

        View btnCart = findViewById(R.id.btnCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }



    private void setupTimer() {
        long endTime = DataManager.getInstance(this).getFlashSaleEndTime();
        long currentTime = System.currentTimeMillis();

        if (endTime > currentTime) {
            long diff = endTime - currentTime;
            countDownTimer = new CountDownTimer(diff, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimerUI(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    if (tvHour != null) tvHour.setText(R.string.timer_default);
                    if (tvMinute != null) tvMinute.setText(R.string.timer_default);
                    if (tvSecond != null) tvSecond.setText(R.string.timer_default);
                }
            }.start();
        } else {
            if (tvHour != null) tvHour.setText(R.string.timer_default);
            if (tvMinute != null) tvMinute.setText(R.string.timer_default);
            if (tvSecond != null) tvSecond.setText(R.string.timer_default);
        }
    }

    private void updateTimerUI(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (tvHour != null) tvHour.setText(String.format(Locale.getDefault(), "%02d", hours));
        if (tvMinute != null) tvMinute.setText(String.format(Locale.getDefault(), "%02d", minutes));
        if (tvSecond != null) tvSecond.setText(String.format(Locale.getDefault(), "%02d", seconds));
    }

    private void setupProducts() {
        allProducts = DataManager.getInstance(this).getFlashSaleProducts();
        
        // Removed hardcoded fallback to ensure consistency with MainActivity
        if (allProducts.isEmpty()) {
            // In case FlashSaleActivity is opened directly without MainActivity initializing data
            // but ideally MainActivity or a Splash/Data initialization should handle this.
            // For safety, we can show an empty state or try to generate here if needed.
        }

        adapter = new FlashSaleGridAdapter(allProducts);
        // Do not set color here, use default dark text for light item background

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);
    }

    @Override
    public void onProductsChanged() {
        runOnUiThread(() -> {
            allProducts.clear();
            allProducts.addAll(DataManager.getInstance(this).getFlashSaleProducts());
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.getInstance(this).removeProductListener(this);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}