package org.example.gestion_restaurant.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{8,15}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPrice(String priceStr) {
        try {
            double price = Double.parseDouble(priceStr);
            return price >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidQuantity(String quantityStr) {
        try {
            int quantity = Integer.parseInt(quantityStr);
            return quantity >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidTableNumber(String tableStr) {
        try {
            int table = Integer.parseInt(tableStr);
            return table > 0 && table <= 50; // Limite raisonnable
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return username != null && 
               username.length() >= 3 && 
               username.length() <= 50 &&
               username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"'&]", "");
    }

    public static boolean isValidProductName(String name) {
        return isNotEmpty(name) && name.length() <= 100;
    }

    public static boolean isValidCategory(String category) {
        return isNotEmpty(category) && category.length() <= 50;
    }
}