package com.example.saive.models;

import java.util.List;

public class CollectionItem {
    private String name;
    private String tag;
    private List<Integer> images; // List of image resource IDs for collage
    private int color;

    public CollectionItem(String name, String tag, List<Integer> images, int color) {
        this.name = name;
        this.tag = tag;
        this.images = images;
        this.color = color;
    }

    public String getName() { return name; }
    public String getTag() { return tag; }
    public List<Integer> getImages() { return images; }
    public int getColor() { return color; }
}