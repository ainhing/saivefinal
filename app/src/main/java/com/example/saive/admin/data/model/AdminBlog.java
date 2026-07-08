package com.example.saive.admin.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import java.util.List;

@IgnoreExtraProperties
public class AdminBlog {
    private String blogId;

    @PropertyName("Title")
    private String title;

    @PropertyName("Author")
    private String author;

    @PropertyName("Content")
    private String content;

    @PropertyName("Summary")
    private String summary;

    @PropertyName("Slug")
    private String slug;

    @PropertyName("Tags")
    private List<String> tags;

    @PropertyName("IsPublished")
    private boolean isPublished;

    @PropertyName("CreatedAt")
    private String createdAt;

    @PropertyName("UpdatedAt")
    private String updatedAt;

    @PropertyName("CoverImage")
    private String coverImage;

    public AdminBlog() {
        // Required for Firebase
    }

    public String getBlogId() { return blogId; }
    public void setBlogId(String blogId) { this.blogId = blogId; }

    @PropertyName("Title")
    public String getTitle() { return title; }
    @PropertyName("Title")
    public void setTitle(String title) { this.title = title; }

    @PropertyName("Author")
    public String getAuthor() { return author; }
    @PropertyName("Author")
    public void setAuthor(String author) { this.author = author; }

    @PropertyName("Content")
    public String getContent() { return content; }
    @PropertyName("Content")
    public void setContent(String content) { this.content = content; }

    @PropertyName("Summary")
    public String getSummary() { return summary; }
    @PropertyName("Summary")
    public void setSummary(String summary) { this.summary = summary; }

    @PropertyName("Slug")
    public String getSlug() { return slug; }
    @PropertyName("Slug")
    public void setSlug(String slug) { this.slug = slug; }

    @PropertyName("Tags")
    public List<String> getTags() { return tags; }
    @PropertyName("Tags")
    public void setTags(List<String> tags) { this.tags = tags; }

    @PropertyName("IsPublished")
    public boolean isPublished() { return isPublished; }
    @PropertyName("IsPublished")
    public void setPublished(boolean published) { isPublished = published; }

    @PropertyName("CreatedAt")
    public String getCreatedAt() { return createdAt; }
    @PropertyName("CreatedAt")
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @PropertyName("UpdatedAt")
    public String getUpdatedAt() { return updatedAt; }
    @PropertyName("UpdatedAt")
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @PropertyName("CoverImage")
    public String getCoverImage() { return coverImage; }
    @PropertyName("CoverImage")
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
}