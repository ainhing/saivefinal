package com.example.saive.admin.ui.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminUser;
import com.example.saive.databinding.AdminItemUserBinding;
import com.example.saive.utils.ImageUtils;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<AdminUser> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(AdminUser user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<AdminUser> newUsers) {
        this.users = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemUserBinding binding = AdminItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemUserBinding binding;

        public UserViewHolder(AdminItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminUser user) {
            // null-safe: Firebase fields may be absent for some users
            String name = user.getFullname();
            String email = user.getEmail();
            String role = user.getRole();

            binding.tvUserName.setText(name != null && !name.isEmpty() ? name : "Unknown");
            binding.tvUserEmail.setText(email != null && !email.isEmpty() ? email : "—");
            binding.tvUserRole.setText(role != null && !role.isEmpty() ? role.toUpperCase() : "CUSTOMER");

            ImageUtils.setSafeImage(binding.ivUserAvatar, user.getAvatarUrl(), R.drawable.ic_profile);

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}