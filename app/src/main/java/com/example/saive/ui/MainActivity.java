package com.example.saive.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.saive.adapters.BottomSheetOptionAdapter;
import java.util.Arrays;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.saive.models.Notification;
import com.example.saive.adapters.NotificationAdapter;
import com.example.saive.R;
import com.example.saive.adapters.BannerAdapter;
import com.example.saive.adapters.CategoryAdapter;
import com.example.saive.adapters.EditorialCardAdapter;
import com.example.saive.adapters.FavoriteAdapter;
import com.example.saive.adapters.FlashProductAdapter;
import com.example.saive.adapters.ProductAdapter;
import com.example.saive.adapters.ProductGridAdapter;
import com.example.saive.models.Category;
import com.example.saive.models.EditorialCard;
import com.example.saive.models.Product;
import com.example.saive.base.BaseActivity;
import com.example.saive.utils.CartManager;
import com.example.saive.utils.FavoriteManager;
import java.util.ArrayList;
import java.util.List;

import com.example.saive.utils.DataManager;
import com.example.saive.utils.ImageUtils;
import com.google.firebase.messaging.FirebaseMessaging;

@android.annotation.SuppressLint("NotifyDataSetChanged")
public class MainActivity extends BaseActivity implements DataManager.OnProductChangeListener {

