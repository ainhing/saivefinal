package com.example.saive.admin.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Maps 1:1 to the "Products" node.
 * Using PascalCase field names to match Firebase automatically, except
 * tag_style / tag_type / tag_color which are snake_case in the DB.
 */
@IgnoreExtraProperties
public class AdminProduct {
    @Exclude
    private String productId;

    public String CategoryId;
    public String ProductName;
    public Object OriginalPrice;
    public Object Price;
    public Object Images; // List or Map
    public String Description;
    public Object Rating;
    public Object IsActive;
    public Object IsFeatured;
    public String CreatedAt;
    public String UpdatedAt;
    public Object NumBuy;

    // Real DB node: Stock -> { size -> { color -> quantity } }
    public Object Stock;

    public String tag_style;
    public String tag_type;
    public Object tag_color;

    public AdminProduct() {
        // Required for Firebase
    }

    @Exclude
    public String getProductId() { return productId; }
    @Exclude
    public void setProductId(String productId) { this.productId = productId; }

    @Exclude
    public String getProductName() { return ProductName != null ? ProductName : ""; }

    @Exclude
    public String getCategoryId() { return CategoryId; }

    @Exclude
    public String getDescription() { return Description != null ? Description : ""; }

    @Exclude
    public double getPrice() { return convertToDouble(Price); }

    @Exclude
    public double getOriginalPrice() { return convertToDouble(OriginalPrice); }

    @Exclude
    public double getRating() { return convertToDouble(Rating); }

    @Exclude
    public int getNumBuy() { return (int) convertToDouble(NumBuy); }

    @Exclude
    public boolean isFeatured() {
        if (IsFeatured instanceof Boolean) return (Boolean) IsFeatured;
        if (IsFeatured instanceof String) return "true".equalsIgnoreCase((String) IsFeatured);
        return false;
    }

    @Exclude
    public String getTagStyle() { return tag_style; }

    @Exclude
    public String getTagType() { return tag_type; }

    @Exclude
    public List<String> getImages() {
        List<String> list = new ArrayList<>();
        if (Images instanceof List) {
            for (Object item : (List) Images) if (item instanceof String) list.add((String) item);
        } else if (Images instanceof Map) {
            for (Object value : ((Map) Images).values()) if (value instanceof String) list.add((String) value);
        }
        return list;
    }

    @Exclude
    public String getFirstImage() {
        List<String> imgs = getImages();
        return !imgs.isEmpty() ? imgs.get(0) : null;
    }

    @Exclude
    public boolean isActive() {
        if (IsActive instanceof Boolean) return (Boolean) IsActive;
        if (IsActive instanceof String) return "true".equalsIgnoreCase((String) IsActive);
        return true;
    }

    /**
     * Real stock is nested: Stock/{size}/{color} = quantity.
     * This sums every size/color combo into a single total, so it matches
     * what's actually in the database (old StockQuantity field never existed).
     */
    @Exclude
    public int getTotalStock() {
        if (!(Stock instanceof Map)) return 0;
        int total = 0;
        for (Object sizeVal : ((Map<?, ?>) Stock).values()) {
            if (!(sizeVal instanceof Map)) continue;
            for (Object qty : ((Map<?, ?>) sizeVal).values()) {
                total += (int) convertToDouble(qty);
            }
        }
        return total;
    }

    /**
     * Optional: human-readable breakdown, e.g. "S-nâu: 93, S-vàng: 87, M-đen: 60".
     * Handy if you want to show per size/color detail instead of just the total.
     */
    @Exclude
    public String getStockBreakdown() {
        if (!(Stock instanceof Map)) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> sizeEntry : ((Map<?, ?>) Stock).entrySet()) {
            if (!(sizeEntry.getValue() instanceof Map)) continue;
            for (Map.Entry<?, ?> colorEntry : ((Map<?, ?>) sizeEntry.getValue()).entrySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(sizeEntry.getKey()).append("-").append(colorEntry.getKey())
                        .append(": ").append((int) convertToDouble(colorEntry.getValue()));
            }
        }
        return sb.toString();
    }

    private double convertToDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try { return Double.parseDouble(((String) obj).replaceAll("[^0-9.]", "")); }
            catch (Exception e) { return 0.0; }
        }
        return 0.0;
    }
}