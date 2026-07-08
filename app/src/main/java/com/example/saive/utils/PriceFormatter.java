package com.example.saive.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {
    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");

    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
        return formatter.format(price);
    }

    public static String formatPrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) return "0 ₫";
        
        // If it's already formatted, return it
        if (priceString.contains("₫") || priceString.contains("$")) return priceString;

        try {
            double price = Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
            return formatPrice(price);
        } catch (NumberFormatException e) {
            return priceString;
        }
    }
    
    /**
     * Attempts to parse a price string like "1.200.000 ₫" back to a double.
     */
    public static double parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) return 0;
        try {
            boolean isK = priceString.toUpperCase().contains("K");
            String cleanString = priceString.replaceAll("[^0-9]", "");
            if (cleanString.isEmpty()) return 0;
            double value = Double.parseDouble(cleanString);
            if (isK) value *= 1000;
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
