package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import services.FactureService;
import services.ProductService;
import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import models.Product;

public class HomeDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label dailyOrdersLabel;
    @FXML private Label topProductLabel;
    @FXML private Label dailySalesLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;

    private FactureService factureService;
    private ProductService productService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            factureService = new FactureService();
            productService = new ProductService();
            
            loadDashboardData();
            setupDynamicContent();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupDynamicContent() {
        // Mettre à jour le message de bienvenue et la date
        // Note: La récupération du nom de l'utilisateur connecté n'est pas implémentée ici.
        welcomeLabel.setText("Bonjour, Admin !");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
    }

    private void loadDashboardData() {
        try {
            // Commandes du jour
            int dailyOrders = factureService.getOrderCountByDate(LocalDate.now());
            if (dailyOrdersLabel != null) {
                dailyOrdersLabel.setText(String.valueOf(dailyOrders));
            }

            // Produit le plus vendu
            String topProduct = factureService.getTopSellingProductByDate(LocalDate.now());
            if (topProductLabel != null) {
                topProductLabel.setText(topProduct);
            }

            // Ventes du jour
            double dailyRevenue = factureService.getTotalRevenueByDate(LocalDate.now());
            if (dailySalesLabel != null) {
                dailySalesLabel.setText(String.format("%.0f FCFA", dailyRevenue));
            }

            // Statistiques de stock
            List<Product> products = productService.getAllProducts();
            long lowStockCount = products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() < 10).count();
            long outOfStockCount = products.stream().filter(p -> p.getQuantity() == 0).count();

            if (lowStockLabel != null) {
                lowStockLabel.setText(String.valueOf(lowStockCount));
            }
            if (outOfStockLabel != null) {
                outOfStockLabel.setText(String.valueOf(outOfStockCount));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshData() {
        loadDashboardData();
    }
}