    private ViewPager2 viewPager, bannerViewPager, vpWardrobeBanner;
    private LinearLayout dotIndicatorWardrobe;
    private RecyclerView rvFlashSale, rvNotifications, rvCategories, rvWardrobe;
    private List<Product> productList, flashProductList, wardrobeProductList, fullWardrobeList;
    private List<Category> categoryList;
    private ProductGridAdapter wardrobeAdapter;
    private CategoryAdapter wardrobeCategoryAdapter;
    private List<Integer> bannerList;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private View notificationBadge;
    private View homeScroll, notificationsContainer, wardrobeContainer, favoritesContainer, flashSaleContainer;
    private View emptyStateWardrobe;
    private RecyclerView rvFavorites;
    private TextView tvFavoritesCount;
    private View emptyStateFavorites;
    private FavoriteAdapter favoriteAdapter;
    private List<Product> favoritesList;
    private TextView tvCartBadge;
    private TextView tvHomeHour, tvHomeMinute, tvHomeSecond;
    private CountDownTimer homeCountDownTimer;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String LANG_PREFS = "language_prefs";
    private static final String LANG_KEY = "selected_language";
    private String currentCategory = "All";
    private String currentSortCriteria = null;
    private String currentNotificationSortCriteria = null;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(Intent intent) {
        if (intent == null)
            return;

        boolean hasSectionExtra = intent.getBooleanExtra("SHOW_NOTIFICATIONS", false) ||
                intent.getBooleanExtra("SHOW_WARDROBE", false) ||
                intent.getBooleanExtra("SHOW_FAVORITES", false) ||
                intent.getBooleanExtra("SHOW_HOME", false);

        // Add a small delay to ensure UI is ready and transitions are smoother
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (hasSectionExtra) {
                // Đảm bảo BottomNav và Center Button luôn hiển thị khi điều hướng từ Activity
                // khác về
                View bottomNav = findViewById(R.id.bottomNav);
                if (bottomNav != null) {
                    bottomNav.setAlpha(1f);
                    bottomNav.setTranslationY(0f);
                    bottomNav.setVisibility(View.VISIBLE);
                }
                View centerFab = findViewById(R.id.centerActionButton);
                if (centerFab != null) {
                    centerFab.setVisibility(View.VISIBLE);
                    if (centerFab.getScaleX() <= 0.1f) {
                        centerFab.setScaleX(1f);
                        centerFab.setScaleY(1f);
                    }
                }
            }

            if (intent.getBooleanExtra("SHOW_NOTIFICATIONS", false)) {
                if (notificationsContainer != null) {
                    showView(notificationsContainer);
                    View navNotify = findViewById(R.id.navNotify);
                    if (navNotify != null)
                        animateNavIcon(navNotify);
                }
            } else if (intent.getBooleanExtra("SHOW_WARDROBE", false)) {
                if (wardrobeContainer != null) {
                    showView(wardrobeContainer);
                    View navWardrobe = findViewById(R.id.navWardrobe);
                    if (navWardrobe != null)
                        animateNavIcon(navWardrobe);
                }
            } else if (intent.getBooleanExtra("SHOW_FAVORITES", false)) {
                if (favoritesContainer != null) {
                    showView(favoritesContainer);
                    View navFavorite = findViewById(R.id.navFavorite);
                    if (navFavorite != null)
                        animateNavIcon(navFavorite);
                }
            } else if (intent.getBooleanExtra("SHOW_HOME", false)) {
                showView(homeScroll);
                View centerFab = findViewById(R.id.centerActionButton);
                if (centerFab != null)
                    animateNavIcon(centerFab);
            }
        }, 150);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("dark_mode", false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                isDark
                        ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View rootLayout = findViewById(R.id.rootLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Push search container down by status bar height
            View searchBar = findViewById(R.id.searchBarWrapper);
            if (searchBar != null) {
                int paddingHorizontal = (int) (24 * getResources().getDisplayMetrics().density);
                int paddingVertical = (int) (12 * getResources().getDisplayMetrics().density);
                searchBar.setPadding(paddingHorizontal,
                        systemBars.top + paddingVertical,
                        paddingHorizontal,
                        paddingVertical);

                // Ensure search bar is visible on top of everything
                searchBar.bringToFront();
            }

            // Push bottom nav up by navigation bar height
            View bottomNav = findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }

            return insets;
        });

        initViews();
        setupBannerViewPager();
        setupWardrobe(); // Khởi tạo với data local trước
        loadProductsFromServer(); // Sau đó fetch từ MongoDB (async)
        setupFlashSale();
        setupWardrobeCategories();
        setupWardrobeBanners();
        setupViewPager();
        setupNotifications();
        setupFavorites();
        setupNavigation();
        setupCartBadge();
        setupHomeTimer();
        setupEditorialImages();

        DataManager.getInstance(this).addProductListener(this);

        checkIntent(getIntent());
        requestNotificationPermission();

        // Bắt đầu hiệu ứng vào cho UI chính
        startEntryAnimations();
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (getWindow() != null) {
            boolean isDarkMode = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

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

    private void setupCartBadge() {
        tvCartBadge = findViewById(R.id.tvCartBadge);
        updateCartBadge();
        CartManager.getInstance(this).addListener(this::updateCartBadge);
    }

    private void updateCartBadge() {
        if (tvCartBadge == null)
            return;

        // Hide cart badge for guests to match ProfileActivity logic
        if (!com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        int count = CartManager.getInstance(this).getItemCount();
        if (count > 0) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);

            // Hiệu ứng nảy nhẹ khi số lượng thay đổi
            tvCartBadge.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                tvCartBadge.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
            }).start();
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void startEntryAnimations() {
        View searchBar = findViewById(R.id.searchBarWrapper);
        View mainContent = findViewById(R.id.homeScroll);
        View bottomNav = findViewById(R.id.bottomNav);
        View centerFab = findViewById(R.id.centerActionButton);

        // 1. Thiết lập trạng thái ẩn ban đầu
        if (searchBar != null) {
            searchBar.setAlpha(0f);
            searchBar.setTranslationY(-100f);
        }
        if (mainContent != null) {
            mainContent.setAlpha(0f);
            mainContent.setTranslationY(200f);
        }
        if (bottomNav != null) {
            bottomNav.setAlpha(0f);
            bottomNav.setTranslationY(100f);
        }
        if (centerFab != null) {
            centerFab.setScaleX(0f);
            centerFab.setScaleY(0f);
        }

        // 2. Chạy chuỗi hiệu ứng (Staggered Animations)

        // Thanh Search trượt xuống
        if (searchBar != null) {
            searchBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .setStartDelay(300)
                    .start();
        }

        // Nội dung chính trượt lên chậm rãi
        if (mainContent != null) {
            mainContent.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(1200)
                    .setInterpolator(new DecelerateInterpolator())
                    .setStartDelay(500)
                    .start();
        }

        // Thanh điều hướng và nút trung tâm
        if (bottomNav != null) {
            bottomNav.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(800)
                    .start();
        }

        if (centerFab != null) {
            centerFab.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(600)
                    .setInterpolator(new OvershootInterpolator())
                    .setStartDelay(1100)
                    .withEndAction(() -> {
                        centerFab.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    })
                    .start();
        }
    }

    private void setupWardrobeBanners() {
        vpWardrobeBanner = findViewById(R.id.vpWardrobeBanner);
        dotIndicatorWardrobe = findViewById(R.id.dotIndicatorWardrobe);

        if (vpWardrobeBanner == null || dotIndicatorWardrobe == null)
            return;

        List<com.example.saive.models.WardrobeBanner> banners = new ArrayList<>();
        // Sử dụng ảnh thay thế hoặc kiểm tra dung lượng
        banners.add(new com.example.saive.models.WardrobeBanner(
                getString(R.string.label_autumn_winter),
                getString(R.string.label_new_collection),
                getString(R.string.label_shop_now),
                R.drawable.atumncollection1));
        banners.add(new com.example.saive.models.WardrobeBanner(
                "CURATED STYLE",
                "ESSENTIALS",
                "EXPLORE",
                R.drawable.atumncollection2));
        banners.add(new com.example.saive.models.WardrobeBanner(
                "LIMITED DROP",
                "URBAN ARCHIVE",
                "DISCOVER",
                R.drawable.banner2));

        com.example.saive.adapters.WardrobeBannerAdapter adapter = new com.example.saive.adapters.WardrobeBannerAdapter(
                banners, banner -> {
                    // ... giữ nguyên logic
                    Intent intent;
                    if (banner.getTitle().equals("ESSENTIALS")) {
                        intent = new Intent(MainActivity.this, CollectionDetailActivity.class);
                        intent.putExtra("COLLECTION_TITLE", "ESSENTIALS");
                    } else if (banner.getTitle().equals("URBAN ARCHIVE")) {
                        intent = new Intent(MainActivity.this, CollectionDetailActivity.class);
                        intent.putExtra("COLLECTION_TITLE", "URBAN ARCHIVE");
                    } else {
                        intent = new Intent(MainActivity.this, CollectionsListActivity.class);
                    }
                    startActivity(intent);
                });
        vpWardrobeBanner.setAdapter(adapter);
        setupDotIndicator(banners.size());

        vpWardrobeBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
            }
        });
    }

    // Thêm hàm nạp ảnh editorial an toàn
    private void setupEditorialImages() {
        ImageView ivEditorialPrimary = findViewById(R.id.ivEditorialPrimary);
        ImageView ivEditorialSecondary = findViewById(R.id.ivEditorialSecondary);
        ImageView ivMaterialStory = findViewById(R.id.ivMaterialStory);
        ImageView ivCapsule1 = findViewById(R.id.ivCapsule1);
        ImageView ivCapsule2 = findViewById(R.id.ivCapsule2);

        if (ivEditorialPrimary != null)
            ImageUtils.setSafeImage(ivEditorialPrimary, R.drawable.model2);
        if (ivEditorialSecondary != null)
            ImageUtils.setSafeImage(ivEditorialSecondary, R.drawable.model1);
        if (ivMaterialStory != null)
            ImageUtils.setSafeImage(ivMaterialStory, R.drawable.banner2);
        if (ivCapsule1 != null)
            ImageUtils.setSafeImage(ivCapsule1, R.drawable.model1);
        if (ivCapsule2 != null)
            ImageUtils.setSafeImage(ivCapsule2, R.drawable.banner3);
    }

    // Thêm hàm helper để load ảnh an toàn
    private void setSafeImage(ImageView imageView, int resId) {
        ImageUtils.setSafeImage(imageView, resId);
    }

    private void setupDotIndicator(int count) {
        dotIndicatorWardrobe.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density));
            params.setMargins(
                    (int) (4 * getResources().getDisplayMetrics().density),
                    0,
                    (int) (4 * getResources().getDisplayMetrics().density),
                    0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.dot_indicator);
            dot.setSelected(i == 0);
            dotIndicatorWardrobe.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotIndicatorWardrobe.getChildCount(); i++) {
            dotIndicatorWardrobe.getChildAt(i).setSelected(i == position);
        }
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navNotify = findViewById(R.id.navNotify);
        View navWardrobe = findViewById(R.id.navWardrobe);
        View navFavorite = findViewById(R.id.navFavorite);
        View navProfile = findViewById(R.id.navProfile);
        View centerActionButton = findViewById(R.id.centerActionButton);
        View searchContainer = findViewById(R.id.searchContainer);
        View btnCart = findViewById(R.id.btnCart);

        // Khởi tạo trạng thái ban đầu (Home đang active)
        updateNavHighlight(centerActionButton);

        if (searchContainer != null) {
            searchContainer.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                showView(homeScroll);
            });
        }

        if (navWardrobe != null) {
            navWardrobe.setOnClickListener(v -> {
                showView(wardrobeContainer);
            });
        }

        if (navNotify != null) {
            navNotify.setOnClickListener(v -> {
                showView(notificationsContainer);
            });
        }

        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                showView(favoritesContainer);
            });
        }

        // The Monochrome Series clicks
        View cardPrimary = findViewById(R.id.cardEditorialPrimary);
        View cardSecondary = findViewById(R.id.cardEditorialSecondary);
        View btnExplore = findViewById(R.id.btnExploreLookbook);

        View.OnClickListener monochromeClickListener = v -> {
            Intent intent = new Intent(MainActivity.this, CollectionsListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        };

        if (cardPrimary != null)
            cardPrimary.setOnClickListener(monochromeClickListener);
        if (cardSecondary != null)
            cardSecondary.setOnClickListener(monochromeClickListener);
        if (btnExplore != null)
            btnExplore.setOnClickListener(monochromeClickListener);

        View tvMaterialTitle = findViewById(R.id.tvMaterialTitle);
        View ivMaterialStory = findViewById(R.id.ivMaterialStory);
        if (ivMaterialStory != null) {
            ivMaterialStory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", getString(R.string.material_story_title));
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                updateNavHighlight(v);
                navigateToProfile();
            });
        }

        // Sort button
        View btnSortWardrobe = findViewById(R.id.btnSortWardrobe);
        if (btnSortWardrobe != null) {
            btnSortWardrobe.setOnClickListener(v -> {
                showSortPopup(v);
            });
        }

        // Notification Settings button
        View btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        if (btnNotificationSettings != null) {
            btnNotificationSettings.setOnClickListener(v -> {
                showNotificationSortPopup(v);
            });
        }

        // Home button (center) also toggles home view
        if (centerActionButton != null) {
            centerActionButton.setOnClickListener(v -> {
                showView(homeScroll);
            });
        }

        View tvViewFullCuration = findViewById(R.id.tvViewFullCuration);
        if (tvViewFullCuration != null) {
            tvViewFullCuration.setOnClickListener(v -> {
                showView(wardrobeContainer);
            });
        }

        // Home Navigation Links
        View tvNavShop = findViewById(R.id.tvNavShop);
        View tvNavArchive = findViewById(R.id.tvNavArchive);
        View tvNavAbout = findViewById(R.id.tvNavAbout);

        if (tvNavShop != null) {
            tvNavShop.setOnClickListener(v -> {
                showView(wardrobeContainer);
                animateNavIcon(navWardrobe);
            });
        }

        if (tvNavArchive != null) {
            tvNavArchive.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CollectionsListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (tvNavAbout != null) {
            tvNavAbout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // New Collection Banner click
        View bannerNewCollection = findViewById(R.id.wardrobeBannerContainer);
        if (bannerNewCollection != null) {
            bannerNewCollection.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CollectionsListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // Flash Sale Container Click
        if (flashSaleContainer != null) {
            flashSaleContainer.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", "FLASH SALE");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void showView(View toShow) {
        if (toShow == null || toShow.getVisibility() == View.VISIBLE)
            return;

        View[] views = { homeScroll, notificationsContainer, wardrobeContainer, favoritesContainer };
        View searchBar = findViewById(R.id.searchBarWrapper);

        // Xác định tab nào cần highlight
        View activeNav = null;
        if (toShow == homeScroll)
            activeNav = findViewById(R.id.centerActionButton);
        else if (toShow == wardrobeContainer)
            activeNav = findViewById(R.id.navWardrobe);
        else if (toShow == notificationsContainer)
            activeNav = findViewById(R.id.navNotify);
        else if (toShow == favoritesContainer)
            activeNav = findViewById(R.id.navFavorite);

        updateNavHighlight(activeNav);

        for (View v : views) {
            if (v == null)
                continue;
            if (v == toShow) {
                // Reset and prepare toShow
                v.setAlpha(0f);
                v.setTranslationY(40f); // Subtle slide up
                v.setVisibility(View.VISIBLE);

                // Animate toShow: Smooth slide up and fade in
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(600)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .withEndAction(() -> {
                            // Ensure search bar stays on top after container switch
                            if (searchBar != null) {
                                searchBar.bringToFront();
                            }
                        })
                        .start();
            } else if (v.getVisibility() == View.VISIBLE) {
                // Animate toHide: Subtle fade out
                v.animate()
                        .alpha(0f)
                        .setDuration(400)
                        .withEndAction(() -> {
                            v.setVisibility(View.GONE);
                        })
                        .start();
            }
        }
    }

    private void setupFavorites() {
        if (rvFavorites != null) {
            rvFavorites.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

            FavoriteManager favoriteManager = FavoriteManager.getInstance(this);
            favoritesList = favoriteManager.getFavoriteItems();

            favoriteAdapter = new FavoriteAdapter(favoritesList, position -> {
                Product product = favoritesList.get(position);
                favoriteManager.removeFavorite(product);
                // The listener will trigger update if we add it, but for now we can update
                // manually or rely on manager
            });

            favoriteManager.addListener(() -> {
                favoritesList.clear();
                favoritesList.addAll(favoriteManager.getFavoriteItems());
                favoriteAdapter.notifyDataSetChanged();
                updateFavoritesUI();
            });

            rvFavorites.setAdapter(favoriteAdapter);
            updateFavoritesUI();
        }
    }

    private void updateFavoritesUI() {
        boolean isLoggedIn = com.example.saive.utils.UserSession.getInstance().isLoggedIn();

        if (tvFavoritesCount != null) {
            if (isLoggedIn) {
                tvFavoritesCount.setText(getString(R.string.favorites_items_count, favoritesList.size()));
                tvFavoritesCount.setVisibility(View.VISIBLE);
            } else {
                tvFavoritesCount.setVisibility(View.GONE);
            }
        }

        if (!isLoggedIn || favoritesList.isEmpty()) {
            emptyStateFavorites.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            emptyStateFavorites.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    private void updateNavHighlight(View activeView) {
        View[] navItems = {
                findViewById(R.id.navFavorite),
                findViewById(R.id.navWardrobe),
                findViewById(R.id.centerActionButton),
                findViewById(R.id.navNotify),
                findViewById(R.id.navProfile)
        };

        for (View item : navItems) {
            if (item == null)
                continue;
            boolean isActive = (item == activeView);

            float targetAlpha;
            if (item.getId() == R.id.centerActionButton) {
                targetAlpha = isActive ? 1.0f : 0.8f; // Home button less faded
            } else {
                targetAlpha = isActive ? 1.0f : 0.5f;
            }

            item.animate()
                    .alpha(targetAlpha)
                    .setDuration(250)
                    .start();
        }
    }

    private void animateNavIcon(View view) {
        updateNavHighlight(view);
    }

    private void setupWardrobe() {
        rvWardrobe = findViewById(R.id.rvWardrobe);
        fullWardrobeList = new ArrayList<>();
        wardrobeProductList = new ArrayList<>();

        // Load cached products from DataManager (previously fetched from Firebase)
        List<Product> cached = DataManager.getInstance(this).getProducts();
        if (cached != null && !cached.isEmpty()) {
            fullWardrobeList.addAll(cached);
        }

        wardrobeProductList.addAll(fullWardrobeList);
        wardrobeAdapter = new ProductGridAdapter(wardrobeProductList);
        rvWardrobe.setLayoutManager(new GridLayoutManager(this, 2));
        rvWardrobe.setAdapter(wardrobeAdapter);
    }

    /** Helper tạo product local và set luôn tagTypeGroup */
    private Product makeLocalProduct(String name, String price, int resId, String tagTypeGroup) {
        Product p = new Product(name, price, resId, tagTypeGroup);
        p.setTagTypeGroup(tagTypeGroup);
        return p;
    }

    /**
     * Fetch sản phẩm từ Firebase Realtime Database.
     */
    @android.annotation.SuppressLint("NotifyDataSetChanged")
    private void loadProductsFromServer() {
        try {
            com.google.firebase.database.DatabaseReference dbRef = 
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference();

            // Fetch categories first
            dbRef.child("Categories").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot catSnapshot) {
                    java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
                    for (com.google.firebase.database.DataSnapshot child : catSnapshot.getChildren()) {
                        String id = getStringSafe(child.child("CategoryId"));
                        String name = getStringSafe(child.child("CategoryName"));
                        if (id != null && !id.isEmpty() && name != null && !name.isEmpty()) {
                            categoryMap.put(id, name);
                        }
                    }

                    // Then fetch products
                    dbRef.child("Products").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot prodSnapshot) {
                            java.util.List<Product> mapped = new ArrayList<>();
                            for (com.google.firebase.database.DataSnapshot child : prodSnapshot.getChildren()) {
                                Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                                if (isActive != null && !isActive) continue;

                                String prodName = getStringSafe(child.child("ProductName"));
                                Object priceObj = child.child("Price").getValue();
                                String desc = getStringSafe(child.child("Description"));
                                String catId = getStringSafe(child.child("CategoryId"));

                                // Đọc tag_type_group từ Firebase — đây là trường chính cho filter
                                String tagTypeGroup = getStringSafe(child.child("tag_type_group"));
                                if (tagTypeGroup == null || tagTypeGroup.isEmpty())
                                    tagTypeGroup = getStringSafe(child.child("TagTypeGroup"));
                                if (tagTypeGroup != null) tagTypeGroup = tagTypeGroup.trim().toLowerCase();

                                // Đọc tag_style — dùng để matching phong cách
                                String tagStyle = getStringSafe(child.child("tag_style"));
                                if (tagStyle != null) tagStyle = tagStyle.trim().toLowerCase();

                                // Đọc tag_type — chi tiết loại sản phẩm
                                String tagType = getStringSafe(child.child("tag_type"));
                                if (tagType != null) tagType = tagType.trim().toLowerCase();

                                // Đọc tag_color — danh sách màu sắc
                                java.util.List<String> tagColorList = new java.util.ArrayList<>();
                                com.google.firebase.database.DataSnapshot tagColorSnap = child.child("tag_color");
                                if (tagColorSnap.exists()) {
                                    for (com.google.firebase.database.DataSnapshot colorChild : tagColorSnap.getChildren()) {
                                        String c = getStringSafe(colorChild);
                                        if (c != null && !c.isEmpty()) tagColorList.add(c.trim().toLowerCase());
                                    }
                                }

                                // Load variantsStock from Firebase (Variants or Stock node)
                                java.util.Map<String, java.util.Map<String, Integer>> variantsStock = new java.util.HashMap<>();
                                com.google.firebase.database.DataSnapshot variantsSnap = child.child("Variants");
                                if (!variantsSnap.exists()) variantsSnap = child.child("Stock");
                                
                                if (variantsSnap.exists()) {
                                    for (com.google.firebase.database.DataSnapshot variantSnap : variantsSnap.getChildren()) {
                                        String key = variantSnap.getKey();
                                        if (key == null) continue;
                                        
                                        if (key.contains("_")) {
                                            // Format: {size}_{color} (e.g., M_Black)
                                            String[] parts = key.split("_");
                                            if (parts.length == 2) {
                                                String size = parts[0];
                                                String color = parts[1];
                                                Object s = variantSnap.child("Stock").getValue();
                                                if (s == null) s = variantSnap.getValue();
                                                
                                                if (s instanceof Number) {
                                                    java.util.Map<String, Integer> colors = variantsStock.get(size);
                                                    if (colors == null) {
                                                        colors = new java.util.HashMap<>();
                                                        variantsStock.put(size, colors);
                                                    }
                                                    colors.put(color, ((Number) s).intValue());
                                                }
                                            }
                                        } else {
                                            // Legacy Format: {size}/{color}
                                            String size = key;
                                            java.util.Map<String, Integer> colors = variantsStock.get(size);
                                            if (colors == null) {
                                                colors = new java.util.HashMap<>();
                                                variantsStock.put(size, colors);
                                            }
                                            for (com.google.firebase.database.DataSnapshot colorSnap : variantSnap.getChildren()) {
                                                String color = colorSnap.getKey();
                                                Object s = colorSnap.child("Stock").getValue();
                                                if (s == null) s = colorSnap.getValue();
                                                if (s instanceof Number) {
                                                    colors.put(color, ((Number) s).intValue());
                                                }
                                            }
                                        }
                                    }
                                }

                                String firstImg = "";
                                com.google.firebase.database.DataSnapshot images = child.child("Images");
                                if (images.exists() && images.getChildrenCount() > 0) {
                                    firstImg = getStringSafe(images.getChildren().iterator().next());
                                } else if (child.child("imageUrl").exists()) {
                                    firstImg = getStringSafe(child.child("imageUrl"));
                                } else if (child.child("ImageUrl").exists()) {
                                    firstImg = getStringSafe(child.child("ImageUrl"));
                                }

                                java.util.List<String> allImages = new java.util.ArrayList<>();
                                if (images.exists()) {
                                    for (com.google.firebase.database.DataSnapshot img : images.getChildren()) {
                                        String url = getStringSafe(img);
                                        if (url != null && !url.isEmpty()) allImages.add(url);
                                    }
                                }
                                if (allImages.isEmpty()) {
                                    com.google.firebase.database.DataSnapshot imageUrlsSnap = child.child("imageUrls");
                                    if (!imageUrlsSnap.exists()) imageUrlsSnap = child.child("ImageUrls");
                                    
                                    if (imageUrlsSnap.exists()) {
                                        for (com.google.firebase.database.DataSnapshot img : imageUrlsSnap.getChildren()) {
                                            String url = getStringSafe(img);
                                            if (url != null && !url.isEmpty()) allImages.add(url);
                                        }
                                    }
                                }
                                if (allImages.isEmpty() && firstImg != null && !firstImg.isEmpty()) {
                                    allImages.add(firstImg);
                                }
                                Boolean isFeatured = child.child("IsFeatured").getValue(Boolean.class);

                                String catKey = categoryMap.get(catId);
                                if (catKey == null) catKey = tagTypeGroup != null ? tagTypeGroup : "other";

                                String priceStr = "0";
                                if (priceObj instanceof Number) {
                                    priceStr = com.example.saive.utils.PriceFormatter.formatPrice(((Number) priceObj).doubleValue());
                                } else if (priceObj instanceof String) {
                                    priceStr = (String) priceObj;
                                }

                                Product p = new Product(prodName, priceStr, R.drawable.tshirt1, catKey);
                                p.setProductId(child.getKey());
                                p.setImageUrl(firstImg);
                                p.setImageUrls(allImages);
                                p.setDescription(desc != null ? desc : "");
                                p.setNameEn(getStringSafe(child.child("ProductName_en")));
                                p.setNameZh(getStringSafe(child.child("ProductName_zh")));
                                p.setDescriptionEn(getStringSafe(child.child("Description_en")));
                                p.setDescriptionZh(getStringSafe(child.child("Description_zh")));
                                p.setFeatured(isFeatured != null && isFeatured);
                                // Lưu tất cả tags vào product — dùng cho recommendation
                                p.setTagTypeGroup(tagTypeGroup);
                                p.setTagStyle(tagStyle);
                                p.setTagType(tagType);
                                if (!tagColorList.isEmpty()) p.setTagColor(tagColorList);
                                if (!variantsStock.isEmpty()) p.setVariantsStock(variantsStock);
                                mapped.add(p);
                            }

                            if (!mapped.isEmpty()) {
                                fullWardrobeList.clear();
                                fullWardrobeList.addAll(mapped);
                                wardrobeProductList.clear();
                                wardrobeProductList.addAll(mapped);
                                if (wardrobeAdapter != null) wardrobeAdapter.notifyDataSetChanged();

                                // Update ViewPager products dynamically
                                productList.clear();
                                for (Product p : mapped) {
                                    if (p.isFeatured()) {
                                        productList.add(p);
                                    }
                                }
                                if (productList.isEmpty()) {
                                    for (int i = 0; i < Math.min(5, mapped.size()); i++) {
                                        productList.add(mapped.get(i));
                                    }
                                }
                                if (viewPager != null && viewPager.getAdapter() != null) {
                                    viewPager.getAdapter().notifyDataSetChanged();
                                }
                                
                                // Áp dụng lại filter
                                if (!currentCategory.equals(getString(R.string.cat_all))) {
                                    filterWardrobe(currentCategory);
                                }
                                
                                DataManager.getInstance(MainActivity.this).saveProducts(mapped);
                                // generateFlashSaleFromWardrobe(); // Đã được xử lý trong saveProducts
                                if (rvFlashSale != null && rvFlashSale.getAdapter() != null) {
                                    flashProductList.clear();
                                    flashProductList.addAll(DataManager.getInstance(MainActivity.this).getFlashSaleProducts());
                                    rvFlashSale.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            android.util.Log.e("MainActivity", "Firebase Products Cancelled", error.toException());
                        }
                    });
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    android.util.Log.e("MainActivity", "Firebase Categories Cancelled", error.toException());
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error starting loadProductsFromServer", e);
        }
    }

    private String getStringSafe(com.google.firebase.database.DataSnapshot snapshot) {
        if (snapshot == null || snapshot.getValue() == null) return "";
        return String.valueOf(snapshot.getValue());
    }


    private void setupNotifications() {
        rvNotifications = findViewById(R.id.rvNotifications);
        View emptyState = findViewById(R.id.emptyStateNotify);

        notificationList = new ArrayList<>();

        // Mock Data based on the prompt
        notificationList.add(new Notification(
                "drop_1",
                getString(R.string.notify_drop_title),
                getString(R.string.notify_drop_desc),
                getString(R.string.notify_drop_action),
                "2h",
                R.drawable.atumncollection1,
                Color.parseColor("#F0EDE3"),
                false,
                androidx.core.content.ContextCompat.getColor(this, R.color.colorAccentBrand),
                Notification.Type.DROP,
                System.currentTimeMillis() - 2 * 3600 * 1000 // 2h ago
        ));

        notificationList.add(new Notification(
                "order_1",
                getString(R.string.notify_order_title),
                getString(R.string.notify_order_desc),
                getString(R.string.notify_order_action),
                "1d",
                R.drawable.atumncollection2,
                Color.parseColor("#FAF8F3"),
                false,
                androidx.core.content.ContextCompat.getColor(this, R.color.colorAccentBrand),
                Notification.Type.ORDER,
                System.currentTimeMillis() - 24 * 3600 * 1000 // 1d ago
        ));

        notificationList.add(new Notification(
                "capsule_1",
                getString(R.string.notify_capsule_title),
                getString(R.string.notify_capsule_desc),
                getString(R.string.notify_capsule_action),
                "2d",
                R.drawable.saive_logo,
                Color.parseColor("#F5EFE6"),
                true,
                ContextCompat.getColor(this, R.color.colorSand),
                Notification.Type.CAPSULE,
                System.currentTimeMillis() - 2 * 24 * 3600 * 1000 // 2d ago
        ));

        notificationList.add(new Notification(
                "reminder_1",
                getString(R.string.notify_reminder_title),
                getString(R.string.notify_reminder_desc),
                getString(R.string.notify_reminder_action),
                "1w",
                R.drawable.saive_logo,
                Color.parseColor("#EDEBDD"),
                true,
                ContextCompat.getColor(this, R.color.colorSand),
                Notification.Type.REMINDER,
                System.currentTimeMillis() - 7 * 24 * 3600 * 1000 // 1w ago
        ));

        if (notificationList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            notificationAdapter = new NotificationAdapter(notificationList, this::updateNotificationBadge);
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(notificationAdapter);
        }
    }

    private void setupWardrobeCategories() {
        rvCategories = findViewById(R.id.rvCategories);
        if (rvCategories == null)
            return;

        currentCategory = getString(R.string.cat_all);
        categoryList = new ArrayList<>();
        // All + 7 tag_type_group của Firebase
        categoryList.add(new Category(getString(R.string.cat_all),       R.drawable.ic_all));
        categoryList.add(new Category(getString(R.string.cat_top),       R.drawable.ic_cat_top));
        categoryList.add(new Category(getString(R.string.cat_bottom),    R.drawable.ic_cat_bottom));
        categoryList.add(new Category(getString(R.string.cat_dress),     R.drawable.ic_cat_dress));
        categoryList.add(new Category(getString(R.string.cat_outerwear), R.drawable.ic_cat_outerwear));
        categoryList.add(new Category(getString(R.string.cat_shoes),     R.drawable.ic_cat_shoes));
        categoryList.add(new Category(getString(R.string.cat_bag),       R.drawable.ic_cat_bag));
        categoryList.add(new Category(getString(R.string.cat_accessory), R.drawable.ic_cat_accessory));

        wardrobeCategoryAdapter = new CategoryAdapter(categoryList, category -> {
            filterWardrobe(category.getName());
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(wardrobeCategoryAdapter);
    }

    /**
     * Map từ tên hiển thị → tag_type_group key (lowercase).
     * Ví dụ: "Top" → "top", "Outerwear" → "outerwear"
     */
    private String getTagGroupFromCategoryName(String categoryName) {
        if (categoryName.equals(getString(R.string.cat_top)))       return "top";
        if (categoryName.equals(getString(R.string.cat_bottom)))    return "bottom";
        if (categoryName.equals(getString(R.string.cat_dress)))     return "dress";
        if (categoryName.equals(getString(R.string.cat_outerwear))) return "outerwear";
        if (categoryName.equals(getString(R.string.cat_shoes)))     return "shoes";
        if (categoryName.equals(getString(R.string.cat_bag)))       return "bag";
        if (categoryName.equals(getString(R.string.cat_accessory))) return "accessory";
        return categoryName.toLowerCase();
    }

    /**
     * Kiểm tra sản phẩm có thuộc category đang chọn không.
     * Ưu tiên so sánh theo tagTypeGroup (từ Firebase tag_type_group),
     * fallback về category field nếu chưa có tagTypeGroup.
     */
    private boolean matchesCategory(Product p, String uiCategoryName) {
        String targetGroup = getTagGroupFromCategoryName(uiCategoryName);

        // Ưu tiên so sánh theo tag_type_group
        if (p.getTagTypeGroup() != null && !p.getTagTypeGroup().isEmpty()) {
            return p.getTagTypeGroup().equalsIgnoreCase(targetGroup);
        }

        // Fallback: so sánh theo category field (legacy data)
        if (p.getCategory() == null) return false;
        String cat = p.getCategory().toLowerCase();
        return cat.contains(targetGroup);
    }

    private void filterWardrobe(String categoryName) {
        currentCategory = categoryName;
        if (categoryName.equals(getString(R.string.cat_all))) {
            wardrobeProductList.clear();
            wardrobeProductList.addAll(fullWardrobeList);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : fullWardrobeList) {
                if (matchesCategory(p, categoryName)) {
                    filtered.add(p);
                }
            }
            wardrobeProductList.clear();
            wardrobeProductList.addAll(filtered);
        }

        if (emptyStateWardrobe != null) {
            if (wardrobeProductList.isEmpty()) {
                emptyStateWardrobe.setVisibility(View.VISIBLE);
                rvWardrobe.setVisibility(View.GONE);
            } else {
                emptyStateWardrobe.setVisibility(View.GONE);
                rvWardrobe.setVisibility(View.VISIBLE);
            }
        }

        if (wardrobeAdapter != null) {
            wardrobeAdapter.notifyDataSetChanged();
        }
    }

    private void setupFlashSale() {
        rvFlashSale = findViewById(R.id.rvFlashSale);

        // Luôn làm mới dữ liệu Flash Sale từ DataManager
        flashProductList = DataManager.getInstance(this).getFlashSaleProducts();

        // Nếu list rỗng, thử generate từ wardrobe hiện có
        if (flashProductList.isEmpty() && fullWardrobeList != null && !fullWardrobeList.isEmpty()) {
            DataManager.getInstance(this).generateAndSaveFlashSale(fullWardrobeList);
            flashProductList = DataManager.getInstance(this).getFlashSaleProducts();
        }

        // Cập nhật danh sách wardrobe gốc với thông tin sale
        updateWardrobeWithFlashSale();

        FlashProductAdapter adapter = new FlashProductAdapter(flashProductList);
        // Do not set color here, use default dark text for light item background

        // Thiết lập LinearLayoutManager nằm ngang
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFlashSale.setLayoutManager(layoutManager);
        rvFlashSale.setAdapter(adapter);

        // Thêm hiệu ứng mượt mà khi cuộn

        rvFlashSale.setNestedScrollingEnabled(false);

        // Nút View All Flash Sale
        View btnViewAll = findViewById(R.id.btnViewAllFlash);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", "FLASH SALE");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void generateFlashSaleFromWardrobe() {
        if (fullWardrobeList == null || fullWardrobeList.isEmpty())
            return;

        // Ưu tiên lấy sản phẩm được Admin đánh dấu là "Nổi bật" (isFeatured) cho Flash Sale
        List<Product> tempFlash = new ArrayList<>();
        
        for (Product p : fullWardrobeList) {
            if (p.isFeatured()) {
                try {
                    double priceVal = com.example.saive.utils.PriceFormatter.parsePrice(p.getPrice());
                    double discount = 0.3; // Giảm cố định 30% cho hàng nổi bật
                    double salePriceVal = priceVal * (1 - discount);

                    String salePrice = String.format(java.util.Locale.US, "%.0f", salePriceVal);
                    String originalPrice = String.format(java.util.Locale.US, "%.0f", priceVal);

                    Product flashProduct = new Product(p.getName(), salePrice, originalPrice,
                            p.getImageResId(), p.getCategory(), p.getDescription());
                    flashProduct.setProductId(p.getProductId());
                    flashProduct.setImageUrl(p.getImageUrl());
                    flashProduct.setImageUrls(p.getImageUrls());
                    flashProduct.setFeatured(true);
                    
                    tempFlash.add(flashProduct);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (tempFlash.size() >= 10) break; // Lấy tối đa 10 sản phẩm nổi bật
        }

        // Nếu Admin chưa chọn sản phẩm nổi bật nào, lấy ngẫu nhiên để không bị trống màn hình
        if (tempFlash.isEmpty()) {
            List<Product> shuffleList = new ArrayList<>(fullWardrobeList);
            java.util.Collections.shuffle(shuffleList);
            int count = Math.min(5, shuffleList.size());
            for (int i = 0; i < count; i++) {
                Product original = shuffleList.get(i);
                // ... logic cũ để tạo sản phẩm giả định ...
                try {
                    double priceVal = com.example.saive.utils.PriceFormatter.parsePrice(original.getPrice());
                    double discount = 0.2;
                    double salePriceVal = priceVal * (1 - discount);
                    String salePrice = String.format(java.util.Locale.US, "%.0f", salePriceVal);
                    Product flashProduct = new Product(original.getName(), salePrice, original.getPrice(),
                            original.getImageResId(), original.getCategory());
                    flashProduct.setImageUrl(original.getImageUrl());
                    tempFlash.add(flashProduct);
                } catch (Exception e) {}
            }
        }

        if (flashProductList == null) flashProductList = new ArrayList<>();
        flashProductList.clear();
        flashProductList.addAll(tempFlash);
        DataManager.getInstance(this).saveFlashSaleProducts(flashProductList);
    }

    private void updateWardrobeWithFlashSale() {
        if (fullWardrobeList == null || fullWardrobeList.isEmpty())
            return;

        List<Product> currentFlash = DataManager.getInstance(this).getFlashSaleProducts();
        if (currentFlash.isEmpty())
            return;

        for (Product wardrobeProduct : fullWardrobeList) {
            for (Product flashProduct : currentFlash) {
                if (wardrobeProduct.getName().equals(flashProduct.getName())) {
                    // Cập nhật giá sale cho sản phẩm trong wardrobe nếu trùng tên
                    wardrobeProduct.setPrice(flashProduct.getPrice());
                    wardrobeProduct.setOriginalPrice(flashProduct.getOriginalPrice());
                }
            }
        }
    }

    private void setupBannerViewPager() {
        bannerViewPager = findViewById(R.id.viewPagerBanner);
        bannerList = new ArrayList<>();
        bannerList.add(R.drawable.banner1);
        bannerList.add(R.drawable.banner2);
        bannerList.add(R.drawable.banner3);
        bannerList.add(R.drawable.model1);
        bannerList.add(R.drawable.model2);

        BannerAdapter bannerAdapter = new BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % bannerList.size();
                bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 5000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
        if (homeCountDownTimer != null) {
            homeCountDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.getInstance(this).removeProductListener(this);
    }

    @Override
    public void onProductsChanged() {
        runOnUiThread(() -> {
            List<Product> cached = DataManager.getInstance(this).getProducts();
            if (cached != null) {
                fullWardrobeList.clear();
                fullWardrobeList.addAll(cached);
                
                // Cập nhật flash sale list từ DataManager
                flashProductList.clear();
                flashProductList.addAll(DataManager.getInstance(this).getFlashSaleProducts());

                // Áp dụng lại filter và cập nhật UI
                filterWardrobe(currentCategory);
                
                if (rvFlashSale != null && rvFlashSale.getAdapter() != null) {
                    rvFlashSale.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load session & Block check — dùng Firebase Auth session, không dùng SharedPreferences
        com.example.saive.utils.UserSession userSession = com.example.saive.utils.UserSession.getInstance();
        if (userSession.isLoggedIn()) {
            if (!userSession.isCacheReady()) {
                userSession.loadUserFromDatabase(userSession.getEmail(), new com.example.saive.utils.UserSession.OnUserLoadedCallback() {
                    @Override
                    public void onSuccess() {
                        updateCartBadge();
                    }

                    @Override
                    public void onError(String message) {
                        if ("Blocked".equals(message)) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, getString(R.string.main_error_acc_blocked), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        }

        if (bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, 5000);
        }
        updateNotificationBadge();
        updateCartBadge(); // Cập nhật badge khi quay lại từ màn hình khác

        // Refresh favorites data and UI based on current auth state
        if (favoritesList != null) {
            favoritesList.clear();
            favoritesList.addAll(com.example.saive.utils.FavoriteManager.getInstance(this).getFavoriteItems());
            if (favoriteAdapter != null) {
                favoriteAdapter.notifyDataSetChanged();
            }
        }
        updateFavoritesUI();

        setupHomeTimer(); // Restart timer to sync or ensure it's running

        // Refresh flash sale and sync with wardrobe
        flashProductList.clear();
        flashProductList.addAll(DataManager.getInstance(this).getFlashSaleProducts());
        updateWardrobeWithFlashSale();

        // Refresh wardrobe adapter when returning to MainActivity
        if (wardrobeAdapter != null) {
            wardrobeAdapter.notifyDataSetChanged();
        }

        RecyclerView rvFlashSale = findViewById(R.id.rvFlashSale);
        if (rvFlashSale != null && rvFlashSale.getAdapter() != null) {
            rvFlashSale.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateNotificationBadge() {
        if (notificationBadge == null)
            return;

        // Hide notification badge for guests
        if (!com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
            notificationBadge.setVisibility(View.GONE);
            return;
        }

        boolean hasUnread = false;
        if (notificationList != null) {
            android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            for (Notification n : notificationList) {
                if (!prefs.getBoolean("read_" + n.getId(), n.isRead())) {
                    hasUnread = true;
                    break;
                }
            }
        }
        notificationBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerProducts);
        notificationBadge = findViewById(R.id.notificationBadge);
        homeScroll = findViewById(R.id.homeScroll);
        notificationsContainer = findViewById(R.id.notificationsContainer);
        wardrobeContainer = findViewById(R.id.wardrobeContainer);
        favoritesContainer = findViewById(R.id.favoritesContainer);
        flashSaleContainer = findViewById(R.id.flashSaleContainer);
        emptyStateWardrobe = findViewById(R.id.emptyStateWardrobe);
        rvFavorites = findViewById(R.id.rvFavorites);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
        emptyStateFavorites = findViewById(R.id.emptyStateFavorites);

        tvHomeHour = findViewById(R.id.tvHomeHour);
        tvHomeMinute = findViewById(R.id.tvHomeMinute);
        tvHomeSecond = findViewById(R.id.tvHomeSecond);

        productList = new ArrayList<>();
        List<Product> cachedProd = DataManager.getInstance(this).getProducts();
        if (cachedProd != null && !cachedProd.isEmpty()) {
            for (Product p : cachedProd) {
                if (p.isFeatured()) {
                    productList.add(p);
                }
            }
            if (productList.isEmpty()) {
                for (int i = 0; i < Math.min(5, cachedProd.size()); i++) {
                    productList.add(cachedProd.get(i));
                }
            }
        }
    }

    private void setupViewPager() {
        ProductAdapter adapter = new ProductAdapter(productList);
        viewPager.setAdapter(adapter);

        // Circular/Carousel Effect
        viewPager.setOffscreenPageLimit(3);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);

        // Standard horizontal carousel padding
        View recyclerView = viewPager.getChildAt(0);
        if (recyclerView instanceof RecyclerView) {
            recyclerView.setPadding(100, 0, 100, 0);
            ((RecyclerView) recyclerView).setClipToPadding(false);
        }

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(24));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float absPos = Math.abs(position);

                // Scale effect
                page.setScaleY(0.85f + (1 - absPos) * 0.15f);
                page.setScaleX(0.85f + (1 - absPos) * 0.15f);

                // Circular/Curve translation
                // As position goes from -1 to 1, we want the Y to dip in the middle or rise?
                // For a "circular" drag, we can offset Y based on position
                page.setTranslationY(absPos * 100);

                // Rotation for circular feel
                page.setRotation(position * -10f);

                page.setAlpha(0.5f + (1 - absPos) * 0.5f);
            }
        });

        viewPager.setPageTransformer(compositePageTransformer);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

        setupEditorialStack();
    }

    private void setupEditorialStack() {
        RecyclerView rvEditorial = findViewById(R.id.rvEditorialStack);
        List<EditorialCard> editorialCards = new ArrayList<>();

        editorialCards.add(new EditorialCard(
                getString(R.string.editorial_title_3),
                getString(R.string.editorial_story_3),
                getString(R.string.editorial_material_3),
                R.drawable.banner3,
                getString(R.string.explore_piece)));
        editorialCards.add(new EditorialCard(
                getString(R.string.editorial_title_2),
                getString(R.string.editorial_story_2),
                getString(R.string.editorial_material_2),
                R.drawable.model2,
                getString(R.string.explore_piece)));
        editorialCards.add(new EditorialCard(
                getString(R.string.editorial_title_1),
                getString(R.string.editorial_story_1),
                getString(R.string.editorial_material_1),
                R.drawable.model1,
                getString(R.string.explore_piece)));

        EditorialCardAdapter editorialAdapter = new EditorialCardAdapter(editorialCards, card -> {
            Intent intent;
            if (card.getTitle().equals(getString(R.string.editorial_title_3))) {
                // SAIVE STORY -> About
                intent = new Intent(this, AboutActivity.class);
            } else if (card.getTitle().equals(getString(R.string.editorial_title_2))) {
                // EVERYDAY BEAUTY -> Silk Story
                intent = new Intent(this, CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", "THE SILK STORY");
            } else {
                // TIMELESS CRAFT -> Monochrome
                intent = new Intent(this, CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", "THE MONOCHROME SERIES");
            }
            startActivity(intent);
            return null;
        });
        rvEditorial.setLayoutManager(new LinearLayoutManager(this));
        rvEditorial.setAdapter(editorialAdapter);
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showNotificationSortPopup(View v) {
        List<String> sortOptions = Arrays.asList(
                getString(R.string.sort_latest),
                getString(R.string.sort_oldest));

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvSheetTitle = sheetView.findViewById(R.id.tvSheetTitle);
        if (tvSheetTitle != null) {
            tvSheetTitle.setText(getString(R.string.notify_title)); // Or a specific sort string if you have one
        }

        RecyclerView rvOptions = sheetView.findViewById(R.id.rvSheetOptions);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        BottomSheetOptionAdapter adapter = new BottomSheetOptionAdapter(sortOptions, currentNotificationSortCriteria,
                option -> {
                    currentNotificationSortCriteria = option;
                    sortNotifications(option);
                    bottomSheetDialog.dismiss();
                });
        rvOptions.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void sortNotifications(String criteria) {
        if (notificationList == null || notificationList.isEmpty())
            return;

        java.util.Collections.sort(notificationList, (n1, n2) -> {
            if (criteria.equals(getString(R.string.sort_latest))) {
                return Long.compare(n2.getTimestamp(), n1.getTimestamp());
            } else if (criteria.equals(getString(R.string.sort_oldest))) {
                return Long.compare(n1.getTimestamp(), n2.getTimestamp());
            }
            return 0;
        });

        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showSortPopup(View v) {
        List<String> sortOptions = Arrays.asList(
                getString(R.string.sort_price_low_high),
                getString(R.string.sort_price_high_low),
                getString(R.string.sort_alphabetical_az),
                getString(R.string.sort_alphabetical_za),
                getString(R.string.sort_latest),
                getString(R.string.sort_oldest));

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvOptions = sheetView.findViewById(R.id.rvSheetOptions);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        BottomSheetOptionAdapter adapter = new BottomSheetOptionAdapter(sortOptions, currentSortCriteria, option -> {
            currentSortCriteria = option;
            sortWardrobe(option);
            bottomSheetDialog.dismiss();
        });
        rvOptions.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void sortWardrobe(String criteria) {
        java.util.Collections.sort(wardrobeProductList, (p1, p2) -> {
            if (criteria.equals(getString(R.string.sort_price_low_high))) {
                return Double.compare(parsePrice(p1.getPrice()), parsePrice(p2.getPrice()));
            } else if (criteria.equals(getString(R.string.sort_price_high_low))) {
                return Double.compare(parsePrice(p2.getPrice()), parsePrice(p1.getPrice()));
            } else if (criteria.equals(getString(R.string.sort_alphabetical_az))) {
                return p1.getName().compareToIgnoreCase(p2.getName());
            } else if (criteria.equals(getString(R.string.sort_alphabetical_za))) {
                return p2.getName().compareToIgnoreCase(p1.getName());
            } else if (criteria.equals(getString(R.string.sort_latest))) {
                return Long.compare(p2.getTimestamp(), p1.getTimestamp());
            } else if (criteria.equals(getString(R.string.sort_oldest))) {
                return Long.compare(p1.getTimestamp(), p2.getTimestamp());
            }
            return 0;
        });
        if (wardrobeAdapter != null) {
            wardrobeAdapter.notifyDataSetChanged();
        }
    }

    private void setupHomeTimer() {
        if (homeCountDownTimer != null) {
            homeCountDownTimer.cancel();
        }

        // Mocking endsAt for 24 hours from now to match FlashSaleActivity
        long diff = java.util.concurrent.TimeUnit.HOURS.toMillis(24);

        homeCountDownTimer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateHomeTimerUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                if (tvHomeHour != null)
                    tvHomeHour.setText("00");
                if (tvHomeMinute != null)
                    tvHomeMinute.setText("00");
                if (tvHomeSecond != null)
                    tvHomeSecond.setText("00");
            }
        }.start();
    }

    private void updateHomeTimerUI(long millis) {
        long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (tvHomeHour != null)
            tvHomeHour.setText(String.format(java.util.Locale.getDefault(), "%02d", hours));
        if (tvHomeMinute != null)
            tvHomeMinute.setText(String.format(java.util.Locale.getDefault(), "%02d", minutes));
        if (tvHomeSecond != null)
            tvHomeSecond.setText(String.format(java.util.Locale.getDefault(), "%02d", seconds));
    }

    private double parsePrice(String price) {
        if (price == null || price.isEmpty())
            return 0;
        try {
            // Remove currency symbols and whitespace
            String cleanPrice = price.replaceAll("[^0-9.,]", "").trim();

            // Check if it's the 1.200.000 format (VN)
            if (cleanPrice.contains(".") && cleanPrice.indexOf(".") != cleanPrice.lastIndexOf(".")) {
                cleanPrice = cleanPrice.replace(".", "");
            }
            // Check if it's 1.200.000,00 format
            else if (cleanPrice.contains(".") && cleanPrice.contains(",")) {
                cleanPrice = cleanPrice.replace(".", "").replace(",", ".");
            }
            // Check if it's 1200000,00 format
            else if (cleanPrice.contains(",") && cleanPrice.length() - cleanPrice.lastIndexOf(",") <= 3) {
                cleanPrice = cleanPrice.replace(",", ".");
            }
            // Check if it's 1,200,000 format
            else if (cleanPrice.contains(",") && cleanPrice.indexOf(",") != cleanPrice.lastIndexOf(",")) {
                cleanPrice = cleanPrice.replace(",", "");
            }

            if (cleanPrice.isEmpty())
                return 0;
            return Double.parseDouble(cleanPrice);
        } catch (Exception e) {
            return 0;
        }
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                subscribeToTopics();
            }
        } else {
            subscribeToTopics();
        }
    }

    private void subscribeToTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("promotions")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FCM", "Subscribed to promotions topic");
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("flash_sale")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FCM", "Subscribed to flash_sale topic");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                subscribeToTopics();
            }
        }
    }
}
