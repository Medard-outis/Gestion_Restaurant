package org.example.gestion_restaurant.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.example.gestion_restaurant.models.Product;
import org.example.gestion_restaurant.services.AuthService;
import org.example.gestion_restaurant.services.OrderService;
import org.example.gestion_restaurant.services.ProductService;
import org.example.gestion_restaurant.services.TableService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private Label usersCountLabel;
    @FXML private Label dailySalesLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label tablesCountLabel;
    @FXML private Label occupiedTablesLabel;
    @FXML private Label pendingOrdersLabel;

    private AuthService authService;
    private OrderService orderService;
    private ProductService productService;
    private TableService tableService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initializeServices();
            loadDashboardData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeServices() throws SQLException {
        authService = new AuthService();
        orderService = new OrderService();
        productService = new ProductService();
        tableService = new TableService();
    }

    private void loadDashboardData() {
        try {
            // Nombre d'utilisateurs
            int userCount = authService.getAllUsers().size();
            updateLabel(usersCountLabel, String.valueOf(userCount));

            // Revenus du jour
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            double dailyRevenue = orderService.getTotalRevenueByDate(startOfDay, endOfDay);
            updateLabel(dailySalesLabel, String.format("%.0f FC", dailyRevenue));
            updateLabel(totalRevenueLabel, String.format("%.0f FC", dailyRevenue));

            // Statistiques de stock
            List<Product> products = productService.getAllProducts();
            long lowStockCount = products.stream().filter(Product::isLowStock).count();
            long outOfStockCount = products.stream().filter(Product::isOutOfStock).count();
            
            updateLabel(lowStockLabel, String.valueOf(lowStockCount));
            updateLabel(outOfStockLabel, String.valueOf(outOfStockCount));

            // Statistiques des tables
            int totalTables = tableService.getTableCount();
            int occupiedTables = tableService.getOccupiedTableCount();
            
            updateLabel(tablesCountLabel, String.valueOf(totalTables));
            updateLabel(occupiedTablesLabel, String.valueOf(occupiedTables));

            // Commandes en attente
            int pendingOrders = orderService.getOrderCountByStatus("En attente");
            updateLabel(pendingOrdersLabel, String.valueOf(pendingOrders));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLabel(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    public void refreshData() {
        loadDashboardData();
    }
}