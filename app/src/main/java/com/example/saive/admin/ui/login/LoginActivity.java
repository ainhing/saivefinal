package com.example.saive.admin.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.saive.R;
import com.example.saive.databinding.AdminActivityLoginBinding;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private AdminActivityLoginBinding binding;
    private LoginViewModel viewModel;

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
        binding = AdminActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Pre-fill credentials
        binding.etEmail.setText("thuy.dao@saive.com");
        binding.etPassword.setText("admin123");

        setupObservers();

        binding.btnLoginAdmin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.contact_error_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                // Navigate to MainActivity (Admin version)
                Intent intent = new Intent(LoginActivity.this, com.example.saive.admin.ui.MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
