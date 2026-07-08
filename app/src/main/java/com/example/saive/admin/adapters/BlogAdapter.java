package com.example.saive.admin.ui.blogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminBlog;
import com.example.saive.databinding.AdminItemBlogBinding;
import com.example.saive.utils.ImageUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách Blogs - CHỈ ĐỌC (Read-only).
 */
public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {
    private List<AdminBlog> blogs = new ArrayList<>();
    private final OnBlogClickListener listener;

    public interface OnBlogClickListener {
        void onBlogClick(AdminBlog blog);
    }

    public BlogAdapter(OnBlogClickListener listener) {
        this.listener = listener;
    }

    public void setBlogs(List<AdminBlog> newBlogs) {
        this.blogs = newBlogs != null ? newBlogs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemBlogBinding binding = AdminItemBlogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BlogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        holder.bind(blogs.get(position));
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemBlogBinding binding;

        public BlogViewHolder(AdminItemBlogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminBlog blog) {
            binding.tvBlogTitle.setText(blog.getTitle());
            binding.tvBlogAuthor.setText(blog.getAuthor());
            binding.tvBlogStatus.setText(blog.isPublished() ? "Đã xuất bản" : "Bản nháp");
            binding.tvBlogStatus.setTextColor(binding.getRoot().getContext().getResources().getColor(
                    blog.isPublished() ? android.R.color.holo_green_dark : R.color.colorSand));

            ImageUtils.setSafeImage(binding.ivBlogCover, blog.getCoverImage(), R.drawable.model1);

            itemView.setOnClickListener(v -> listener.onBlogClick(blog));
        }
    }
}