package com.example.saive.admin.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminOrder;
import com.example.saive.admin.data.model.AdminOrderItem;
import com.example.saive.databinding.AdminFragmentOrderDetailBinding;
import com.example.saive.databinding.ItemOrderDetailBinding;
import com.example.saive.utils.ImageUtils;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Chi tiết đơn hàng - R (xem thông tin) + U (đổi Status). Không có Delete.
 */
public class OrderDetailFragment extends Fragment {
    private AdminFragmentOrderDetailBinding binding;
    private OrderDetailViewModel viewModel;
    private String orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        setupToolbar();
        setupStatusSpinner();
        setupObservers();

        if (orderId != null) {
            viewModel.loadOrder(orderId);
            viewModel.loadOrderDetails(orderId);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, OrderStatus.ALL_UI);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spnStatus.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getOrder().observe(getViewLifecycleOwner(), this::bindOrderData);

        viewModel.getOrderItems().observe(getViewLifecycleOwner(), items -> {
            binding.llItemsContainer.removeAllViews();
            if (items != null) {
                for (com.example.saive.admin.data.model.AdminOrderItem item : items) {
                    ItemOrderDetailBinding itemBinding = ItemOrderDetailBinding.inflate(getLayoutInflater(), binding.llItemsContainer, false);
                    itemBinding.tvItemName.setText(item.getProductName());
                    itemBinding.tvItemAttributes.setText(getString(R.string.format_order_attributes, item.getSize(), item.getQuantity()));
                    itemBinding.tvItemPrice.setText(formatPrice(item.getPrice()));
                    ImageUtils.setSafeImage(itemBinding.ivItemImage, item.getImage(), R.drawable.model1);
                    binding.llItemsContainer.addView(itemBinding.getRoot());
                }
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), R.string.toast_update_success, Toast.LENGTH_SHORT).show();
                viewModel.clearUpdateSuccess();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            if (orderId == null) return;

            String newStatus = OrderStatus.ALL[binding.spnStatus.getSelectedItemPosition()];
            String note = binding.etNote.getText().toString();

            Map<String, Object> updates = new HashMap<>();
            updates.put("Status", newStatus);
            updates.put("Note", note);
            updates.put("UpdatedAt", getISO8601Timestamp());

            viewModel.updateOrder(orderId, updates);
        });
    }

    private void bindOrderData(AdminOrder order) {
        if (order == null) return;
        binding.tvOrderId.setText(getString(R.string.label_order_id_format, order.getOrderId()));
        binding.tvOrderDate.setText(getString(R.string.label_order_date_format, order.getCreatedAt()));
        binding.tvCustomerName.setText(order.getFullName());
        binding.tvCustomerEmail.setText(order.getEmail());
        binding.tvCustomerPhone.setText(order.getPhone());
        binding.tvShippingAddress.setText(order.getShippingAddress());
        binding.etNote.setText(order.getNote());

        // Chọn đúng vị trí trong spinner theo Status hiện tại của đơn hàng
        for (int i = 0; i < OrderStatus.ALL.length; i++) {
            if (OrderStatus.ALL[i].equals(order.getStatus())) {
                binding.spnStatus.setSelection(i);
                break;
            }
        }
        updateStatusUI(order.getStatus());
    }

    private void updateStatusUI(String status) {
        if (OrderStatus.CANCELLED.equals(status)) {
            binding.cardStatus.setCardBackgroundColor(requireContext().getColor(android.R.color.holo_red_light));
        } else if (OrderStatus.DELIVERED.equals(status)) {
            binding.cardStatus.setCardBackgroundColor(requireContext().getColor(android.R.color.holo_green_light));
        } else {
            binding.cardStatus.setCardBackgroundColor(requireContext().getColor(R.color.white));
        }
    }

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(price);
    }

    private String getISO8601Timestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}