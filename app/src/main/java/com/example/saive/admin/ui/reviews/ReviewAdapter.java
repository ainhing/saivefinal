package com.example.saive.admin.ui.reviews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.admin.data.model.AdminReview;
import com.example.saive.databinding.AdminItemReviewBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách Reviews.
 * R: hiển thị nội dung đánh giá.
 * U: đổi Status (approve/reject) qua swipe trái/phải (xử lý bởi ItemTouchHelper trong Fragment).
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<AdminReview> reviews = new ArrayList<>();

    public void setReviews(List<AdminReview> newReviews) {
        this.reviews = newReviews != null ? newReviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public AdminReview getReviewAt(int position) {
        return reviews.get(position);
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemReviewBinding binding = AdminItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemReviewBinding binding;

        public ReviewViewHolder(AdminItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminReview review) {
            binding.tvUserName.setText("CUSTOMER: " + review.getCustomerId());
            binding.tvDate.setText(formatDate(review.getCreatedAt()));
            binding.ratingBar.setRating(review.getRating());
            binding.tvComment.setText(review.getContent());
            
            String prodId = review.getProductId();
            String prodName = review.getProductName();
            binding.tvProductId.setVisibility(View.VISIBLE);
            binding.tvProductId.setText("PRODUCT: " + prodId + (prodName != null && !prodName.equals(prodId) ? " (" + prodName + ")" : ""));

            binding.tvLikeCount.setText(review.getLikecount() + " Likes");
            
            String status = review.getEffectiveStatus();
            binding.tvStatusBadge.setText(status.toUpperCase());
            
            // Reusing existing badge background
            binding.tvStatusBadge.setBackgroundResource(com.example.saive.R.drawable.badge_background_sand);
        }

        private String formatDate(String iso) {
            if (iso == null || iso.isEmpty()) return "";
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = input.parse(iso.length() >= 19 ? iso.substring(0, 19) : iso);
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return date != null ? output.format(date) : iso;
            } catch (Exception e) {
                return iso;
            }
        }
    }
}