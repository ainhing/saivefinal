package com.example.saive.models;

public class AdminOrder implements java.io.Serializable {
    private String orderId;
    private String customerName;
    private String itemsSummary;
    private String totalAmount;
    private String status;
    private String timeAgo;
    private int productImageResId;
    private String imageUrl;
    private String size;
    private String color;
    private int quantity;
    private String paymentMethod;
    private String shippingAddress;
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();
    private boolean isReviewed;

    public AdminOrder(String orderId, String customerName, String itemsSummary, String totalAmount, String status, String timeAgo) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.itemsSummary = itemsSummary != null ? itemsSummary : "Order";
        this.totalAmount = totalAmount != null ? totalAmount : "0";
        this.status = status != null ? status : "PENDING";
        this.timeAgo = timeAgo != null ? timeAgo : "Just now";
        this.productImageResId = 0;
        this.imageUrl = null;
        this.size = "L";
        this.color = "Black";
        this.quantity = 1;
        this.paymentMethod = "Momo";
        this.shippingAddress = "123 Le Loi, District 1, HCMC, Vietnam";
        this.isReviewed = false;
    }

    public AdminOrder(String orderId, String customerName, String itemsSummary, String totalAmount, String status, String timeAgo, int productImageResId, String imageUrl, String size, String color, int quantity, String paymentMethod, String shippingAddress) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.itemsSummary = itemsSummary != null ? itemsSummary : "Order";
        this.totalAmount = totalAmount != null ? totalAmount : "0";
        this.status = status != null ? status : "PENDING";
        this.timeAgo = timeAgo != null ? timeAgo : "Just now";
        this.productImageResId = productImageResId;
        this.imageUrl = imageUrl;
        this.size = size != null ? size : "L";
        this.color = color != null ? color : "Default";
        this.quantity = Math.max(1, quantity);
        this.paymentMethod = paymentMethod != null ? paymentMethod : "COD";
        this.shippingAddress = shippingAddress != null ? shippingAddress : "";
        this.isReviewed = false;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setItems(java.util.List<OrderItem> items) { this.items = items; }
    public java.util.List<OrderItem> getItems() {
        if (items == null) items = new java.util.ArrayList<>();
        return items;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getItemsSummary() { return itemsSummary != null ? itemsSummary : "Order Items"; }
    public void setItemsSummary(String itemsSummary) { this.itemsSummary = itemsSummary; }
    public String getTotalAmount() { return totalAmount != null ? totalAmount : "0"; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }
    public String getTimeAgo() { return timeAgo != null ? timeAgo : ""; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public String getSize() { return size != null ? size : "M"; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color != null ? color : "Default"; }
    public void setColor(String color) { this.color = color; }
    public int getQuantity() { return Math.max(1, quantity); }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "COD"; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getShippingAddress() { return shippingAddress != null ? shippingAddress : ""; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public int getProductImageResId() { return productImageResId; }
    public void setProductImageResId(int productImageResId) { this.productImageResId = productImageResId; }

    public static String normalizeStatus(String serverStatus) {
        if (serverStatus == null) return "pending";
        String status = serverStatus.toLowerCase(java.util.Locale.ROOT).trim();
        switch(status) {
            case "pending": 
                return "pending";
            case "confirmed": 
            case "in progress": 
            case "processing":
                return "confirmed";
            case "shipping": 
            case "shipped": 
                return "shipping";
            case "delivered": 
            case "completed": 
            case "finished":
                return "delivered";
            case "cancelled": 
            case "canceled":
                return "cancelled";
            default: 
                return status;
        }
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.isReviewed = reviewed;
    }
}