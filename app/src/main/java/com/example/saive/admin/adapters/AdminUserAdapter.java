package com.example.saive.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminUser;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onBlockToggle(AdminUser user, int position);
    }

    private List<AdminUser> userList;
    private OnUserActionListener listener;

    public AdminUserAdapter(List<AdminUser> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AdminUser user = userList.get(position);
        holder.tvUserName.setText(user.getFullname());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserRole.setText(user.getRole() != null ? user.getRole().toUpperCase() : "USER");

        if (!user.isActive()) {
            holder.btnBlockUser.setImageResource(R.drawable.ic_check_circle); 
            holder.btnBlockUser.setColorFilter(Color.GRAY);
            holder.tvUserName.setTextColor(Color.GRAY);
        } else {
            holder.btnBlockUser.setImageResource(R.drawable.ic_notifications); 
            holder.btnBlockUser.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMaroon));
            holder.tvUserName.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNoirBlack));
        }

        holder.btnBlockUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBlockToggle(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void updateList(List<AdminUser> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole;
        ImageView btnBlockUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnBlockUser = itemView.findViewById(R.id.btnBlockUser);
        }
    }
}