package com.example.saive.admin.ui.users;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.saive.databinding.AdminFragmentUsersBinding;
import com.example.saive.R;

/**
 * Quản lý Người dùng - CHỈ ĐỌC (Read-only).
 * Không cho phép thêm (Create), sửa (Update), xóa (Delete), hay block/unblock.
 * Bấm vào người dùng để xem chi tiết (read-only dialog).
 */
public class UsersFragment extends Fragment {
    private AdminFragmentUsersBinding binding;
    private UsersViewModel viewModel;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();

        viewModel.loadUsers("");
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this::showUserDetailDialog);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    /** Hiện thông tin chi tiết người dùng dạng dialog - chỉ xem, không sửa được. */
    private void showUserDetailDialog(com.example.saive.admin.data.model.AdminUser user) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.admin_dialog_user_detail, null);
        
        com.google.android.material.imageview.ShapeableImageView ivAvatar = dialogView.findViewById(R.id.ivAvatar);
        android.widget.TextView tvFullName = dialogView.findViewById(R.id.tvFullName);
        android.widget.TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        android.widget.TextView tvRole = dialogView.findViewById(R.id.tvRole);
        android.widget.TextView tvProvider = dialogView.findViewById(R.id.tvProvider);
        android.widget.TextView tvCreatedAt = dialogView.findViewById(R.id.tvCreatedAt);
        com.google.android.material.button.MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);

        tvFullName.setText(safe(user.getFullname()));
        tvEmail.setText(safe(user.getEmail()));
        
        // Capitalize role name for display
        String roleStr = safe(user.getRole());
        if (roleStr != null && roleStr.length() > 0) {
            roleStr = roleStr.substring(0, 1).toUpperCase() + roleStr.substring(1).toLowerCase();
        }
        tvRole.setText(roleStr);
        tvProvider.setText(safe(user.getProvider()));
        
        // Format date to YYYY-MM-DD
        String rawDate = user.getCreatedAt();
        if (rawDate != null && rawDate.length() >= 10) {
            tvCreatedAt.setText(rawDate.substring(0, 10));
        } else {
            tvCreatedAt.setText(safe(rawDate));
        }
        
        com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, user.getAvatarUrl(), R.drawable.model1);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private String safe(String value) {
        return (value == null || value.isEmpty()) ? "—" : value;
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.loadUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.setUsers(users);
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

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadUsers(binding.etSearch.getText().toString()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}