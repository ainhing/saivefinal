package com.example.saive.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.saive.adapters.FlashProductAdapter;
import com.example.saive.adapters.ReviewAdapter;
import com.example.saive.utils.FavoriteManager;
import com.example.saive.utils.ToastUtils;
import com.example.saive.utils.DataManager;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.utils.CartManager;
import com.example.saive.utils.PriceFormatter;
import com.example.saive.utils.ImageUtils;
import com.bumptech.glide.Glide;

import com.example.saive.adapters.ProductImageAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import com.example.saive.models.Review;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.airbnb.lottie.LottieAnimationView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends BaseActivity {

    private ViewPager2 vpProductImages;
    private TabLayout tabIndicator;
    private ImageView ivWardrobeIcon, btnBack;
    private ImageButton btnFavorite, btnShare;
    private View btnCart;
    private LottieAnimationView lottieFavorite;
    private TextView tvProductName, tvPrice, tvOriginalPrice, tvDescription, tvWardrobeAction, btnWriteReview, btnSeeMore, tvCartBadge, tvQuantity, tvQuantityBottom, tvSizeStockStatus, tvColorStockStatus;
    private View btnAddToWardrobe, sizeSelectionContainer, colorSelectionContainer, btnSizeGuide, btnRecommendation;
    private android.widget.LinearLayout colorSwatchContainer;
    private ImageButton btnDecrease, btnIncrease, btnDecreaseBottom, btnIncreaseBottom;
    private RecyclerView rvCompleteLook, rvReviews;
    private List<View> sizeViews, colorViews;
    
    private Product currentProduct;
    private boolean isAddedToWardrobe = false;
    private boolean isDescriptionExpanded = false;
    private int selectedQuantity = 1;
    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;
    private CartManager.OnCartChangeListener cartChangeListener;
    private FavoriteManager.OnFavoriteChangeListener favoriteChangeListener;
    private DataManager.OnReviewChangeListener reviewChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStatusBar();
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        View rootLayout = findViewById(android.R.id.content);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());

            View searchContainer = findViewById(R.id.searchContainer);
            if (searchContainer != null) {
                int paddingHorizontal = (int) (24 * getResources().getDisplayMetrics().density);
                searchContainer.setPadding(paddingHorizontal,
                        systemBars.top,
                        paddingHorizontal,
                        (int) (12 * getResources().getDisplayMetrics().density));
                searchContainer.bringToFront();
            }

            return insets;
        });

        initViews();
        setupData();
        setupListeners();
        setupReviews();
        setupCompleteTheLook();
        updateWardrobeUI();
        updateCartBadge();
        setupCartObserver();
        setupFavoriteObserver();
        setupReviewObserver();
    }

    private void updateStatusBar() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        // Product detail uses Cotton (Off-white/Black) header
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorCotton));
        
        if (!isDarkMode) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void setupCartObserver() {
        cartChangeListener = this::updateCartBadge;
        CartManager.getInstance(this).addListener(cartChangeListener);
    }

    private void setupFavoriteObserver() {
        favoriteChangeListener = this::updateFavoriteUI;
        FavoriteManager.getInstance(this).addListener(favoriteChangeListener);
    }

    private void setupReviewObserver() {
        reviewChangeListener = this::setupReviews;
        DataManager.getInstance(this).addReviewListener(reviewChangeListener);
    }

    private void updateFavoriteUI() {
        if (currentProduct != null && btnFavorite != null) {
            boolean isFavorite = FavoriteManager.getInstance(this).isFavorite(currentProduct);
            btnFavorite.setSelected(isFavorite);
            btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_heart_thin);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentBrand));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartChangeListener != null) {
            CartManager.getInstance(this).removeListener(cartChangeListener);
        }
        if (favoriteChangeListener != null) {
            FavoriteManager.getInstance(this).removeListener(favoriteChangeListener);
        }
        if (reviewChangeListener != null) {
            DataManager.getInstance(this).removeReviewListener(reviewChangeListener);
        }
    }

    private void updateCartBadge() {
        int count = CartManager.getInstance(this).getItemCount();
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {
        vpProductImages = findViewById(R.id.vpProductImages);
        tabIndicator = findViewById(R.id.tabIndicator);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        lottieFavorite = findViewById(R.id.lottieFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnSeeMore = findViewById(R.id.btnSeeMore);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        
        tvQuantity = findViewById(R.id.tvQuantity);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        
        rvCompleteLook = findViewById(R.id.rvCompleteLook);
        rvReviews = findViewById(R.id.rvReviews);
        
        btnAddToWardrobe = findViewById(R.id.btnAddToWardrobe);
        btnSizeGuide = findViewById(R.id.btnSizeGuide);
        ivWardrobeIcon = findViewById(R.id.ivWardrobeIcon);
        tvWardrobeAction = findViewById(R.id.tvWardrobeAction);

        sizeSelectionContainer = findViewById(R.id.sizeSelectionContainer);
        colorSelectionContainer = findViewById(R.id.colorSelectionContainer);
        colorSwatchContainer = findViewById(R.id.colorSwatchContainer);
        // colorViews will be populated dynamically in buildColorSwatches()
        colorViews = new ArrayList<>();
        sizeViews = java.util.Arrays.asList(
                findViewById(R.id.sizeXS),
                findViewById(R.id.sizeS),
                findViewById(R.id.sizeM),
                findViewById(R.id.sizeL),
                findViewById(R.id.sizeXL)
        );

        tvSizeStockStatus = findViewById(R.id.tvSizeStockStatus);
        tvColorStockStatus = findViewById(R.id.tvColorStockStatus);
        btnRecommendation = findViewById(R.id.btnRecommendation);
    }

    /**
     * Merges variantsStock and tagColor from DataManager cache into currentProduct.
     * These fields can be null after Java Serializable Intent round-trip.
     * DataManager holds the most up-to-date Firebase data.
     */
    private void mergeStockDataFromCache() {
        if (currentProduct == null) return;

        List<Product> cached = DataManager.getInstance(this).getProducts();
        Product match = null;

        // 1. Match by productId first (most accurate)
        if (currentProduct.getProductId() != null) {
            for (Product p : cached) {
                if (currentProduct.getProductId().equals(p.getProductId())) {
                    match = p;
                    break;
                }
            }
        }

        // 2. Fallback: match by name
        if (match == null && currentProduct.getName() != null) {
            for (Product p : cached) {
                if (currentProduct.getName().equalsIgnoreCase(p.getName())) {
                    match = p;
                    break;
                }
            }
        }

        // Copy productId if missing
        if (currentProduct.getProductId() == null && match.getProductId() != null) {
            currentProduct.setProductId(match.getProductId());
        }

        // Copy variantsStock if missing or empty
        if ((currentProduct.getVariantsStock() == null || currentProduct.getVariantsStock().isEmpty())
                && match.getVariantsStock() != null && !match.getVariantsStock().isEmpty()) {
            currentProduct.setVariantsStock(match.getVariantsStock());
        }

        // Copy tagColor if missing
        if ((currentProduct.getTagColor() == null || currentProduct.getTagColor().isEmpty())
                && match.getTagColor() != null && !match.getTagColor().isEmpty()) {
            currentProduct.setTagColor(match.getTagColor());
        }

        // Copy stockQuantity if zero
        if (currentProduct.getStockQuantity() == 0 && match.getStockQuantity() > 0) {
            currentProduct.setStockQuantity(match.getStockQuantity());
        }

        // Copy tagType/tagStyle/tagTypeGroup if missing
        if (currentProduct.getTagTypeGroup() == null && match.getTagTypeGroup() != null) {
            currentProduct.setTagTypeGroup(match.getTagTypeGroup());
        }
        if (currentProduct.getTagStyle() == null && match.getTagStyle() != null) {
            currentProduct.setTagStyle(match.getTagStyle());
        }
        if (currentProduct.getTagType() == null && match.getTagType() != null) {
            currentProduct.setTagType(match.getTagType());
        }
    }

    /** Map color names (lowercase) to hex color strings. */
    private String colorNameToHex(String colorName) {
        if (colorName == null) return "#9E9E9E";
        String normalized = colorName.toLowerCase(java.util.Locale.ROOT).trim();
        // Handle potential snake_case or spaces
        normalized = normalized.replace("_", "").replace(" ", "");

        switch (normalized) {
            // English colors
            case "black":        
            case "đen":          
            case "den":          return "#212121";
            
            case "white":        
            case "trắng":        
            case "trang":        return "#F5F5F5";
            
            case "gray":
            case "grey":         
            case "xám":          
            case "xam":          return "#9E9E9E";
            
            case "lightgray":
            case "lightgrey":    return "#E0E0E0";
            case "darkgray":
            case "darkgrey":     return "#616161";
            case "silver":       return "#C0C0C0";
            
            case "red":          
            case "đỏ":           
            case "do":           return "#E53935";
            case "darkred":
            case "maroon":       return "#800000";
            
            case "pink":         
            case "hồng":         
            case "hong":         return "#F06292";
            case "lightpink":    return "#F8BBD0";
            case "hotpink":      return "#FF69B4";
            
            case "blue":         
            case "xanhdương":    
            case "xanhduong":    
            case "xanhda":       return "#1E88E5";
            case "navy":
            case "darkblue":     return "#1A237E";
            case "lightblue":    return "#81D4FA";
            case "cyan":         return "#00BCD4";
            case "teal":         return "#009688";
            
            case "green":        
            case "xanhlá":       
            case "xanhla":       
            case "xanh":         // Generic 'xanh' usually implies green or blue, default to green here
            case "xanhlục":      return "#43A047";
            case "lightgreen":   return "#81C784";
            case "darkgreen":    return "#1B5E20";
            case "olive":        return "#808000";
            case "lime":         return "#CDDC39";
            
            case "yellow":       
            case "vàng":         
            case "vang":         return "#FDD835";
            case "gold":         return "#FFD700";
            case "orange":       
            case "cam":          return "#FB8C00";
            case "coral":        return "#FF7F50";
            case "peach":        return "#FFDAB9";
            
            case "brown":        
            case "nâu":          
            case "nau":          return "#6D4C41";
            case "beige":        return "#F5F5DC";
            case "cream":        
            case "kem":          return "#FFFDD0";
            case "khaki":        return "#F0E68C";
            case "tan":          return "#D2B48C";
            
            case "purple":       
            case "tím":          
            case "tim":          return "#8E24AA";
            case "lavender":     return "#E6E6FA";
            case "magenta":      return "#FF00FF";
            case "violet":       return "#EE82EE";
            
            default:             
                // Check if it's already a valid hex string
                if (normalized.startsWith("#") && (normalized.length() == 7 || normalized.length() == 9)) {
                    return normalized;
                }
                return "#9E9E9E"; // Default fallback
        }
    }

    /**
     * Builds color swatches dynamically based on the product's variantsStock keys.
     * Each color in the Stock node gets one circular swatch. Unknown colors get a gray circle.
     */
    private void buildColorSwatches(boolean isPerfume) {
        if (colorSwatchContainer == null) return;

        // Remove old swatches but keep tvColorStockStatus (last child)
        int keepCount = (tvColorStockStatus != null) ? 1 : 0;
        int childCount = colorSwatchContainer.getChildCount();
        for (int i = childCount - 1 - keepCount; i >= 0; i--) {
            colorSwatchContainer.removeViewAt(i);
        }
        colorViews.clear();

        // Collect unique colors from variantsStock
        List<String> stockColors = new ArrayList<>();
        if (currentProduct.getVariantsStock() != null) {
            for (java.util.Map<String, Integer> colorMap : currentProduct.getVariantsStock().values()) {
                if (colorMap == null) continue;
                for (String color : colorMap.keySet()) {
                    boolean found = false;
                    for (String sc : stockColors) {
                        if (sc.equalsIgnoreCase(color)) { found = true; break; }
                    }
                    if (!found) stockColors.add(color.toLowerCase(java.util.Locale.ROOT));
                }
            }
        }

        if (stockColors.isEmpty()) {
            // Hide the whole color section if no color variants defined
            if (colorSelectionContainer != null) colorSelectionContainer.setVisibility(View.GONE);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        int sizePx  = (int)(36 * density);  // swatch size in px
        int marginPx = (int)(12 * density); // spacing between swatches
        int dp6 = (int)(6 * density);       // border radius for the foreground drawable

        for (String colorName : stockColors) {
            // Create the color swatch View
            View swatch = new View(this);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(sizePx, sizePx);
            lp.setMarginEnd(marginPx);
            swatch.setLayoutParams(lp);

            // Background: solid circle using GradientDrawable
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            try {
                bg.setColor(android.graphics.Color.parseColor(colorNameToHex(colorName)));
            } catch (Exception e) {
                bg.setColor(android.graphics.Color.GRAY);
            }
            swatch.setBackground(bg);

            // White border for light colors (white, cream, beige, light gray)
            String hex = colorNameToHex(colorName);
            if (hex.equalsIgnoreCase("#F5F5F5") || hex.equalsIgnoreCase("#E0E0E0") 
                || hex.equalsIgnoreCase("#F5F5DC") || hex.equalsIgnoreCase("#FFFDD0")) {
                bg.setStroke((int)(1.5f * density), android.graphics.Color.parseColor("#CCCCCC"));
            }

            swatch.setTag(colorName);
            swatch.setClickable(true);
            swatch.setFocusable(true);

            // Foreground: selection indicator (ring when selected)
            swatch.setForeground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_color_square));

            // Check stock availability
            int totalStock = currentProduct.getStockForVariant(null, colorName);
            swatch.setAlpha(totalStock > 0 ? 1.0f : 0.3f);

            // Insert BEFORE tvColorStockStatus
            int insertIdx = (tvColorStockStatus != null)
                    ? colorSwatchContainer.indexOfChild(tvColorStockStatus)
                    : colorSwatchContainer.getChildCount();
            colorSwatchContainer.addView(swatch, insertIdx);
            colorViews.add(swatch);

            // Click listener
            swatch.setOnClickListener(v -> {
                // Deselect all
                for (View cv : colorViews) {
                    cv.setSelected(false);
                    cv.setScaleX(1.0f);
                    cv.setScaleY(1.0f);
                }
                // Select this
                v.setSelected(true);
                v.setScaleX(1.15f);
                v.setScaleY(1.15f);

                String color = v.getTag().toString();
                currentProduct.setSelectedColor(color);
                updateStockStatus(color, false);
                refreshSizeOptionsAvailability(color);
                updateWardrobeStatus();
            });
        }

        // Make sure color section is visible
        if (colorSelectionContainer != null && !isPerfume) {
            colorSelectionContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupData() {
        currentProduct = (Product) getIntent().getSerializableExtra("PRODUCT");

        if (currentProduct == null) {
            // Check lowercase version if uppercase fails
            currentProduct = (Product) getIntent().getSerializableExtra("product");
        }

        if (currentProduct == null) {
            // Fallback mock if no product passed
            currentProduct = new Product("Amor Mystique", "1.200.000 ₫", R.drawable.model1, "Perfume");
        }

        // ── Merge fresh stock/tag data from DataManager ──────────────────────────────
        // variantsStock and tagColor can be lost/null after Java Serializable round-trip
        // through Intent. DataManager always has the freshest Firebase data.
        mergeStockDataFromCache();
        // ─────────────────────────────────────────────────────────────────────────────

        // Reset selections when opening detail to force user to choose
        currentProduct.setSelectedSize(null);
        currentProduct.setSelectedColor(null);

        
        tvProductName.setText(currentProduct.getName());
        tvPrice.setText(PriceFormatter.formatPrice(currentProduct.getPrice()));
        
        if (currentProduct.getOriginalPrice() != null) {
            tvOriginalPrice.setText(PriceFormatter.formatPrice(currentProduct.getOriginalPrice()));
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }

        tvDescription.setText(currentProduct.getDescription());

        // Load ảnh slideshow
        setupImageSlider();

        // Khởi tạo hiển thị số lượng mặc định
        if (tvQuantity != null) tvQuantity.setText(String.valueOf(selectedQuantity));
        if (tvQuantityBottom != null) tvQuantityBottom.setText(String.valueOf(selectedQuantity));

        // Toggle Size/Color selection based on category
        String category = currentProduct.getCategory() != null ? currentProduct.getCategory().toLowerCase(java.util.Locale.ROOT) : "";
        boolean isGlasses = category.contains("glasses");
        boolean isPerfume = category.contains("perfume");

        if (sizeSelectionContainer != null) {
            sizeSelectionContainer.setVisibility((isGlasses || isPerfume) ? View.GONE : View.VISIBLE);
            
            // Initial availability check for Sizes
            for (View v : sizeViews) {
                if (v instanceof TextView) {
                    String size = ((TextView) v).getText().toString();
                    int totalSizeStock = currentProduct.getStockForVariant(size, null);
                    
                    // Dim size if completely out of stock across all colors
                    if (totalSizeStock <= 0) {
                        v.setAlpha(0.3f);
                    } else {
                        v.setAlpha(1.0f);
                    }
                }
            }
        }
        if (btnSizeGuide != null) {
            btnSizeGuide.setVisibility((isGlasses || isPerfume) ? View.GONE : View.VISIBLE);
        }
        buildColorSwatches(isPerfume);


        // Show "Shop The Look" button for all wearable product types
        // Only hide for perfume/fragrance which can't form a visual outfit
        boolean canRecommend = !isPerfume;
        if (btnRecommendation != null) {
            btnRecommendation.setVisibility(canRecommend ? View.VISIBLE : View.GONE);
        }

        // Check if already in wardrobe
        updateWardrobeStatus();
    }

    private void setupImageSlider() {
        if (currentProduct == null) return;

        List<String> urls = currentProduct.getImageUrls();
        if (urls == null) urls = new ArrayList<>();

        // Nếu có imageUrl đơn lẻ từ API nhưng list urls đang trống, thì dùng nó
        if (urls.isEmpty() && currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            urls.add(currentProduct.getImageUrl());
        }

        List<Integer> resIds = currentProduct.getImageResIds();
        if (resIds == null) resIds = new ArrayList<>();

        // CHỈ thêm ảnh mẫu nếu ĐÃ XÁC ĐỊNH đây là sản phẩm Local (không có URL nào)
        if (urls.isEmpty() && resIds.isEmpty()) {
            if (currentProduct.getImageResId() != 0) {
                resIds.add(currentProduct.getImageResId());
                // Chỉ thêm ảnh phụ nếu thực sự là sản phẩm mẫu của hệ thống
                if (currentProduct.getImageResId() == R.drawable.model1 || currentProduct.getImageResId() == R.drawable.model2) {
                    int otherModel = (currentProduct.getImageResId() == R.drawable.model1) ? R.drawable.model2 : R.drawable.model1;
                    resIds.add(otherModel);
                }
            } else {
                resIds.add(R.drawable.model1); // Ảnh mặc định cuối cùng
            }
        }

        ProductImageAdapter adapter = new ProductImageAdapter(urls, resIds);
        vpProductImages.setAdapter(adapter);

        // Setup dot indicator
        if (adapter.getItemCount() > 1) {
            tabIndicator.setVisibility(View.VISIBLE);
            new TabLayoutMediator(tabIndicator, vpProductImages, (tab, position) -> {
                // No text needed for dots
            }).attach();
        } else {
            tabIndicator.setVisibility(View.GONE);
        }
    }

    private void setupReviews() {
        if (currentProduct == null) return;
        
        final String currentProductName = currentProduct.getName() != null ? 
                currentProduct.getName().trim().toLowerCase().replaceAll("\\s+", "") : "";
        final String currentProductId = currentProduct.getProductId();

        reviewList = new ArrayList<>();
        
        // Lấy tất cả reviews từ DataManager
        List<Review> allReviews = DataManager.getInstance(this).getReviews();
        
        // Lọc reviews cho sản phẩm hiện tại và chỉ lấy những review ĐÃ DUYỆT
        for (Review r : allReviews) {
            if (!r.isApproved()) continue;

            boolean match = false;
            
            // 1. So sánh theo ProductId (Chính xác nhất)
            if (currentProductId != null && r.getProductId() != null) {
                if (currentProductId.equals(r.getProductId())) {
                    match = true;
                }
            }
            
            // 2. Fallback: So sánh theo ProductName (Chuẩn hóa)
            if (!match && r.getProductName() != null) {
                String normalizedRName = r.getProductName().trim().toLowerCase().replaceAll("\\s+", "");
                if (currentProductName.equals(normalizedRName)) {
                    match = true;
                }
            }

            if (match) {
                reviewList.add(r);
            }
        }

        // Kiểm tra xem user đã mua hàng chưa để hiện nút đánh giá
        String userId = com.example.saive.utils.UserSession.getInstance().getUserId();
        if (userId == null) userId = "";
        
        // Cần truyền đúng tên gốc cho hasPurchasedProduct vì nó cũng có logic so sánh
        if (DataManager.getInstance(this).hasPurchasedProduct(userId, currentProduct.getName())) {
            btnWriteReview.setVisibility(View.VISIBLE);
        } else {
            // DEBUG: Luôn hiện nút để test nếu cần, nhưng đúng logic là ẩn
            // btnWriteReview.setVisibility(View.VISIBLE); 
            btnWriteReview.setVisibility(View.GONE);
        }

        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupListeners() {
        if (btnSizeGuide != null) {
            btnSizeGuide.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showSizeGuideDialog();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = getString(R.string.share_message_format, currentProduct.getName(), currentProduct.getPrice());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.btn_share)));
            });
        }

        FrameLayout btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProductDetailActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showWriteReviewDialog();
            });
        }

        if (btnFavorite != null && currentProduct != null) {
            boolean isFavorite = FavoriteManager.getInstance(this).isFavorite(currentProduct);
            btnFavorite.setSelected(isFavorite);
            btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_heart_thin);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentBrand));

            btnFavorite.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                FavoriteManager favoriteManager = FavoriteManager.getInstance(this);
                boolean newState = !favoriteManager.isFavorite(currentProduct);
                
                if (newState) {
                    favoriteManager.addFavorite(currentProduct);
                    btnFavorite.setVisibility(View.INVISIBLE);
                    lottieFavorite.setVisibility(View.VISIBLE);
                    lottieFavorite.playAnimation();
                    lottieFavorite.addAnimatorUpdateListener(animation -> {
                        if (animation.getAnimatedFraction() >= 1f) {
                            lottieFavorite.setVisibility(View.GONE);
                            btnFavorite.setVisibility(View.VISIBLE);
                            btnFavorite.setSelected(true);
                            btnFavorite.setImageResource(R.drawable.ic_favorite);
                            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentBrand));
                        }
                    });
                } else {
                    favoriteManager.removeFavorite(currentProduct);
                    btnFavorite.setSelected(false);
                    btnFavorite.setImageResource(R.drawable.ic_heart_thin);
                    btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentBrand));
                }
            });
        }

        View.OnClickListener increaseListener = v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            selectedQuantity++;
            if (tvQuantity != null) tvQuantity.setText(String.valueOf(selectedQuantity));
            if (tvQuantityBottom != null) tvQuantityBottom.setText(String.valueOf(selectedQuantity));
        };
        
        if (btnIncrease != null) btnIncrease.setOnClickListener(increaseListener);
        if (btnIncreaseBottom != null) btnIncreaseBottom.setOnClickListener(increaseListener);

        View.OnClickListener decreaseListener = v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (selectedQuantity > 1) {
                selectedQuantity--;
                if (tvQuantity != null) tvQuantity.setText(String.valueOf(selectedQuantity));
                if (tvQuantityBottom != null) tvQuantityBottom.setText(String.valueOf(selectedQuantity));
            }
        };

        if (btnDecrease != null) btnDecrease.setOnClickListener(decreaseListener);
        if (btnDecreaseBottom != null) btnDecreaseBottom.setOnClickListener(decreaseListener);

        if (btnAddToWardrobe != null) {
            btnAddToWardrobe.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                toggleWardrobe();
            });
        }

        if (btnSeeMore != null) {
            btnSeeMore.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                isDescriptionExpanded = !isDescriptionExpanded;
                if (isDescriptionExpanded) {
                    tvDescription.setMaxLines(Integer.MAX_VALUE);
                    btnSeeMore.setText(R.string.see_less);
                } else {
                    tvDescription.setMaxLines(4);
                    btnSeeMore.setText(R.string.see_more);
                }
            });
        }

        if (btnRecommendation != null) {
            btnRecommendation.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProductDetailActivity.this, RecommendationActivity.class);
                intent.putExtra("PRODUCT", currentProduct);
                startActivity(intent);
            });
        }

        setupSelectionListeners(sizeViews, true);
        setupSelectionListeners(colorViews, false);
    }


    @android.annotation.SuppressLint("InflateParams")
    private void showWriteReviewDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_write_review, null);
        bottomSheetDialog.setContentView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarInput);
        EditText etReview = dialogView.findViewById(R.id.etComment);
        View btnSubmit = dialogView.findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            float rating = ratingBar.getRating();
            String reviewText = etReview.getText().toString().trim();
            
            if (reviewText.isEmpty()) {
                ToastUtils.showCustomToast(this, getString(R.string.product_error_review_empty));
                return;
            }

            String reviewerName = com.example.saive.utils.UserSession.getInstance().getDisplayName();
            if (reviewerName == null || reviewerName.isEmpty()) reviewerName = "User";
            Review newReview = new Review(
                currentProduct.getName(),
                reviewerName,
                rating,
                reviewText,
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()),
                null
            );
            newReview.setProductId(currentProduct.getProductId()); // Set ProductId
            newReview.setApproved(false); // Ensure it's marked as unapproved initially

            DataManager.getInstance(this).submitReviewToFirebase(newReview, () -> {
                reviewList.add(0, newReview);
                reviewAdapter.notifyItemInserted(0);
                rvReviews.scrollToPosition(0);
                ToastUtils.showCustomToast(this, getString(R.string.product_toast_review_success));
            }, () -> ToastUtils.showCustomToast(this, "Failed to submit review"));

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void setupSelectionListeners(List<View> views, boolean isSize) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(v -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    
                    for (View other : views) {
                        other.setSelected(false);
                    }
                    
                    v.setSelected(true);
                    
                    if (isSize && v instanceof TextView) {
                        String size = ((TextView) v).getText().toString();
                        currentProduct.setSelectedSize(size);
                        updateStockStatus(size, true);
                        // Update color availability based on selected size
                        refreshColorOptionsAvailability(size);
                    } else if (!isSize && v.getTag() != null) {
                        String color = v.getTag().toString();
                        currentProduct.setSelectedColor(color);
                        updateStockStatus(color, false);
                        // Update size availability based on selected color
                        refreshSizeOptionsAvailability(color);
                    }
                    
                    updateWardrobeStatus();
                });
            }
        }
    }

    private void refreshColorOptionsAvailability(String selectedSize) {
        for (View v : colorViews) {
            if (v != null && v.getTag() != null) {
                String colorTag = v.getTag().toString();
                int stock = currentProduct.getStockForVariant(selectedSize, colorTag);
                // Dim if this specific size+color combination is out of stock
                v.setAlpha(stock > 0 ? 1.0f : 0.3f);
            }
        }
    }

    private void refreshSizeOptionsAvailability(String selectedColor) {
        for (View v : sizeViews) {
            if (v instanceof TextView) {
                String size = ((TextView) v).getText().toString();
                int stock = currentProduct.getStockForVariant(size, selectedColor);

                // Dim size buttons if stock for THIS color is zero
                v.setAlpha(stock > 0 ? 1.0f : 0.3f);
            }
        }
    }

    private void updateStockStatus(String selection, boolean isSize) {
        String size = currentProduct.getSelectedSize();
        String color = currentProduct.getSelectedColor();
        
        int stock;
        boolean bothSelected = (size != null && color != null);
        
        if (bothSelected) {
            stock = currentProduct.getStockForVariant(size, color);
        } else if (size != null) {
            stock = currentProduct.getStockForVariant(size, null);
        } else if (color != null) {
            stock = currentProduct.getStockForVariant(null, color);
        } else {
            stock = currentProduct.getStockQuantity();
        }
        
        TextView targetTv = isSize ? tvSizeStockStatus : tvColorStockStatus;
        if (targetTv == null) return;
        
        if (stock <= 0) {
            targetTv.setText(R.string.inventory_out_of_stock);
            targetTv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            targetTv.setVisibility(View.VISIBLE);
        } else if (stock < 20) {
            // Low stock warning — always show this regardless of selection
            String status = "Chỉ còn " + stock + " sản phẩm";
            targetTv.setText(status);
            targetTv.setTextColor(ContextCompat.getColor(this, R.color.colorMaroon));
            targetTv.setVisibility(View.VISIBLE);
        } else {
            // Stock is fine: only show confirmation text when BOTH size & color selected
            if (bothSelected) {
                targetTv.setText("Còn hàng (" + stock + ")");
                targetTv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                targetTv.setVisibility(View.VISIBLE);
            } else {
                targetTv.setVisibility(View.GONE);
            }
        }

    }

    private void updateWardrobeStatus() {
        isAddedToWardrobe = CartManager.getInstance(this).isProductInCart(currentProduct);
        updateWardrobeUI();
    }

    private void toggleWardrobe() {
        // Kiểm tra chọn size nếu container đang hiển thị
        if (sizeSelectionContainer.getVisibility() == View.VISIBLE && currentProduct.getSelectedSize() == null) {
            ToastUtils.showCustomToast(this, getString(R.string.product_error_select_size));
            return;
        }
        // Kiểm tra chọn màu nếu container đang hiển thị
        if (colorSelectionContainer.getVisibility() == View.VISIBLE && currentProduct.getSelectedColor() == null) {
            ToastUtils.showCustomToast(this, getString(R.string.product_error_select_color));
            return;
        }

        CartManager cartManager = CartManager.getInstance(this);
        
        // Stock lookup for specific variant
        String selectedSize = currentProduct.getSelectedSize();
        String selectedColor = currentProduct.getSelectedColor();
        
        int stock = currentProduct.getStockForVariant(selectedSize, selectedColor);
        if (stock < 5) {
            ToastUtils.showCustomToast(this, getString(R.string.inventory_out_of_stock));
            return;
        }

        cartManager.addProduct(currentProduct, selectedQuantity);
        ToastUtils.showCustomToast(this, getString(R.string.product_toast_added_to_cart));
        
        updateWardrobeStatus();
    }

    private void updateWardrobeUI() {
        // Luôn hiển thị trạng thái ADD TO BAG để khuyến khích mua thêm
        btnAddToWardrobe.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorButtonBg)));
        ivWardrobeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorButtonText));
        tvWardrobeAction.setText(R.string.btn_add_to_wardrobe);
        tvWardrobeAction.setTextColor(ContextCompat.getColor(this, R.color.colorButtonText));
    }

    private void setupCompleteTheLook() {
        if (currentProduct == null) return;

        // Lấy category gốc và chuẩn hóa để so sánh
        String rawCat = currentProduct.getCategory();
        final String currentCat = (rawCat != null) ? rawCat.trim().toLowerCase() : "";
        final String currentName = currentProduct.getName() != null ? currentProduct.getName() : "";

        // Lấy danh sách sản phẩm thực từ DataManager (Admin data)
        List<Product> allProducts = com.example.saive.utils.DataManager.getInstance(this).getProducts();
        List<Product> suggestions = new ArrayList<>();

        if (allProducts != null && !allProducts.isEmpty()) {
            for (Product p : allProducts) {
                String pCat = (p.getCategory() != null) ? p.getCategory().trim().toLowerCase() : "";
                
                // So sánh: Chung category VÀ không phải là sản phẩm đang xem
                if (!currentCat.isEmpty() && pCat.equals(currentCat)) {
                    if (!p.getName().equalsIgnoreCase(currentName)) {
                        suggestions.add(p);
                    }
                }
                if (suggestions.size() >= 10) break; 
            }
        }

        View label = findViewById(R.id.tvLabelCompleteLook);
        if (suggestions.isEmpty()) {
            // Nếu không có sản phẩm nào cùng loại, ẩn toàn bộ section
            rvCompleteLook.setVisibility(View.GONE);
            if (label != null) label.setVisibility(View.GONE);
        } else {
            // Thiết lập Adapter và hiển thị
            FlashProductAdapter adapter = new FlashProductAdapter(suggestions);
            rvCompleteLook.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvCompleteLook.setAdapter(adapter);
            
            // Tắt NestedScrolling để cuộn mượt mà trong NestedScrollView
            rvCompleteLook.setNestedScrollingEnabled(false);
            
            rvCompleteLook.setVisibility(View.VISIBLE);
            if (label != null) label.setVisibility(View.VISIBLE);
        }
    }
}
