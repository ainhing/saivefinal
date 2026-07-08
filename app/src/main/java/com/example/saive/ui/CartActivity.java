package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import com.example.saive.R;
import com.example.saive.adapters.CartAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.models.Coupon;
import com.example.saive.utils.DataManager;
import com.example.saive.utils.CartManager;
import com.example.saive.utils.DialogUtils;
import com.example.saive.utils.ToastUtils;
import java.util.List;
import java.util.Locale;

public class CartActivity extends BaseActivity {

    private RecyclerView rvCartItems;
    private View emptyStateCart;
    private TextView tvSubtotal, tvTotalPrice, tvDiscountValue;
    private View layoutDiscount, layoutExpandableDetails, layoutTotalToggle;
    private ImageView ivExpandArrow;
    private EditText etCoupon;
    private View btnApplyCoupon;
    private CartAdapter adapter;
    private CartManager cartManager;

    private boolean isSummaryExpanded = false;
    private double currentDiscountRate = 0;
    private double currentDiscountValue = 0;
    private String appliedCouponCode = "";
    private String appliedCouponType = "Percentage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        updateStatusBar();

        setContentView(R.layout.activity_cart);

        initViews();
        setupCartList();

        handleIntent(getIntent());
    }

    private void updateStatusBar() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        // Setup status bar for full-bleed header
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorHeaderBg));
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        
        // In Dark Mode, colorHeaderBg becomes beige, so we need dark icons
        if (isDarkMode) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("COUPON_CODE")) {
            String code = intent.getStringExtra("COUPON_CODE");
            if (code != null) {
                etCoupon.setText(code);
                applyCoupon();
            }
        }
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyStateCart = findViewById(R.id.emptyStateCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvDiscountValue = findViewById(R.id.tvDiscountValue);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        layoutExpandableDetails = findViewById(R.id.layoutExpandableDetails);
        layoutTotalToggle = findViewById(R.id.layoutTotalToggle);
        ivExpandArrow = findViewById(R.id.ivExpandArrow);
        etCoupon = findViewById(R.id.etCoupon);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        View btnBack = findViewById(R.id.btnBack);
        View btnCheckout = findViewById(R.id.btnCheckout);

        if (layoutTotalToggle != null) {
            layoutTotalToggle.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                toggleSummaryExpansion();
            });
        }

        // Navigation Bar Items
        View navFavorite = findViewById(R.id.navFavorite);
        View navWardrobe = findViewById(R.id.navWardrobe);
        View navHome = findViewById(R.id.navHome);
        View navNotify = findViewById(R.id.navNotify);
        View navProfile = findViewById(R.id.navProfile);
        View centerActionButton = findViewById(R.id.centerActionButton);

        btnBack.setOnClickListener(v -> finish());

        // Navigation Listeners
        navFavorite.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.putExtra("SHOW_FAVORITES", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navWardrobe.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.putExtra("SHOW_WARDROBE", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        View.OnClickListener homeListener = v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.putExtra("SHOW_HOME", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        };
        navHome.setOnClickListener(homeListener);
        centerActionButton.setOnClickListener(homeListener);

        navNotify.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.putExtra("SHOW_NOTIFICATIONS", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getCartItems().isEmpty()) {
                ToastUtils.showCustomToast(this, getString(R.string.cart_empty_error));
                return;
            }
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("total_price", tvTotalPrice.getText().toString());
            intent.putExtra("discount_rate", currentDiscountRate);
            intent.putExtra("discount_value", currentDiscountValue);
            intent.putExtra("discount_type", appliedCouponType);
            intent.putExtra("coupon_code", appliedCouponCode);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnApplyCoupon.setOnClickListener(v -> {
            applyCoupon();
        });

        // Add click listener to etCoupon or a container to open CouponActivity
        etCoupon.setFocusable(false);
        etCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CouponActivity.class);
            startActivityForResult(intent, 1001);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra("COUPON_CODE");
            if (code != null) {
                etCoupon.setText(code);
                applyCoupon();
            }
        }
    }

    private void applyCoupon() {
        String code = etCoupon.getText().toString().trim();
        if (code.isEmpty()) {
            ToastUtils.showCustomToast(this, getString(R.string.coupon_empty));
            return;
        }

        List<Coupon> coupons = DataManager.getInstance(this).getCoupons();
        Coupon foundCoupon = null;
        
        for (Coupon coupon : coupons) {
            if (coupon.getCode().equalsIgnoreCase(code)) {
                foundCoupon = coupon;
                break;
            }
        }

        if (foundCoupon != null) {
            currentDiscountRate = 0;
            currentDiscountValue = 0;
            appliedCouponCode = foundCoupon.getCode();
            appliedCouponType = foundCoupon.getType();
            if (appliedCouponType == null) appliedCouponType = "Percentage";
            
            double subtotal = cartManager.getTotalPrice();
            String rawDiscount = foundCoupon.getDiscount();
            
            // Tự động nhận diện Fixed vs Percentage để tránh lỗi data từ Firebase
            boolean isPercentageStr = rawDiscount != null && rawDiscount.contains("%");
            double parsedVal = com.example.saive.utils.PriceFormatter.parsePrice(rawDiscount);
            boolean isFixedStr = rawDiscount != null && (rawDiscount.toUpperCase().contains("K") || parsedVal > 100);
            
            if (isFixedStr || appliedCouponType.equalsIgnoreCase("Fixed") || appliedCouponType.equalsIgnoreCase("amount") || appliedCouponType.equalsIgnoreCase("discount")) {
                // Fixed amount discount
                appliedCouponType = "Fixed"; // Ghi đè để CheckoutActivity hiểu
                currentDiscountValue = parsedVal;
                if (subtotal > 0) currentDiscountRate = currentDiscountValue / subtotal;
                ToastUtils.showCustomToast(this, getString(R.string.toast_coupon_applied_percent, (int)currentDiscountValue) + " ₫");
            } else if (appliedCouponType.equalsIgnoreCase("FreeShipping")) {
                currentDiscountValue = 0; 
                currentDiscountRate = 0;
                ToastUtils.showCustomToast(this, "Free Shipping Applied");
            } else {
                // Default: Percentage
                appliedCouponType = "Percentage";
                currentDiscountRate = parseDiscount(rawDiscount);
                currentDiscountValue = subtotal * currentDiscountRate;
                int percent = (int) (currentDiscountRate * 100);
                ToastUtils.showCustomToast(this, getString(R.string.toast_coupon_applied_percent, percent));
            }
        } else {
            currentDiscountRate = 0;
            currentDiscountValue = 0;
            appliedCouponCode = "";
            appliedCouponType = "Percentage";
            ToastUtils.showCustomToast(this, getString(R.string.coupon_invalid));
        }
        
        updateTotal();
    }

    private double parseDiscount(String discountStr) {
        try {
            if (discountStr == null || discountStr.isEmpty()) return 0;
            String clean = discountStr.replace("%", "").trim();
            double value = Double.parseDouble(clean);
            // If the string contains '%' or the value is > 1 (like "10"), treat it as a percentage
            if (discountStr.contains("%") || value >= 1) {
                return value / 100.0;
            }
            return value;
        } catch (Exception e) {
            return 0;
        }
    }

    private void setupCartList() {
        cartManager = CartManager.getInstance(this);
        List<Product> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            checkEmptyState();
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            emptyStateCart.setVisibility(View.GONE);

            adapter = new CartAdapter(cartItems, new CartAdapter.OnCartChangeListener() {
                @Override
                public void onRemove(int position) {
                    DialogUtils.showCustomAlertDialog(
                            CartActivity.this,
                            getString(R.string.dialog_remove_item_title),
                            getString(R.string.dialog_remove_item_msg),
                            getString(R.string.dialog_remove),
                            getString(R.string.dialog_cancel),
                            () -> {
                                Product product = cartItems.get(position);
                                cartManager.removeProduct(product);
                                adapter.notifyItemRemoved(position);
                                updateTotal();
                                checkEmptyState();
                            }
                    );
                }

                @Override
                public void onQuantityChanged(Product product) {
                    cartManager.updateQuantity(product, product.getQuantity());
                    updateTotal();
                }

                @Override
                public void onVariantClick(int position, Product product) {
                    showVariantSelectionDialog(position, product);
                }
            });

            rvCartItems.setLayoutManager(new LinearLayoutManager(this));
            rvCartItems.setAdapter(adapter);
            updateTotal();
        }
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showVariantSelectionDialog(int position, Product product) {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase(java.util.Locale.ROOT) : "";
        boolean isGlasses = category.contains("glasses");
        boolean isPerfume = category.contains("perfume");
        
        String[] options = isGlasses ? new String[]{"Black", "Tortoise", "Gold", "Silver"} : new String[]{"XS", "S", "M", "L", "XL"};
        String title = isGlasses ? getString(R.string.label_select_color) : getString(R.string.label_select_size);
        String currentSelection = isGlasses ? product.getSelectedColor() : product.getSelectedSize();

        View dialogView = getLayoutInflater().inflate(R.layout.layout_variant_selection, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout optionsContainer = dialogView.findViewById(R.id.optionsContainer);
        View btnSizeGuide = dialogView.findViewById(R.id.btnSizeGuide);
        tvTitle.setText(title);

        if (btnSizeGuide != null) {
            btnSizeGuide.setVisibility((isGlasses || isPerfume) ? View.GONE : View.VISIBLE);
            btnSizeGuide.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showSizeGuideDialog();
            });
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Base_Theme_Saive).setView(dialogView).create();

        for (String option : options) {
            TextView btnOption = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (48 * getResources().getDisplayMetrics().density),
                    (int) (48 * getResources().getDisplayMetrics().density));
            params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
            btnOption.setLayoutParams(params);
            btnOption.setGravity(android.view.Gravity.CENTER);
            btnOption.setText(option);
            btnOption.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            btnOption.setTextAppearance(R.style.TextAppearance_Saive_Nav);
            btnOption.setBackgroundResource(R.drawable.bg_variant_selector);
            btnOption.setTextColor(ContextCompat.getColorStateList(this, R.color.selector_size_text));
            
            if (option.equals(currentSelection)) {
                btnOption.setSelected(true);
            } else {
                btnOption.setSelected(false);
            }

            btnOption.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (isGlasses) {
                    product.setSelectedColor(option);
                } else {
                    product.setSelectedSize(option);
                }
                adapter.notifyItemChanged(position);
                cartManager.updateQuantity(product, product.getQuantity());
                dialog.dismiss();
            });

            optionsContainer.addView(btnOption);
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void toggleSummaryExpansion() {
        isSummaryExpanded = !isSummaryExpanded;
        
        if (layoutExpandableDetails != null) {
            if (isSummaryExpanded) {
                layoutExpandableDetails.setVisibility(View.VISIBLE);
                if (ivExpandArrow != null) ivExpandArrow.setRotation(180);
            } else {
                layoutExpandableDetails.setVisibility(View.GONE);
                if (ivExpandArrow != null) ivExpandArrow.setRotation(0);
            }
            
            android.transition.TransitionManager.beginDelayedTransition(findViewById(R.id.checkoutContainer));
        }
    }

    private void updateTotal() {
        double subtotal = cartManager.getTotalPrice();
        double discountAmount = 0;
        
        if ("Fixed".equalsIgnoreCase(appliedCouponType)) {
            discountAmount = currentDiscountValue;
        } else {
            discountAmount = subtotal * currentDiscountRate;
        }
        
        double total = Math.max(0, subtotal - discountAmount);

        tvSubtotal.setText(com.example.saive.utils.PriceFormatter.formatPrice(subtotal));
        
        if (discountAmount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscountValue.setText(getString(R.string.format_negative_price, com.example.saive.utils.PriceFormatter.formatPrice(discountAmount)));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
        
        tvTotalPrice.setText(com.example.saive.utils.PriceFormatter.formatPrice(total));
    }



    private void checkEmptyState() {
        if (cartManager.getCartItems().isEmpty()) {
            rvCartItems.setVisibility(View.GONE);
            emptyStateCart.setVisibility(View.VISIBLE);
            tvSubtotal.setText(R.string.price_zero_format);
            tvTotalPrice.setText(R.string.price_zero_format);
            layoutDiscount.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}