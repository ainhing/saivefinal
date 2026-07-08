package com.example.saive.admin.ui.blogs;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminBlog;
import com.example.saive.databinding.AdminFragmentBlogDetailBinding;
import com.example.saive.utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import com.google.android.material.checkbox.MaterialCheckBox;

/**
 * Xem chi tiết bài viết, cho phép Sửa / Xóa.
 */
public class BlogDetailFragment extends Fragment {
    private AdminFragmentBlogDetailBinding binding;
    private BlogDetailViewModel viewModel;
    private String blogId;
    private AdminBlog currentBlog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentBlogDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BlogDetailViewModel.class);

        if (getArguments() != null) {
            blogId = getArguments().getString("blogId");
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        setupToolbarMenu();

        viewModel.getBlog().observe(getViewLifecycleOwner(), blog -> {
            currentBlog = blog;
            bindBlogData(blog);
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Blog updated successfully!", Toast.LENGTH_SHORT).show();
                viewModel.clearUpdateSuccess();
            }
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Blog deleted successfully!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        if (blogId != null) {
            viewModel.loadBlog(blogId);
        }
    }

    private void setupToolbarMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_blog_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                if (currentBlog != null) {
                    showEditBlogDialog(currentBlog);
                }
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                confirmDeleteBlog();
                return true;
            }
            return false;
        });
    }

    private void showEditBlogDialog(AdminBlog blog) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_blog, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etSummary = dialogView.findViewById(R.id.etSummary);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        EditText etCoverImage = dialogView.findViewById(R.id.etCoverImage);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        MaterialCheckBox cbPublished = dialogView.findViewById(R.id.cbPublished);

        // Prepopulate
        etTitle.setText(blog.getTitle());
        etAuthor.setText(blog.getAuthor());
        etSummary.setText(blog.getSummary());
        etContent.setText(blog.getContent());
        etCoverImage.setText(blog.getCoverImage());
        if (blog.getTags() != null) {
            etTags.setText(String.join(", ", blog.getTags()));
        }
        cbPublished.setChecked(blog.isPublished());

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Blog")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String author = etAuthor.getText().toString().trim();
                    String summary = etSummary.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String coverImage = etCoverImage.getText().toString().trim();
                    String tagsStr = etTags.getText().toString().trim();
                    boolean isPublished = cbPublished.isChecked();

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("Title", title);
                    updates.put("Author", author);
                    updates.put("Summary", summary);
                    updates.put("Content", content);
                    updates.put("CoverImage", coverImage);
                    updates.put("IsPublished", isPublished);
                    
                    if (!tagsStr.isEmpty()) {
                        updates.put("Tags", Arrays.stream(tagsStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList()));
                    } else {
                        updates.put("Tags", null);
                    }

                    viewModel.updateBlog(blogId, updates);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteBlog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Blog")
                .setMessage("Are you sure you want to delete this blog?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (blogId != null) {
                        viewModel.deleteBlog(blogId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void bindBlogData(AdminBlog blog) {
        if (blog == null) return;

        binding.tvTitle.setText(blog.getTitle());
        binding.tvAuthor.setText("Author: " + (blog.getAuthor() != null ? blog.getAuthor() : "—"));
        binding.tvDate.setText(blog.getCreatedAt() != null ? blog.getCreatedAt() : "");
        binding.tvSummary.setText(blog.getSummary());
        binding.tvStatus.setText(blog.isPublished() ? "Published" : "Draft");
        binding.tvStatus.setTextColor(requireContext().getColor(
                blog.isPublished() ? android.R.color.holo_green_dark : R.color.colorSand));

        if (blog.getContent() != null) {
            binding.tvContent.setText(Html.fromHtml(blog.getContent(), Html.FROM_HTML_MODE_COMPACT));
        }

        if (blog.getTags() != null && !blog.getTags().isEmpty()) {
            binding.tvTags.setText(String.join(", ", blog.getTags()));
            binding.tvTags.setVisibility(View.VISIBLE);
        } else {
            binding.tvTags.setVisibility(View.GONE);
        }

        ImageUtils.setSafeImage(binding.ivCover, blog.getCoverImage(), R.drawable.model1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}