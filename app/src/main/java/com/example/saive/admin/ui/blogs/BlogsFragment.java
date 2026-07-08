package com.example.saive.admin.ui.blogs;

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
import com.example.saive.databinding.AdminFragmentBlogsBinding;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.saive.admin.data.model.AdminBlog;

/**
 * Quản lý Bài viết (Blogs) - Đầy đủ CRUD (R, C, U, D).
 */
public class BlogsFragment extends Fragment {
    private AdminFragmentBlogsBinding binding;
    private BlogsViewModel viewModel;
    private BlogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentBlogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BlogsViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();

        binding.fabAddBlog.setOnClickListener(v -> showCreateBlogDialog());

        viewModel.loadBlogs();
    }

    private void setupRecyclerView() {
        adapter = new BlogAdapter(blog -> {
            Bundle bundle = new Bundle();
            bundle.putString("blogId", blog.getBlogId());
            Navigation.findNavController(requireView()).navigate(R.id.action_blogs_to_detail, bundle);
        });
        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBlogs.setAdapter(adapter);
    }

    private void showCreateBlogDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_blog, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etSummary = dialogView.findViewById(R.id.etSummary);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        EditText etCoverImage = dialogView.findViewById(R.id.etCoverImage);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        MaterialCheckBox cbPublished = dialogView.findViewById(R.id.cbPublished);

        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo bài viết mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String author = etAuthor.getText().toString().trim();
                    String summary = etSummary.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String coverImage = etCoverImage.getText().toString().trim();
                    String tagsStr = etTags.getText().toString().trim();
                    boolean isPublished = cbPublished.isChecked();

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Tiêu đề không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AdminBlog blog = new AdminBlog();
                    blog.setTitle(title);
                    blog.setAuthor(author.isEmpty() ? "Admin" : author);
                    blog.setSummary(summary);
                    blog.setContent(content);
                    blog.setCoverImage(coverImage);
                    blog.setPublished(isPublished);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String dateStr = sdf.format(new Date());
                    blog.setCreatedAt(dateStr);
                    blog.setUpdatedAt(dateStr);

                    if (!tagsStr.isEmpty()) {
                        blog.setTags(Arrays.stream(tagsStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList()));
                    }

                    viewModel.createBlog(blog);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getBlogs().observe(getViewLifecycleOwner(), blogs -> {
            adapter.setBlogs(blogs);
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Đã tạo bài viết thành công!", Toast.LENGTH_SHORT).show();
                viewModel.clearCreateSuccess();
                viewModel.loadBlogs();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadBlogs());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}