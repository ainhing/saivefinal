package com.example.saive.adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.Notification;
import com.example.saive.utils.ImageUtils;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private static final String PREFS_NAME = "notification_prefs";

    public interface OnNotificationClickListener {
        void onNotificationClick();
    }

    private OnNotificationClickListener listener;

    public NotificationAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        // Sync with SharedPreferences
        SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences(PREFS_NAME, 0);
        boolean isRead = prefs.getBoolean("read_" + notification.getId(), notification.isRead());
        notification.setRead(isRead);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvDesc.setText(notification.getDescription());
        holder.tvAction.setText(notification.getActionLabel());
        holder.tvTime.setText(notification.getTime());
        ImageUtils.setSafeImage(holder.ivCategory, notification.getIconResId());
        // Remove color filter to allow full-color images (like mipmap) to show properly
        holder.ivCategory.clearColorFilter();
        
        holder.iconContainer.getBackground().setTint(notification.getIconBgColor());
        
        if (notification.isRead()) {
            holder.rootView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorReadBg));
            holder.unreadDot.setVisibility(View.GONE);
        } else {
            holder.rootView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorUnreadBg));
            holder.unreadDot.setVisibility(View.VISIBLE);
            holder.unreadDot.getBackground().setTint(notification.getDotColor());
        }

        holder.tvAction.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            handleNotificationAction(v.getContext(), notification);
            
            if (!notification.isRead()) {
                notification.setRead(true);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("read_" + notification.getId(), true);
                editor.apply();
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onNotificationClick();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.98f).scaleY(0.98f).alpha(0.8f).setDuration(80).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(150);
                
                Notification.Type type = notification.getType();
                if (type == Notification.Type.DROP) {
                    // Open Detail Activity (Bảng tin)
                    android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.saive.ui.NotificationDetailActivity.class);
                    intent.putExtra("id", notification.getId());
                    intent.putExtra("title", notification.getTitle());
                    intent.putExtra("time", notification.getTime());
                    intent.putExtra("desc", notification.getDescription());
                    intent.putExtra("action", notification.getActionLabel());
                    intent.putExtra("icon", notification.getIconResId());
                    intent.putExtra("type", type.name());
                    v.getContext().startActivity(intent);
                } else {
                    // Direct navigation for Order, Wardrobe items, and Capsule suggestions
                    handleNotificationAction(v.getContext(), notification);
                }

                if (!notification.isRead()) {
                    notification.setRead(true);
                    // Persist state
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("read_" + notification.getId(), true);
                    editor.apply();

                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onNotificationClick();
                    }
                }
            });
        });
    }

    private void handleNotificationAction(android.content.Context context, Notification notification) {
        Notification.Type type = notification.getType();
        android.content.Intent intent;

        if (type == Notification.Type.REMINDER) {
            // Jump to Cart
            intent = new android.content.Intent(context, com.example.saive.ui.CartActivity.class);
        } else if (type == Notification.Type.DROP) {
            // Jump to Collection
            intent = new android.content.Intent(context, com.example.saive.ui.CollectionDetailActivity.class);
            intent.putExtra("COLLECTION_TITLE", notification.getTitle());
        } else if (type == Notification.Type.CAPSULE) {
            // Jump to Wardrobe
            intent = new android.content.Intent(context, com.example.saive.ui.MainActivity.class);
            intent.putExtra("SHOW_WARDROBE", true);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (type == Notification.Type.ORDER) {
            // Jump to My Orders list
            intent = new android.content.Intent(context, com.example.saive.ui.MyOrdersActivity.class);
        } else {
            // Default to Shop (Home)
            intent = new android.content.Intent(context, com.example.saive.ui.MainActivity.class);
            intent.putExtra("SHOW_HOME", true);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvAction, tvTime;
        ImageView ivCategory;
        View unreadDot, iconContainer, rootView, underlineAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvAction = itemView.findViewById(R.id.tv_action);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivCategory = itemView.findViewById(R.id.iv_category);
            unreadDot = itemView.findViewById(R.id.unread_dot);
            iconContainer = itemView.findViewById(R.id.icon_container);
            rootView = itemView.findViewById(R.id.root_notification);
            underlineAction = itemView.findViewById(R.id.underline_action);
        }
    }
}
