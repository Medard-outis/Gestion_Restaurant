package org.example.gestion_restaurant.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class AlertUtils {

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Succès", message);
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Optional<String> showTextInput(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        return dialog.showAndWait();
    }

    public static Optional<String> showTextInput(String title, String message) {
        return showTextInput(title, message, "");
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthodes spécialisées pour l'application restaurant
    public static void showStockAlert(String productName, int currentStock) {
        if (currentStock == 0) {
            showError("Rupture de stock", 
                     String.format("Le produit '%s' est en rupture de stock!", productName));
        } else if (currentStock < 10) {
            showWarning("Stock faible", 
                       String.format("Le produit '%s' a un stock faible (%d unités restantes)", 
                                   productName, currentStock));
        }
    }

    public static boolean confirmDelete(String itemName) {
        return showConfirmation("Confirmer la suppression", 
                               String.format("Êtes-vous sûr de vouloir supprimer '%s' ?", itemName));
    }

    public static boolean confirmOrderCancellation(int orderNumber) {
        return showConfirmation("Annuler la commande", 
                               String.format("Êtes-vous sûr de vouloir annuler la commande #%d ?", orderNumber));
    }

    public static void showOrderSuccess(int orderNumber, double total) {
        showSuccess(String.format("Commande #%d créée avec succès!\nTotal: %.2f FC", orderNumber, total));
    }

    public static void showPaymentSuccess(int orderNumber, double amount) {
        showSuccess(String.format("Paiement de %.2f FC reçu pour la commande #%d", amount, orderNumber));
    }
}