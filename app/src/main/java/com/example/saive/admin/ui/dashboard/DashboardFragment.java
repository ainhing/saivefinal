package com.example.saive.admin.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.Dialog;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.saive.R;
import com.example.saive.admin.util.SessionManager;
import com.example.saive.databinding.AdminFragmentDashboardBinding;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private AdminFragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private SessionManager sessionManager;
    private String mWordCloudJson = "[]";
    private String mTreemapJson = "[]";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        sessionManager = new SessionManager(requireContext());

        binding.tvAdminName.setText(getString(R.string.admin_greeting) + " " + sessionManager.getDisplayName());

        setupObservers();
        setupListeners();

        viewModel.loadStats();
    }

    private void setupObservers() {
        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.tvTotalUsers.setText(String.valueOf(stats.getOrDefault("totalUsers", 0L)));
                binding.tvTotalOrders.setText(String.valueOf(stats.getOrDefault("totalOrders", 0L)));
                binding.tvTotalProducts.setText(String.valueOf(stats.getOrDefault("totalProducts", 0L)));
                binding.tvTotalBlogs.setText(String.valueOf(stats.getOrDefault("totalBlogs", 0L)));
                binding.tvTotalReviews.setText(String.valueOf(stats.getOrDefault("totalReviews", 0L)));
                
                Object revenue = stats.get("totalRevenue");
                if (revenue instanceof Number) {
                    binding.tvTotalRevenue.setText(formatPriceWithoutSymbol(((Number) revenue).doubleValue()));
                }

                // Review Topic Analytics
                long totalReviews = (long) stats.getOrDefault("totalReviews", 0L);
                long productCount = (long) stats.getOrDefault("reviewProductCount", 0L);
                long priceCount = (long) stats.getOrDefault("reviewPriceCount", 0L);
                long serviceCount = (long) stats.getOrDefault("reviewServiceCount", 0L);
                long deliveryCount = (long) stats.getOrDefault("reviewDeliveryCount", 0L);

                int percentProduct = 0;
                int percentPrice = 0;
                int percentService = 0;
                int percentDelivery = 0;

                if (totalReviews > 0) {
                    percentProduct = (int) Math.round((productCount * 100.0) / totalReviews);
                    percentPrice = (int) Math.round((priceCount * 100.0) / totalReviews);
                    percentService = (int) Math.round((serviceCount * 100.0) / totalReviews);
                    percentDelivery = (int) Math.round((deliveryCount * 100.0) / totalReviews);

                    percentProduct = Math.min(100, percentProduct);
                    percentPrice = Math.min(100, percentPrice);
                    percentService = Math.min(100, percentService);
                    percentDelivery = Math.min(100, percentDelivery);
                }

                binding.progressProduct.setProgress(percentProduct, true);
                binding.progressPrice.setProgress(percentPrice, true);
                binding.progressService.setProgress(percentService, true);
                binding.progressDelivery.setProgress(percentDelivery, true);

                binding.tvPercentProduct.setText(percentProduct + "% (" + productCount + ")");
                binding.tvPercentPrice.setText(percentPrice + "% (" + priceCount + ")");
                binding.tvPercentService.setText(percentService + "% (" + serviceCount + ")");
                binding.tvPercentDelivery.setText(percentDelivery + "% (" + deliveryCount + ")");

                mWordCloudJson = (String) stats.getOrDefault("wordCloudJson", "[]");
                mTreemapJson = (String) stats.getOrDefault("treemapJson", "[]");
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadStats());

        binding.btnProductsShortcut.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminProducts));
        binding.btnOrdersShortcut.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminOrders));
        binding.btnUsersShortcut.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminUsers));
        binding.btnBlogsShortcut.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminBlogs));
        binding.btnReviewsShortcut.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminReviews));

        binding.btnPendingOrders.setOnClickListener(v -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("defaultStatus", "pending");
            Navigation.findNavController(v).navigate(R.id.adminOrders, bundle);
        });

        binding.btnUnapprovedReviews.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.adminReviews);
        });

        binding.btnViewTopicChart.setOnClickListener(v -> showTopicAnalyticsDialog());
    }

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }

    private String formatPriceWithoutSymbol(double price) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }

    private void showTopicAnalyticsDialog() {
        if (getContext() == null) return;
        
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.admin_dialog_review_charts);
        
        WebView webView = dialog.findViewById(R.id.webViewCharts);
        ImageButton btnClose = dialog.findViewById(R.id.btnDialogClose);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public String getWordCloudData() {
                return mWordCloudJson;
            }
            
            @JavascriptInterface
            public String getTreemapData() {
                return mTreemapJson;
            }
        }, "Android");
        
        webView.loadUrl("file:///android_asset/review_analytics.html");
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
