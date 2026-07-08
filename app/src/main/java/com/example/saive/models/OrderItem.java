package com.example.saive.models;

public class OrderItem implements java.io.Serializable {
    private String productId;
    private String name;
    private String size;
    private String color;
    private int quantity;
    private String price;
    private int imageResId;
    private String imageUrl;

    public OrderItem(String name, String size, String color, int quantity, String price, int imageResId, String imageUrl) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
    }

    public OrderItem(String name, String size, String color, int quantity, String price, int imageResId) {
        this(name, size, color, quantity, price, imageResId, null);
    }

    public OrderItem(String name, String size, int quantity, String price, int imageResId) {
        this(name, size, "—", quantity, price, imageResId, null);
    }

    public String getProductId() { return productId; }
    public String getName() { return name != null ? name : "Product"; }
    public String getSize() { return size != null ? size : "M"; }
    public String getColor() { return color != null ? color : "—"; }
    public int getQuantity() { return Math.max(1, quantity); }
    public String getPrice() { return price != null ? price : "0 ₫"; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }

    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setSize(String size) { this.size = size; }
    public void setColor(String color) { this.color = color; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(String price) { this.price = price; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
