package com.example.saive.admin.data.model;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.Exclude;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class AdminOrder {
    private String orderId; // Usually the key in Firebase

    @PropertyName("UserId")
    private String userId;

    @PropertyName("Email")
    private String email;

    @PropertyName("FullName")
    private String fullName;

    @PropertyName("Phone")
    private String phone;

    @PropertyName("ShippingAddress")
    private String shippingAddress;

    @PropertyName("Items")
    private List<AdminOrderItem> items;

    @PropertyName("SubTotal")
    private Object subTotal;

    @PropertyName("Discount")
    private Object discount;

    @PropertyName("ShippingFee")
    private Object shippingFee;

    @PropertyName("TotalAmount")
    private Object totalAmount;

    @PropertyName("PaymentMethod")
    private String paymentMethod;

    @PropertyName("Status")
    private String status;

    @PropertyName("Note")
    private String note;

    @PropertyName("CreatedAt")
    private Object createdAt;

    @PropertyName("UpdatedAt")
    private Object updatedAt;

    public AdminOrder() {
        // Required for Firebase
    }

    @PropertyName("OrderId")
    public String getOrderId() { return orderId; }
    @PropertyName("OrderId")
    public void setOrderId(String orderId) { this.orderId = orderId; }


    @PropertyName("UserId")
    public String getUserId() { return userId; }
    @PropertyName("UserId")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("Email")
    public String getEmail() { return email; }
    @PropertyName("Email")
    public void setEmail(Object email) { this.email = email == null ? null : String.valueOf(email); }

    @PropertyName("FullName")
    public String getFullName() { return fullName; }
    @PropertyName("FullName")
    public void setFullName(Object fullName) { this.fullName = fullName == null ? null : String.valueOf(fullName); }

    @PropertyName("Phone")
    public String getPhone() { return phone; }
    @PropertyName("Phone")
    public void setPhone(Object phone) { this.phone = phone == null ? null : String.valueOf(phone); }

    @PropertyName("ShippingAddress")
    public String getShippingAddress() { return shippingAddress; }
    @PropertyName("ShippingAddress")
    public void setShippingAddress(Object shippingAddress) { this.shippingAddress = shippingAddress == null ? null : String.valueOf(shippingAddress); }

    @PropertyName("Items")
    public List<AdminOrderItem> getItems() { return items; }

    @PropertyName("Items")
    public void setItems(Object itemsObj) {
        if (itemsObj == null) {
            this.items = null;
            return;
        }
        if (itemsObj instanceof List) {
            this.items = new ArrayList<>();
            for (Object obj : (List) itemsObj) {
                if (obj instanceof Map) {
                    AdminOrderItem item = parseItemFromMap((Map) obj);
                    if (item != null) {
                        this.items.add(item);
                    }
                } else if (obj instanceof AdminOrderItem) {
                    this.items.add((AdminOrderItem) obj);
                }
            }
        } else if (itemsObj instanceof Map) {
            this.items = new ArrayList<>();
            for (Object value : ((Map) itemsObj).values()) {
                if (value instanceof Map) {
                    AdminOrderItem item = parseItemFromMap((Map) value);
                    if (item != null) {
                        this.items.add(item);
                    }
                } else if (value instanceof AdminOrderItem) {
                    this.items.add((AdminOrderItem) value);
                }
            }
        }
    }

    private AdminOrderItem parseItemFromMap(Map map) {
        try {
            AdminOrderItem item = new AdminOrderItem();
            item.setProductId(String.valueOf(map.get("ProductId")));
            item.setProductName(String.valueOf(map.get("ProductName")));
            item.setSize(String.valueOf(map.get("Size")));
            item.setImage(String.valueOf(map.get("Image")));
            
            Object qtyObj = map.get("Quantity");
            if (qtyObj != null) {
                item.setQuantity(qtyObj);
            }
            
            Object priceObj = map.get("Price");
            if (priceObj != null) {
                item.setPrice(priceObj);
            }
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @PropertyName("SubTotal")
    public Object getSubTotalObj() { return subTotal; }
    @PropertyName("SubTotal")
    public void setSubTotal(Object subTotal) { this.subTotal = subTotal; }
    @Exclude
    public double getSubTotal() { return convertToDouble(subTotal); }

    @PropertyName("Discount")
    public Object getDiscountObj() { return discount; }
    @PropertyName("Discount")
    public void setDiscount(Object discount) { this.discount = discount; }
    @Exclude
    public double getDiscount() { return convertToDouble(discount); }

    @PropertyName("ShippingFee")
    public Object getShippingFeeObj() { return shippingFee; }
    @PropertyName("ShippingFee")
    public void setShippingFee(Object shippingFee) { this.shippingFee = shippingFee; }
    @Exclude
    public double getShippingFee() { return convertToDouble(shippingFee); }

    @PropertyName("TotalAmount")
    public Object getTotalAmountObj() { return totalAmount; }
    @PropertyName("TotalAmount")
    public void setTotalAmount(Object totalAmount) { this.totalAmount = totalAmount; }
    @Exclude
    public double getTotalAmount() { return convertToDouble(totalAmount); }

    private double convertToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @PropertyName("PaymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    @PropertyName("PaymentMethod")
    public void setPaymentMethod(Object paymentMethod) { this.paymentMethod = paymentMethod == null ? null : String.valueOf(paymentMethod); }

    @PropertyName("Status")
    public String getStatus() { return normalizeStatus(status); }
    @PropertyName("Status")
    public void setStatus(Object status) { this.status = normalizeStatus(status == null ? null : String.valueOf(status)); }

    @Exclude
    public static String normalizeStatus(String serverStatus) {
        if (serverStatus == null) return "pending";
        String s = serverStatus.toLowerCase(java.util.Locale.ROOT).trim();
        switch(s) {
            case "chờ xác nhận":
            case "pending":
                return "pending";
            case "đã xác nhận":
            case "confirmed":
            case "in progress":
            case "processing":
                return "confirmed";
            case "đang giao":
            case "shipping":
            case "shipped":
                return "shipping";
            case "đã giao":
            case "delivered":
            case "completed":
            case "finished":
                return "delivered";
            case "đã hủy":
            case "cancelled":
            case "canceled":
                return "cancelled";
            default:
                return s;
        }
    }

    @PropertyName("Note")
    public String getNote() { return note; }
    @PropertyName("Note")
    public void setNote(Object note) { this.note = note == null ? null : String.valueOf(note); }

    @PropertyName("CreatedAt")
    public Object getCreatedAt() { return createdAt; }
    @PropertyName("CreatedAt")
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    @PropertyName("UpdatedAt")
    public Object getUpdatedAt() { return updatedAt; }
    @PropertyName("UpdatedAt")
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }

    @Exclude
    public String getCreatedAtString() {
        if (createdAt instanceof String) return (String) createdAt;
        if (createdAt instanceof Long) {
             java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
             return sdf.format(new java.util.Date((Long) createdAt));
        }
        return "N/A";
    }
}
