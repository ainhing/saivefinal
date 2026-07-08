package com.example.saive.models;

public class Coupon {
    private String title;
    private String description;
    private String descriptionEn;
    private String descriptionZh;
    private String discount;
    private String expiryDate;
    private String code;
    private String status; // "Active", "Expired", "Scheduled"
    private String type;   // "Percentage", "Fixed", "FreeShipping"
    private int usageCount;

    public Coupon(String title, String description, String discount, String expiryDate, String code) {
        this(title, description, discount, expiryDate, code, "Active", 0, "Percentage");
    }

    public Coupon(String title, String description, String discount, String expiryDate, String code, String status, int usageCount) {
        this(title, description, discount, expiryDate, code, status, usageCount, "Percentage");
    }

    public Coupon(String title, String description, String discount, String expiryDate, String code, String status, int usageCount, String type) {
        this.title = title;
        this.description = description;
        this.discount = discount;
        this.expiryDate = expiryDate;
        this.code = code;
        this.status = status;
        this.usageCount = usageCount;
        this.type = type;
    }

    public String getTitle() { return title; }
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
        return description;
    }
    public String getDiscount() { return discount; }
    public String getExpiryDate() { return expiryDate; }
    public String getCode() { return code; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public int getUsageCount() { return usageCount; }
}
