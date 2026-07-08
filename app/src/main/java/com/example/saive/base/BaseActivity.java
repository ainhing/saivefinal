package com.example.saive.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saive.R;
import com.example.saive.ui.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Locale;

import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String LANG_PREFS = "language_prefs";
    protected static final String LANG_KEY = "selected_language";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure status bar is consistent across all activities
        if (getWindow() != null) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorCotton));
            
            boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            
            View decorView = getWindow().getDecorView();
            if (isDarkMode) {
                // Remove light status bar flag in dark mode to show white icons
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                // Set light status bar flag in light mode to show dark icons
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    protected void navigateToMain(String extra) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(extra, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void navigateToProfile() {
        Intent intent = new Intent(this, com.example.saive.ui.ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @android.annotation.SuppressLint("InflateParams")
    public void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View toastRoot = inflater.inflate(R.layout.layout_custom_toast, null);

        TextView text = toastRoot.findViewById(R.id.toast_text);
        if (text != null) {
            text.setText(message);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastRoot);
        toast.show();
    }

    @android.annotation.SuppressLint("InflateParams")
    public void showSizeGuideDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_size_guide, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

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
}