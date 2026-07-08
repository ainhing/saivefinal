package com.example.saive.admin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.adapters.BottomSheetOptionAdapter;
import com.example.saive.databinding.AdminActivityMainHostBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private AdminActivityMainHostBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("language_prefs", MODE_PRIVATE);
        String lang = prefs.getString("selected_language", "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        
        android.content.res.Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra session Admin
        com.example.saive.admin.util.SessionManager sessionManager = new com.example.saive.admin.util.SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, com.example.saive.admin.ui.login.LoginActivity.class));
            finish();
            return;
        }

        binding = AdminActivityMainHostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.adminToolbar;
        DrawerLayout drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.adminNavigationView;

        // Thiết lập sự kiện đổi ngôn ngữ trên Nav Header
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            View btnChangeLanguage = headerView.findViewById(R.id.btn_change_language);
            if (btnChangeLanguage != null) {
                btnChangeLanguage.setOnClickListener(v -> {
                    showLanguageDialog();
                });
            }
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Configure top-level destinations so they all show hamburger menu instead of back button
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.adminDashboard, R.id.adminOrders, R.id.adminProducts,
                    R.id.adminBlogs, R.id.adminReviews, R.id.adminUsers, R.id.adminEmployees)
                    .setOpenableLayout(drawerLayout)
                    .build();
            
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            
            // Intercept navigation drawer item clicks to handle custom actions like Logout
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.adminLogout) {
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, com.example.saive.admin.ui.login.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                
                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == id) {
                    drawerLayout.closeDrawers();
                    return true;
                }

                boolean handled = false;
                try {
                    navController.navigate(id);
                    handled = true;
                } catch (Exception e) {
                    handled = NavigationUI.onNavDestinationSelected(item, navController);
                }
                
                drawerLayout.closeDrawers();
                return handled;
            });
        }
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showLanguageDialog() {
        List<String> languages = Arrays.asList(getString(R.string.lang_en), getString(R.string.lang_vi),
                getString(R.string.lang_zh));
        List<String> langCodes = Arrays.asList("en", "vi", "zh");

        SharedPreferences prefs = getSharedPreferences("language_prefs", MODE_PRIVATE);
        String currentLang = prefs.getString("selected_language", "en");

        String currentLangName = getString(R.string.lang_en);
        if (currentLang.equals("vi"))
            currentLangName = getString(R.string.lang_vi);
        else if (currentLang.equals("zh"))
            currentLangName = getString(R.string.lang_zh);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvSheetTitle);
        if (tvTitle != null) {
            tvTitle.setText(R.string.menu_language);
        }

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
        SharedPreferences.Editor editor = getSharedPreferences("language_prefs", MODE_PRIVATE).edit();
        editor.putString("selected_language", langCode);
        editor.apply();

        Intent intent = new Intent(this, com.example.saive.ui.LanguageLoadingActivity.class);
        intent.putExtra("is_admin", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
