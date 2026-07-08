package com.example.saive.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseActivity {

    private static final String LANG_PREFS = "language_prefs";
    private static final String LANG_KEY = "selected_language";

    private ActivityAboutBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
        String lang = prefs.getString(LANG_KEY, "en");
        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupCartBadge();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (binding.searchContainer != null) {
                int paddingHorizontal = (int) (24 * getResources().getDisplayMetrics().density);
                int paddingVertical = (int) (12 * getResources().getDisplayMetrics().density);
                binding.searchContainer.setPadding(paddingHorizontal,
                        systemBars.top + paddingVertical,
                        paddingHorizontal,
                        paddingVertical);
                binding.searchContainer.bringToFront();
            }

            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        applyAnimations();
    }

    private void initViews() {
        com.example.saive.utils.ImageUtils.setSafeImage(binding.ivAboutHero, R.drawable.abouthero);
        com.example.saive.utils.ImageUtils.setSafeImage(binding.ivLinen, R.drawable.banner1);
        com.example.saive.utils.ImageUtils.setSafeImage(binding.ivTencel, R.drawable.atumncollection2);
        com.example.saive.utils.ImageUtils.setSafeImage(binding.ivBamboo, R.drawable.banner2);

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
            });
        }

        if (binding.btnCart != null) {
            binding.btnCart.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(AboutActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (binding.searchContainer != null) {
            binding.searchContainer.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(AboutActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Initial states for animation
        View[] animatedViews = {binding.tvHeroTitle, binding.tvHeroSubtitle, binding.tvOriginYear, binding.tvOriginText, binding.tvMaterialTitle, binding.svMaterials};
        for (View v : animatedViews) {
            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(40f);
            }
        }
    }

    private void applyAnimations() {
        getWindow().getDecorView().post(() -> {
            long duration = 800;
            android.view.animation.Interpolator interpolator = new android.view.animation.PathInterpolator(0.22f, 1f, 0.36f, 1f);

            animateView(binding.tvHeroTitle, 100, duration, interpolator);
            animateView(binding.tvHeroSubtitle, 250, duration, interpolator);
            animateView(binding.tvOriginYear, 400, duration, interpolator);
            animateView(binding.tvOriginText, 550, duration, interpolator);
            animateView(binding.tvMaterialTitle, 700, duration, interpolator);
            animateView(binding.svMaterials, 850, duration, interpolator);
        });
    }

    private void animateView(View view, long delay, long duration, android.view.animation.Interpolator interpolator) {
        if (view == null) return;
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setStartDelay(delay)
                .setInterpolator(interpolator)
                .withLayer()
                .withEndAction(() -> view.setLayerType(View.LAYER_TYPE_NONE, null))
                .start();
    }

    private void setupCartBadge() {
        updateCartBadge();
        com.example.saive.utils.CartManager.getInstance(this).addListener(this::updateCartBadge);
    }

    private void updateCartBadge() {
        if (binding.tvCartBadge == null) return;
        int count = com.example.saive.utils.CartManager.getInstance(this).getItemCount();
        if (count > 0) {
            binding.tvCartBadge.setText(String.valueOf(count));
            binding.tvCartBadge.setVisibility(View.VISIBLE);
            binding.tvCartBadge.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                binding.tvCartBadge.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
            }).start();
        } else {
            binding.tvCartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }
}