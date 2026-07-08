package com.example.saive.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String name;
    
    @SerializedName(value = "ProductName_en", alternate = {"nameEn"})
    private String nameEn;

    @SerializedName(value = "ProductName_zh", alternate = {"nameZh"})
    private String nameZh;

    @SerializedName(value = "Description_en", alternate = {"descriptionEn"})
    private String descriptionEn;

    @SerializedName(value = "Description_zh", alternate = {"descriptionZh"})
    private String descriptionZh;
    private String price;
    private String originalPrice;
    private int imageResId;
    
    @SerializedName(value = "imageUrl", alternate = {"image", "img", "productImage", "thumbnail"})
    private String imageUrl;      // URL ảnh chính từ server

    @SerializedName(value = "imageUrls", alternate = {"images", "gallery", "imageList"})
    private java.util.List<String> imageUrls; // Danh sách các URL ảnh cho slideshow
    private java.util.List<Integer> imageResIds; // Danh sách các resource ID ảnh local
    private String productId;     // ID từ MongoDB
    private String category;
    private String description;
    private long timestamp;
    private boolean isFeatured;
    /** tag_type_group từ Firebase: top, bottom, dress, outerwear, shoes, bag, accessory */
    private String tagTypeGroup;

    /** tag_style từ Firebase: casual, formal, streetwear, minimalist, bohemian, ... */
    @SerializedName(value = "tagStyle", alternate = {"tag_style"})
    private String tagStyle;

    /** tag_color từ Firebase: danh sách màu sắc */
    @SerializedName(value = "tagColor", alternate = {"tag_color"})
    private List<String> tagColor;

    /** tag_type từ Firebase: chi tiết loại sản phẩm */
    @SerializedName(value = "tagType", alternate = {"tag_type"})
    private String tagType;

    @SerializedName("StockQuantity")
    private int stockQuantity;

    @SerializedName(value = "variantsStock", alternate = {"Variants", "Stock"})
    private java.util.Map<String, java.util.Map<String, Integer>> variantsStock;

    private int quantity = 1;
    private String selectedSize;
    private String selectedColor;

    public Product(String name, String price, int imageResId, String category) {
        this(name, price, null, imageResId, category);
    }

    public Product(String name, String price, String originalPrice, int imageResId, String category) {
        this(name, price, originalPrice, imageResId, category, "A study of form and function. This piece is crafted from premium materials with meticulous attention to detail.");
    }

    public Product(String name, String price, String originalPrice, int imageResId, String category, String description) {
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageResId = imageResId;
        this.category = category;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public Product(String name, String price, int imageResId, String category, String description) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.category = category;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getName() {
        String lang = java.util.Locale.getDefault().getLanguage();
        if ("en".equalsIgnoreCase(lang) && nameEn != null && !nameEn.isEmpty()) {
            return nameEn;
        } else if ("zh".equalsIgnoreCase(lang) && nameZh != null && !nameZh.isEmpty()) {
            return nameZh;
        }
        if (name != null && name.trim().startsWith("{") && name.trim().endsWith("}")) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(name);
                if (json.has(lang)) {
                    return json.getString(lang);
                } else if (json.has("en")) {
                    return json.getString("en");
                } else if (json.has("vi")) {
                    return json.getString("vi");
                }
            } catch (Exception ignored) { }
        }
        return name;
    }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls; }

    public java.util.List<Integer> getImageResIds() { return imageResIds; }
    public void setImageResIds(java.util.List<Integer> imageResIds) { this.imageResIds = imageResIds; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTagTypeGroup() { return tagTypeGroup; }
    public void setTagTypeGroup(String tagTypeGroup) { this.tagTypeGroup = tagTypeGroup; }

    public String getTagStyle() { return tagStyle; }
    public void setTagStyle(String tagStyle) { this.tagStyle = tagStyle; }

    public List<String> getTagColor() { return tagColor; }
    public void setTagColor(List<String> tagColor) { this.tagColor = tagColor; }

    public String getTagType() { return tagType; }
    public void setTagType(String tagType) { this.tagType = tagType; }

    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionZh() { return descriptionZh; }
    public void setDescriptionZh(String descriptionZh) { this.descriptionZh = descriptionZh; }

    public String getDescription() {
        String lang = java.util.Locale.getDefault().getLanguage();
        if ("en".equalsIgnoreCase(lang) && descriptionEn != null && !descriptionEn.isEmpty()) {
            return descriptionEn;
        } else if ("zh".equalsIgnoreCase(lang) && descriptionZh != null && !descriptionZh.isEmpty()) {
            return descriptionZh;
        }
        if (description != null && description.trim().startsWith("{") && description.trim().endsWith("}")) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(description);
                if (json.has(lang)) {
                    return json.getString(lang);
                } else if (json.has("en")) {
                    return json.getString("en");
                } else if (json.has("vi")) {
                    return json.getString("vi");
                }
            } catch (Exception ignored) { }
        }
        return description;
    }
    public void setDescription(String description) { this.description = description; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public java.util.Map<String, java.util.Map<String, Integer>> getVariantsStock() { return variantsStock; }
    public void setVariantsStock(java.util.Map<String, java.util.Map<String, Integer>> variantsStock) { this.variantsStock = variantsStock; }

    public int getStockForVariant(String size, String color) {
        if (variantsStock == null || variantsStock.isEmpty()) return stockQuantity;
        
        // If both are provided, get specific stock
        if (size != null && color != null) {
            // Case-insensitive size lookup
            for (java.util.Map.Entry<String, java.util.Map<String, Integer>> sizeEntry : variantsStock.entrySet()) {
                if (sizeEntry.getKey().equalsIgnoreCase(size)) {
                    java.util.Map<String, Integer> colorMap = sizeEntry.getValue();
                    if (colorMap != null) {
                        for (java.util.Map.Entry<String, Integer> colorEntry : colorMap.entrySet()) {
                            if (colorEntry.getKey().equalsIgnoreCase(color)) {
                                // Safely convert: Gson may deserialize integers as Double
                                Object val = colorEntry.getValue();
                                if (val instanceof Number) return ((Number) val).intValue();
                                try { return Integer.parseInt(String.valueOf(val)); } catch (Exception e) { return 0; }
                            }
                        }
                    }
                    return 0; // size found but color not found
                }
            }
            return 0; // size not found
        }
        
        // If only size is provided, sum all colors for that size
        if (size != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Integer>> sizeEntry : variantsStock.entrySet()) {
                if (sizeEntry.getKey().equalsIgnoreCase(size)) {
                    java.util.Map<String, Integer> colorMap = sizeEntry.getValue();
                    if (colorMap == null) return 0;
                    int sum = 0;
                    for (Object val : colorMap.values()) {
                        if (val instanceof Number) sum += ((Number) val).intValue();
                        else { try { sum += Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) {} }
                    }
                    return sum;
                }
            }
            return 0;
        }

        // If only color is provided, sum all sizes for that color
        if (color != null) {
            int sum = 0;
            for (java.util.Map<String, Integer> colorMap : variantsStock.values()) {
                if (colorMap == null) continue;
                for (java.util.Map.Entry<String, Integer> colorEntry : colorMap.entrySet()) {
                    if (colorEntry.getKey().equalsIgnoreCase(color)) {
                        Object val = colorEntry.getValue();
                        if (val instanceof Number) sum += ((Number) val).intValue();
                        else { try { sum += Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) {} }
                    }
                }
            }
            return sum;
        }

        // No size/color filter: sum everything
        int total = 0;
        for (java.util.Map<String, Integer> colorMap : variantsStock.values()) {
            if (colorMap == null) continue;
            for (Object val : colorMap.values()) {
                if (val instanceof Number) total += ((Number) val).intValue();
                else { try { total += Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) {} }
            }
        }
        return total > 0 ? total : stockQuantity;
    }


    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return name != null ? name.equals(product.name) : product.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
