package com.example.saive.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.adapters.ProductGridAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import android.widget.TextView;
import android.content.Intent;

public class SearchActivity extends BaseActivity {


    private EditText etSearch;
    private ImageView btnClear, btnBack;
    private RecyclerView rvSearchResults;
    private View searchSuggestions;
    private ProductGridAdapter adapter;
    private List<Product> allProducts;
    private TextView tvCartBadge;
    private View btnCart;
    private ChipGroup chipGroupRecent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        initViews();
        setupData();
        setupListeners();
        setupCartBadge();
        
        etSearch.requestFocus();
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
        etSearch = findViewById(R.id.etSearch);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        searchSuggestions = findViewById(R.id.searchSuggestions);
        chipGroupRecent = findViewById(R.id.chipGroupRecent);
        btnCart = findViewById(R.id.btnCart);

        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductGridAdapter(new ArrayList<>());
        rvSearchResults.setAdapter(adapter);

        View main = findViewById(R.id.main);
        if (main == null) main = findViewById(android.R.id.content);
        View header = findViewById(R.id.searchContainer);

        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), systemBars.top + (int)(12 * getResources().getDisplayMetrics().density),
                        header.getPaddingRight(), (int)(12 * getResources().getDisplayMetrics().density));
            }
            return insets;
        });
    }

    private void setupData() {
        allProducts = new ArrayList<>();
        List<Product> firebaseProducts = com.example.saive.utils.DataManager.getInstance(this).getProducts();
        if (firebaseProducts != null && !firebaseProducts.isEmpty()) {
            allProducts.addAll(firebaseProducts);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            finish();
        });
        
        btnClear.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            etSearch.setText("");
        });
        
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (chipGroupRecent != null) {
            for (int i = 0; i < chipGroupRecent.getChildCount(); i++) {
                View chip = chipGroupRecent.getChildAt(i);
                if (chip instanceof Chip) {
                    chip.setOnClickListener(v -> {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        String text = ((Chip) v).getText().toString();
                        etSearch.setText(text);
                        etSearch.setSelection(text.length());
                        performSearch(text);
                    });
                }
            }
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClear.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
                if (query.isEmpty()) {
                    showSuggestions();
                } else {
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        searchSuggestions.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);

        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase(java.util.Locale.getDefault()).contains(query.toLowerCase(java.util.Locale.getDefault())) 
                        || p.getCategory().toLowerCase(java.util.Locale.getDefault()).contains(query.toLowerCase(java.util.Locale.getDefault())))
                .collect(Collectors.toList());
        
        adapter.updateData(filtered);
    }

    private void showSuggestions() {
        searchSuggestions.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}