package com.example.saive.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.saive.adapters.BottomSheetOptionAdapter;
import java.util.Arrays;
import java.util.List;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.utils.DialogUtils;
import com.example.saive.utils.DataManager;

import java.util.Locale;

public class ProfileActivity extends BaseActivity {

    private View notificationBadge;
    private com.google.android.material.button.MaterialButton btnLogoutNew;
    private TextView tvCurrentLanguage;
    private TextView tvChangePassword;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String USER_PREFS = "user_prefs";
    private static final String LANG_PREFS = "language_prefs";
    private static final String LANG_KEY = "selected_language";

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
        updateAuthUI();
        loadAvatarFromPrefs();
    }

    private void loadAvatarFromPrefs() {
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar == null)
            return;

        String avatarUrl = com.example.saive.utils.UserSession.getInstance().getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, android.net.Uri.parse(avatarUrl));
            } catch (Exception ignored) {
                com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, R.drawable.model1);
            }
        } else {
            com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, R.drawable.model1);
        }
    }

    private void updateAuthUI() {
        com.example.saive.utils.UserSession session = com.example.saive.utils.UserSession.getInstance();
        boolean isLoggedIn = session.isLoggedIn();

        if (btnLogoutNew != null) {
            btnLogoutNew.setText(isLoggedIn ? R.string.menu_logout : R.string.menu_login);
        }

        // Cập nhật tên người dùng
        TextView tvUserName = findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            if (isLoggedIn) {
                // Lấy tên từ session (in-memory cache từ Firebase DB)
                String name = session.getDisplayName();
                if (name == null || name.isEmpty()) {
                    // Fallback: dùng phần trước @ của email
                    String em = session.getEmail();
                    name = (em != null && em.contains("@")) ? em.split("@")[0] : getString(R.string.profile_user_name);
                }
                tvUserName.setText(name);
                tvUserName.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorNoirBlack));
            } else {
                tvUserName.setText(R.string.msg_guest_profile_prompt);
                tvUserName.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorMaroon));
            }
        }

        // Update current language display
        if (tvCurrentLanguage != null) {
            SharedPreferences langPrefs = getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
            String currentLang = langPrefs.getString(LANG_KEY, "en");
            if (currentLang.equals("vi"))
                tvCurrentLanguage.setText(R.string.lang_vi);
            else if (currentLang.equals("zh"))
                tvCurrentLanguage.setText(R.string.lang_zh);
            else
                tvCurrentLanguage.setText(R.string.lang_en);
        }

        // Cập nhật text cho Change Password
        if (tvChangePassword != null) {
            tvChangePassword.setText(R.string.change_password_title);
        }

        // Cập nhật số lượng bộ sưu tập đã lưu (Favorites)
        TextView tvSavedCollectionsCount = findViewById(R.id.tvSavedCollectionsCount);
        if (tvSavedCollectionsCount != null) {
            if (isLoggedIn) {
                int count = com.example.saive.utils.FavoriteManager.getInstance(this).getItemCount();
                tvSavedCollectionsCount.setText(String.valueOf(count));
                tvSavedCollectionsCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            } else {
                tvSavedCollectionsCount.setVisibility(View.GONE);
            }
        }

        // Ẩn badge thông báo nếu không đăng nhập (bao gồm các thông báo về cart/order nếu có)
        if (!isLoggedIn && notificationBadge != null) {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private void updateNotificationBadge() {
        if (notificationBadge == null)
            return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);

        String[] ids = { "drop_1", "order_1", "capsule_1", "reminder_1" };
        boolean[] defaultRead = { false, false, true, true };

        boolean hasUnread = false;
        for (int i = 0; i < ids.length; i++) {
            if (!prefs.getBoolean("read_" + ids[i], defaultRead[i])) {
                hasUnread = true;
                break;
            }
        }
        notificationBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showLanguageDialog() {
        List<String> languages = Arrays.asList(getString(R.string.lang_en), getString(R.string.lang_vi),
                getString(R.string.lang_zh));
        List<String> langCodes = Arrays.asList("en", "vi", "zh");

        SharedPreferences prefs = getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
        String currentLang = prefs.getString(LANG_KEY, "en");

        String currentLangName = getString(R.string.lang_en);
        if (currentLang.equals("vi"))
            currentLangName = getString(R.string.lang_vi);
        else if (currentLang.equals("zh"))
            currentLangName = getString(R.string.lang_zh);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvSheetTitle);
        tvTitle.setText(R.string.menu_language);

        RecyclerView rvOptions = sheetView.findViewById(R.id.rvSheetOptions);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        BottomSheetOptionAdapter adapter = new BottomSheetOptionAdapter(languages, currentLangName, option -> {
            int index = languages.indexOf(option);
            String selectedLang = langCodes.get(index);
            if (!selectedLang.equals(currentLang)) {
                setLocale(selectedLang);
            }
            bottomSheetDialog.dismiss();
        });
        rvOptions.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void setLocale(String langCode) {
        SharedPreferences.Editor editor = getSharedPreferences(LANG_PREFS, MODE_PRIVATE).edit();
        editor.putString(LANG_KEY, langCode);
        editor.apply();

        // Show loading screen while switching language
        Intent intent = new Intent(this, LanguageLoadingActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void navigateToMain(String sectionExtra) {
        Intent intent = new Intent(this, MainActivity.class);
        if (sectionExtra != null) {
            intent.putExtra(sectionExtra, true);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
        String lang = prefs.getString(LANG_KEY, "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
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
        setContentView(R.layout.activity_profile);

        View profileScroll = findViewById(R.id.profileScroll);
        ViewCompat.setOnApplyWindowInsetsListener(profileScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            View headerContainer = findViewById(R.id.headerContainer);
            if (headerContainer != null) {
                headerContainer.setPadding(headerContainer.getPaddingLeft(),
                        systemBars.top + (int) (14 * getResources().getDisplayMetrics().density),
                        headerContainer.getPaddingRight(),
                        headerContainer.getPaddingBottom());
            }

            View bottomNav = findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }

            return insets;
        });

        // Navigation Items
        notificationBadge = findViewById(R.id.notificationBadge);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        tvChangePassword = findViewById(R.id.tvChangePassword);

        // Color 'ơ' in "HỒ SƠ" beige
        TextView tvProfileHeader = findViewById(R.id.tvProfileHeader);
        if (tvProfileHeader != null) {
            String headerText = tvProfileHeader.getText().toString();
            int oIndex = headerText.indexOf('ơ');
            if (oIndex == -1)
                oIndex = headerText.indexOf('Ơ');

            if (oIndex != -1) {
                SpannableString spannable = new SpannableString(headerText);
                spannable.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorBeigeBg)),
                        oIndex, oIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvProfileHeader.setText(spannable);
            }
        }

        // Fix large bitmap issue for profile avatar and logo
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar != null) {
            try {
                com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, R.drawable.model1);
            } catch (Exception ignored) {
            }
        }

        View centerActionButton = findViewById(R.id.centerActionButton);
        if (centerActionButton != null) {
            ImageView ivLogo = (ImageView) ((android.view.ViewGroup) centerActionButton).getChildAt(0);
            if (ivLogo != null) {
                try {
                    com.example.saive.utils.ImageUtils.setSafeImage(ivLogo, R.drawable.saive_logo);
                } catch (Exception ignored) {
                }
            }

            centerActionButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                navigateToMain("SHOW_HOME");
            });
        }
        SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);
        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                String message = isChecked ? getString(R.string.notifications_enabled)
                        : getString(R.string.notifications_disabled);
                showCustomToast(message);
            });
        }

        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            boolean isDarkMode = getSharedPreferences("theme_prefs", MODE_PRIVATE)
                    .getBoolean("dark_mode", false);
            switchDarkMode.setChecked(isDarkMode);

            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                getSharedPreferences("theme_prefs", MODE_PRIVATE)
                        .edit().putBoolean("dark_mode", isChecked).apply();
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        isChecked
                                ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                                : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
            });
        }

        // Logout Action
        btnLogoutNew = findViewById(R.id.btnLogoutNew);

        if (btnLogoutNew != null) {
            btnLogoutNew.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                com.example.saive.utils.UserSession session = com.example.saive.utils.UserSession.getInstance();

                if (session.isLoggedIn()) {
                    DialogUtils.showCustomAlertDialog(
                            this,
                            getString(R.string.logout_title),
                            getString(R.string.logout_message),
                            getString(R.string.yes),
                            getString(R.string.no),
                            () -> {
                                // Đăng xuất tổng thể: Firebase + xóa sạch Disk data (Orders, Cards, Address, Favorites)
                                session.logout(this);
                                showCustomToast(getString(R.string.toast_logged_out));
                                updateAuthUI();
                            });
                } else {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Notify Navigation Item
        View navNotify = findViewById(R.id.navNotify);
        if (navNotify != null) {
            navNotify.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                navigateToMain("SHOW_NOTIFICATIONS");
            });
        }

        // Wardrobe Navigation Item
        View navWardrobe = findViewById(R.id.navWardrobe);
        if (navWardrobe != null) {
            navWardrobe.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                navigateToMain("SHOW_WARDROBE");
            });
        }

        // Language Menu Item
        View btnLanguage = findViewById(R.id.btnLanguageNew);
        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showLanguageDialog();
            });
        }

        // ESG Research Menu Item
        View btnEsgResearch = findViewById(R.id.btnEsgResearch);
        if (btnEsgResearch != null) {
            btnEsgResearch.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProfileActivity.this, EsgActivity.class);
                startActivity(intent);
            });
        }

        // About SAIVE Menu Item
        View btnAbout = findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProfileActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }

        // FAQ Menu Item
        View btnFAQ = findViewById(R.id.btnFAQ);
        if (btnFAQ != null) {
            btnFAQ.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProfileActivity.this, FaqActivity.class);
                startActivity(intent);
            });
        }

        // Contact Us Menu Item
        View btnContactUs = findViewById(R.id.btnContactUs);
        if (btnContactUs != null) {
            btnContactUs.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(ProfileActivity.this, ContactUsActivity.class);
                startActivity(intent);
            });
        }

        // Delete Account Menu Item
        View btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                DialogUtils.showCustomAlertDialog(
                        this,
                        getString(R.string.dialog_delete_account_title),
                        getString(R.string.dialog_delete_account_message),
                        getString(R.string.dialog_delete_confirm),
                        getString(R.string.dialog_cancel),
                        () -> {
                            SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear(); // Clear all user data
                            editor.apply();

                            // Also clear address data
                            getSharedPreferences("address_prefs", MODE_PRIVATE).edit().clear().apply();

                            showCustomToast(getString(R.string.toast_account_deleted));
                            updateAuthUI();

                            // Chỉ updateAuth và quay về Main
                            updateAuthUI();
                        });
            });
        }

        // Shipping Address Menu Item
        View btnAddress = findViewById(R.id.btnAddressNew);
        if (btnAddress != null) {
            btnAddress.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    Intent intent = new Intent(ProfileActivity.this, AddressListActivity.class);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        // Payment Cards Menu Item
        View btnPaymentCards = findViewById(R.id.btnPaymentCards);
        if (btnPaymentCards != null) {
            btnPaymentCards.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    startActivity(new Intent(ProfileActivity.this, PaymentCardsActivity.class));
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        // Change Password Menu Item
        View btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        // My Orders Menu Item
        View btnMyOrders = findViewById(R.id.btnMyOrdersNew);
        if (btnMyOrders != null) {
            btnMyOrders.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    Intent intent = new Intent(ProfileActivity.this, MyOrdersActivity.class);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        // My Coupons Menu Item
        View btnMyCoupons = findViewById(R.id.btnMyCoupons);
        if (btnMyCoupons != null) {
            btnMyCoupons.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    Intent intent = new Intent(ProfileActivity.this, CouponActivity.class);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        // Saved Collections Menu Item
        View btnSavedCollections = findViewById(R.id.btnSavedCollections);
        if (btnSavedCollections != null) {
            btnSavedCollections.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                navigateToMain("SHOW_FAVORITES");
            });
        }

        // Favorite Navigation Item
        View navFavorite = findViewById(R.id.navFavorite);
        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                navigateToMain("SHOW_FAVORITES");
            });
        }

        // Edit Profile — bấm vào avatar hoặc tên
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }

        View tvUserName = findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            tvUserName.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                } else {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
            });
        }
    }
}