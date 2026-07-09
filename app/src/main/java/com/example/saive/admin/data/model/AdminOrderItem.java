package com.example.saive.admin.data.model;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.Exclude;

public class AdminOrderItem {
    @PropertyName("ProductId")
    private String productId;
    
    @PropertyName("ProductName")
    private String productName;
    
    @PropertyName("Size")
    private String size;
    
    @PropertyName("Quantity")
    private Object quantity;
    
    @PropertyName("Price")
    private Object price;
    
    @PropertyName("Image")
    private String image;

    public AdminOrderItem() {
        // Required for Firebase
    }

    @PropertyName("ProductId")
    public String getProductId() { return productId; }
    
    @PropertyName("ProductId")
    public void setProductId(String productId) { this.productId = productId; }

    @PropertyName("ProductName")
    public String getProductName() { return productName; }
    
    @PropertyName("ProductName")
    public void setProductName(String productName) { this.productName = productName; }

    @PropertyName("Size")
    public String getSize() { return size; }
    
    @PropertyName("Size")
    public void setSize(String size) { this.size = size; }

    @PropertyName("Quantity")
    public Object getQuantityObj() { return quantity; }
    
    @PropertyName("Quantity")
    public void setQuantity(Object quantity) { this.quantity = quantity; }
    
    @Exclude
    public int getQuantity() {
        if (quantity instanceof Number) {
            return ((Number) quantity).intValue();
        } else if (quantity instanceof String) {
            try {
                return Integer.parseInt((String) quantity);
            } catch (Exception e) {
                return 1;
            }
        }
        return 1;
    }

    @PropertyName("Price")
    public Object getPriceObj() { return price; }
    
    @PropertyName("Price")
    public void setPrice(Object price) { this.price = price; }
    
    @Exclude
    public double getPrice() {
        if (price instanceof Number) {
            return ((Number) price).doubleValue();
        } else if (price instanceof String) {
            try {
                return Double.parseDouble((String) price);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @PropertyName("Image")
    public String getImage() { return image; }
    
    @PropertyName("Image")
    public void setImage(String image) { this.image = image; }

    @PropertyName("OrderId")
    private String orderId;

    @PropertyName("OrderId")
    public String getOrderId() { return orderId; }

    @PropertyName("OrderId")
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @PropertyName("UnitPrice")
    public Object getUnitPriceObj() { return price; }

    @PropertyName("UnitPrice")
    public void setUnitPrice(Object unitPrice) { this.price = unitPrice; }
}
