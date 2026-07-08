package com.example.saive.admin.ui.orders;

/**
 * Constants representing order statuses in English.
 * Database values are lowercase English to match the customer app:
 * ("pending", "confirmed", "shipping", "delivered", "cancelled").
 * UI values are capitalized English for display.
 */
public final class OrderStatus {
    // Database values
    public static final String PENDING = "pending";
    public static final String CONFIRMED = "confirmed";
    public static final String SHIPPING = "shipping";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";

    // UI values
    public static final String PENDING_UI = "Pending";
    public static final String CONFIRMED_UI = "Confirmed";
    public static final String SHIPPING_UI = "Shipping";
    public static final String DELIVERED_UI = "Delivered";
    public static final String CANCELLED_UI = "Cancelled";

    /** Order status array (database keys) */
    public static final String[] ALL = {
            PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    };

    /** Order status array (UI strings) */
    public static final String[] ALL_UI = {
            PENDING_UI, CONFIRMED_UI, SHIPPING_UI, DELIVERED_UI, CANCELLED_UI
    };

    /** Tab filter statuses (database keys), index matches tab layout */
    public static final String[] ALL_WITH_EMPTY = {
            "", PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    };

    /** Tab filter status display names (UI strings) */
    public static final String[] ALL_WITH_EMPTY_UI = {
            "All", PENDING_UI, CONFIRMED_UI, SHIPPING_UI, DELIVERED_UI, CANCELLED_UI
    };

    public static String getUIString(String dbStatus) {
        if (dbStatus == null) return PENDING_UI;
        switch(dbStatus.toLowerCase(java.util.Locale.ROOT).trim()) {
            case PENDING: return PENDING_UI;
            case CONFIRMED: return CONFIRMED_UI;
            case SHIPPING: return SHIPPING_UI;
            case DELIVERED: return DELIVERED_UI;
            case CANCELLED: return CANCELLED_UI;
            default: 
                if (dbStatus.length() > 0) {
                    return dbStatus.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + dbStatus.substring(1);
                }
                return dbStatus;
        }
    }

    private OrderStatus() {}
}