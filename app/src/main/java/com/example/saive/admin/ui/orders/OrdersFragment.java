package com.example.saive.admin.ui.orders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.saive.R;
import com.example.saive.databinding.AdminFragmentOrdersBinding;
import com.google.android.material.tabs.TabLayout;

/**
 * Quản lý Đơn hàng - R (xem danh sách + chi tiết), U (đổi Status).
 * Không có Create / Delete.
 * QUAN TRỌNG: các giá trị Status trong Firebase là TIẾNG VIỆT
 * ("Chờ xác nhận", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy"),
 * nên các tab filter ở đây phải dùng đúng các chuỗi này (không dùng tiếng Anh).
 */
public class OrdersFragment extends Fragment {
    private AdminFragmentOrdersBinding binding;
    private OrdersViewModel viewModel;
    private OrderAdapter adapter;
    private String currentStatus = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);

        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupObservers();

        String defaultStatus = "";
        if (getArguments() != null) {
            defaultStatus = getArguments().getString("defaultStatus", "");
        }

        if (!defaultStatus.isEmpty()) {
            for (int i = 0; i < OrderStatus.ALL_WITH_EMPTY.length; i++) {
                if (OrderStatus.ALL_WITH_EMPTY[i].equalsIgnoreCase(defaultStatus)) {
                    TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                    if (tab != null) {
                        tab.select();
                        break;
                    }
                }
            }
        } else {
            viewModel.loadOrders("", "");
        }
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter((v, order) -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", order.getOrderId());
            Navigation.findNavController(v).navigate(R.id.action_orders_to_detail, bundle);
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String[] statusKeys = OrderStatus.ALL_WITH_EMPTY;
                currentStatus = statusKeys[tab.getPosition()];
                viewModel.loadOrders(currentStatus, binding.etSearch.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.loadOrders(currentStatus, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            adapter.setOrders(orders);
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() ->
                viewModel.loadOrders(currentStatus, binding.etSearch.getText().toString())
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}