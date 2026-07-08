package com.example.saive.models;

public class WardrobeBanner {
    private String caption;
    private String title;
    private String action;
    private int imageResId;

    public WardrobeBanner(String caption, String title, String action, int imageResId) {
        this.caption = caption;
        this.title = title;
        this.action = action;
        this.imageResId = imageResId;
    }

    public String getCaption() { return caption; }
    public String getTitle() { return title; }
    public String getAction() { return action; }
    public int getImageResId() { return imageResId; }
}