package com.example.saive.admin.ui.reviews;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminReview;
import com.example.saive.databinding.AdminFragmentReviewsBinding;
import com.google.android.material.tabs.TabLayout;

/**
 * Quản lý Reviews - R (xem theo tab: Chờ duyệt / Đã duyệt / Đã từ chối) + U (đổi Status).
 * Không có Create / Delete thật sự (chỉ đổi Status, dữ liệu vẫn còn trong Firebase).
 * Swipe PHẢI (sang phải) = Duyệt (approved).
 * Swipe TRÁI (sang trái) = Từ chối (rejected).
 * Thiết kế tối ưu để dùng 1 tay trên mobile, không cần nút bấm nhỏ.
 */
public class ReviewsFragment extends Fragment {
    private AdminFragmentReviewsBinding binding;
    private ReviewsViewModel viewModel;
    private ReviewAdapter adapter;
    private String currentStatus = "pending";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentReviewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReviewsViewModel.class);

        setupRecyclerView();
        setupSwipeActions();
        setupTabs();
        setupObservers();

        viewModel.loadReviews(currentStatus);
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(adapter);
    }

    /** Gắn ItemTouchHelper để vuốt trái/phải đổi trạng thái review - dễ dùng trên mobile. */
    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                AdminReview review = adapter.getReviewAt(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right -> Approve
                    viewModel.updateStatus(review.getReviewId(), "approved", currentStatus);
                    Toast.makeText(getContext(), "Review approved", Toast.LENGTH_SHORT).show();
                } else {
                    // Swipe left -> Reject
                    viewModel.updateStatus(review.getReviewId(), "rejected", currentStatus);
                    Toast.makeText(getContext(), "Review rejected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                android.content.Context context = recyclerView.getContext();

                if (dX > 0) {
                    // Swipe right: green background "Approve"
                    paint.setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    c.drawRect(new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom()), paint);
                    drawLabel(c, itemView, "APPROVE", true, dX);
                } else if (dX < 0) {
                    // Swipe left: red background "Reject"
                    paint.setColor(ContextCompat.getColor(context, R.color.colorMaroon));
                    c.drawRect(new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom()), paint);
                    drawLabel(c, itemView, "REJECT", false, dX);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            private void drawLabel(Canvas c, View itemView, String text, boolean isRight, float dX) {
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(36f);
                textPaint.setAntiAlias(true);
                textPaint.setFakeBoldText(true);
                float textWidth = textPaint.measureText(text);
                float padding = 48f;
                float x = isRight
                        ? itemView.getLeft() + padding
                        : itemView.getRight() - padding - textWidth;
                float y = itemView.getTop() + (itemView.getHeight() / 2f) + (textPaint.getTextSize() / 3f);
                if (Math.abs(dX) > textWidth + padding * 2) {
                    c.drawText(text, x, y, textPaint);
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvReviews);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String[] statusKeys = {"pending", "approved", "rejected"};
                currentStatus = statusKeys[tab.getPosition()];
                viewModel.loadReviews(currentStatus);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupObservers() {
        viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            adapter.setReviews(reviews);
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

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadReviews(currentStatus));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}