package com.example.saive.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.ProductGridAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

@android.annotation.SuppressLint("NotifyDataSetChanged")
public class CollectionDetailActivity extends BaseActivity implements com.example.saive.utils.DataManager.OnProductChangeListener {

    private ImageView ivHeroLeft, ivHeroRight, ivSectionModel;
    private TextView tvHeadline, tvSubHeadline, tvWhyTitle, tvWhyDesc;
    private RecyclerView rvCollectionProducts;
    private ProgressBar pbLoading;
    private TextView tvEmptyProducts;
    private ProductGridAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        // Register listener
        com.example.saive.utils.DataManager.getInstance(this).addProductListener(this);

        // Adjust status bar for dark mode if needed
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.id.topBar != 0 ? R.color.colorCotton : R.color.colorMaroon));
        }

        initViews();
        loadCollectionData();
        setupBottomNavigation();

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.example.saive.utils.DataManager.getInstance(this).removeProductListener(this);
    }

    @Override
    public void onProductsChanged() {
        runOnUiThread(() -> {
            String title = getIntent().getStringExtra("COLLECTION_TITLE");
            if (title == null) title = "THE SILK STORY";
            loadProductsFromApi(title.toUpperCase(java.util.Locale.getDefault()));
        });
    }

    private void setupBottomNavigation() {
        View bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        // Navigation Item Clicks
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateToMain(""));
        }

        View centerActionButton = findViewById(R.id.centerActionButton);
        if (centerActionButton != null) {
            centerActionButton.setOnClickListener(v -> navigateToMain(""));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> navigateToProfile());
        }

        View navWardrobe = findViewById(R.id.navWardrobe);
        if (navWardrobe != null) {
            navWardrobe.setOnClickListener(v -> navigateToMain("OPEN_WARDROBE"));
        }
        
        View navFavorite = findViewById(R.id.navFavorite);
        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> navigateToMain("OPEN_FAVORITES"));
        }
        
        View navNotify = findViewById(R.id.navNotify);
        if (navNotify != null) {
            navNotify.setOnClickListener(v -> navigateToMain("OPEN_NOTIFICATIONS"));
        }
    }

    private void initViews() {
        ivHeroLeft = findViewById(R.id.ivHeroLeft);
        ivHeroRight = findViewById(R.id.ivHeroRight);
        ivSectionModel = findViewById(R.id.ivSectionModel);
        tvHeadline = findViewById(R.id.tvHeadline);
        tvSubHeadline = findViewById(R.id.tvSubHeadline);
        tvWhyTitle = findViewById(R.id.tvWhyTitle);
        tvWhyDesc = findViewById(R.id.tvWhyDesc);
        rvCollectionProducts = findViewById(R.id.rvCollectionProducts);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyProducts = findViewById(R.id.tvEmptyProducts);

        // Pre-set states for animation to avoid "flashing"
        // Reduced translationY to 30f for smoother perceived motion
        View[] animatedViews = {ivHeroLeft, ivHeroRight, tvHeadline, tvSubHeadline, findViewById(R.id.btnShopAll), rvCollectionProducts};
        for (View v : animatedViews) {
            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(30f);
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.btnShopAll).setOnClickListener(v -> {
            rvCollectionProducts.getParent().requestChildFocus(rvCollectionProducts, rvCollectionProducts);
        });

        productList = new ArrayList<>();
        productAdapter = new ProductGridAdapter(productList);
        rvCollectionProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvCollectionProducts.setAdapter(productAdapter);
    }

    private void loadCollectionData() {
        String title = getIntent().getStringExtra("COLLECTION_TITLE");
        if (title == null) title = "THE SILK STORY";

        String titleUpper = title.toUpperCase(java.util.Locale.getDefault());
        com.google.android.material.button.MaterialButton btnShopAll = findViewById(R.id.btnShopAll);
        View heroSection = findViewById(R.id.heroSection);
        View productSection = findViewById(R.id.productSection);

        if (titleUpper.contains("MONOCHROME")) {
            // COLLECTION 1: THE MONOCHROME SERIES - Minimalist Grey/Black
            tvHeadline.setText(R.string.col_monochrome_headline);
            tvSubHeadline.setText(R.string.col_monochrome_subheadline);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.model2);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.banner2);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.jacket1);
            tvWhyTitle.setText(R.string.col_monochrome_why_title);
            tvWhyDesc.setText(R.string.col_monochrome_why_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#242424") : Color.parseColor("#F2F2F2"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1A1A1A") : Color.parseColor("#F9F9F9"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.WHITE : Color.BLACK));
                btnShopAll.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
                btnShopAll.setText(R.string.btn_shop_monochrome);
            }
        } else if (titleUpper.contains("AUTUMN") || titleUpper.contains("WINTER")) {
            // COLLECTION 2: THE AUTUMN COLLECTION - Warm Earthy
            tvHeadline.setText(R.string.col_autumn_headline);
            tvSubHeadline.setText(R.string.col_autumn_subheadline);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.atumncollection1);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.atumncollection2);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.model1);
            tvWhyTitle.setText(R.string.col_autumn_why_title);
            tvWhyDesc.setText(R.string.col_autumn_why_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#2A241A") : Color.parseColor("#FAF3E0"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1F1B12") : Color.parseColor("#FFFBF0"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.parseColor("#D4C9B8") : Color.parseColor("#810100")));
                btnShopAll.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
                btnShopAll.setText(R.string.btn_shop_autumn);
            }
        } else if (titleUpper.contains("ESSENTIALS")) {
            tvHeadline.setText(R.string.col_essentials_headline);
            tvSubHeadline.setText(R.string.col_essentials_subheadline);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.model2);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.atumncollection1);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.jacket3);
            tvWhyTitle.setText(R.string.col_essentials_why_title);
            tvWhyDesc.setText(R.string.col_essentials_why_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#24221F") : Color.parseColor("#F0EDE3"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1C1B19") : Color.parseColor("#F9F8F4"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.WHITE : Color.parseColor("#4A4A4A")));
                btnShopAll.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
                btnShopAll.setText(R.string.btn_shop_essentials);
            }
        } else if (titleUpper.contains("URBAN ARCHIVE")) {
            tvHeadline.setText(R.string.col_urban_headline);
            tvSubHeadline.setText(R.string.col_urban_subheadline);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.banner2);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.model1);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.pant2);
            tvWhyTitle.setText(R.string.col_urban_why_title);
            tvWhyDesc.setText(R.string.col_urban_why_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1A1A1A") : Color.parseColor("#E5E5E5"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#121212") : Color.parseColor("#EFEFEF"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.WHITE : Color.parseColor("#1A1A1A")));
                btnShopAll.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
                btnShopAll.setText(R.string.btn_explore_drop);
            }
        } else if (titleUpper.contains("FLASH SALE") || titleUpper.contains("LƯU TRỮ ĐẶC BIỆT")) {
            tvHeadline.setText(R.string.flash_sale_label);
            tvSubHeadline.setText(R.string.flash_sale_desc);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.jacket1);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.model1);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.atumncollection1);
            tvWhyTitle.setText(R.string.flash_sale_title);
            tvWhyDesc.setText(R.string.flash_sale_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#2A1A1A") : Color.parseColor("#FFF0F0"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1F1212") : Color.parseColor("#FFF5F5"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.parseColor("#E57373") : Color.parseColor("#D32F2F")));
                btnShopAll.setTextColor(Color.WHITE);
                btnShopAll.setText(R.string.label_flash_sale);
            }
        } else {
            // COLLECTION 3: THE SILK STORY - Heritage Beige
            tvHeadline.setText(R.string.col_silk_headline);
            tvSubHeadline.setText(R.string.col_silk_subheadline);
            ImageUtils.setSafeImage(ivHeroLeft, R.drawable.banner1);
            ImageUtils.setSafeImage(ivHeroRight, R.drawable.banner2);
            ImageUtils.setSafeImage(ivSectionModel, R.drawable.model2);
            tvWhyTitle.setText(R.string.col_silk_why_title);
            tvWhyDesc.setText(R.string.col_silk_why_desc);
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (heroSection != null) heroSection.setBackgroundColor(isDarkMode ? Color.parseColor("#1E1D18") : Color.parseColor("#EDEBDD"));
            if (productSection != null) productSection.setBackgroundColor(isDarkMode ? Color.parseColor("#151412") : Color.parseColor("#F5F4EE"));
            if (btnShopAll != null) {
                btnShopAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDarkMode ? Color.parseColor("#D4C9B8") : Color.parseColor("#BC8F8F")));
                btnShopAll.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
                btnShopAll.setText(R.string.btn_shop_silk);
            }
        }

        int adaptiveTextColor = ContextCompat.getColor(this, R.color.colorNoirBlack);
        productAdapter.setTextColor(adaptiveTextColor);
        loadProductsFromApi(titleUpper);  // Thử lấy từ API trước, fallback mock nếu lỗi
        applyAnimations();
    }

    private void applyAnimations() {
        // Use getDecorView().post() to ensure layout is ready
        getWindow().getDecorView().post(() -> {
            long duration = 700; // 700ms for a more premium, relaxed feel
            // Quintic Out: ultra-smooth deceleration (0.22, 1, 0.36, 1)
            android.view.animation.Interpolator interpolator = new android.view.animation.PathInterpolator(0.22f, 1f, 0.36f, 1f);
            
            // Staggered appearance with 120ms gaps for a more rhythmic flow
            animateView(ivHeroLeft, 50, duration, interpolator);
            animateView(ivHeroRight, 170, duration, interpolator);
            animateView(tvHeadline, 290, duration, interpolator);
            animateView(tvSubHeadline, 410, duration, interpolator);
            animateView(findViewById(R.id.btnShopAll), 530, duration, interpolator);
            animateView(rvCollectionProducts, 650, duration, interpolator);
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

    /**
     * Lấy sản phẩm từ DataManager theo categoryId tương ứng với collection.
     * Đảm bảo tính nhất quán về logic parsing ảnh và kho hàng.
     */
    private void loadProductsFromApi(String titleUpper) {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvEmptyProducts != null) tvEmptyProducts.setVisibility(View.GONE);

        List<Product> flashSaleProducts = com.example.saive.utils.DataManager.getInstance(this).getFlashSaleProducts();
        productList.clear();

        // Nếu là bộ sưu tập Flash Sale, hiển thị trực tiếp danh sách flash sale
        if (titleUpper.contains("FLASH SALE") || titleUpper.contains("LƯU TRỮ ĐẶC BIỆT")) {
            if (flashSaleProducts == null || flashSaleProducts.isEmpty()) {
                // Fallback: Nếu list flash sale rỗng nhưng có sản phẩm, thử generate lại
                List<Product> allProducts = com.example.saive.utils.DataManager.getInstance(this).getProducts();
                if (!allProducts.isEmpty()) {
                    com.example.saive.utils.DataManager.getInstance(this).generateAndSaveFlashSale(allProducts);
                    flashSaleProducts = com.example.saive.utils.DataManager.getInstance(this).getFlashSaleProducts();
                }
            }
            if (flashSaleProducts != null) {
                productList.addAll(flashSaleProducts);
            }
        } else {
            String categoryId = mapCollectionToCategoryId(titleUpper);
            List<Product> allProducts = com.example.saive.utils.DataManager.getInstance(this).getProducts();

            android.util.Log.d("CollectionDetail", "Filtering for category/tag: " + categoryId + ". Total products: " + allProducts.size());

            for (Product p : allProducts) {
                // Refined mapping: check CategoryId OR TagTypeGroup (consistency with MainActivity)
                String pCat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                String pTag = p.getTagTypeGroup() != null ? p.getTagTypeGroup().toLowerCase() : "";
                
                boolean matchCategory = categoryId.equalsIgnoreCase(pCat);
                boolean matchTag = categoryId.equalsIgnoreCase(pTag);
                
                if (matchCategory || matchTag) {
                    // Check if product is in flash sale and update price
                    for (Product flashProduct : flashSaleProducts) {
                        if (flashProduct.getProductId() != null && flashProduct.getProductId().equals(p.getProductId())) {
                            p.setPrice(flashProduct.getPrice());
                            p.setOriginalPrice(flashProduct.getOriginalPrice());
                            break;
                        }
                    }
                    productList.add(p);
                }
            }
        }

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (productList.isEmpty()) {
            if (tvEmptyProducts != null) tvEmptyProducts.setVisibility(View.VISIBLE);
        } else {
            if (tvEmptyProducts != null) tvEmptyProducts.setVisibility(View.GONE);
            productAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Map tên tiêu đề collection → CategoryId/TagTypeGroup trong Firebase.
     * Chỉnh sửa các key này nếu dữ liệu trong DB khác.
     */
    private String mapCollectionToCategoryId(String titleUpper) {
        if (titleUpper.contains("MONOCHROME"))               return "outerwear"; // Hoặc "top" nếu muốn
        if (titleUpper.contains("AUTUMN") || titleUpper.contains("WINTER")) return "outerwear";
        if (titleUpper.contains("ESSENTIALS"))               return "top";
        if (titleUpper.contains("URBAN ARCHIVE"))            return "bottom";
        if (titleUpper.contains("SILK"))                     return "dress";
        return "top"; // default
    }

    private void loadMockProducts(String titleUpper) {
        // Removed local mock fallback to only display Firebase products
        productList.clear();
        productAdapter.notifyDataSetChanged();
    }
}
