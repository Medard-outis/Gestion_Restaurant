package org.example.gestion_restaurant.services;

import org.example.gestion_restaurant.models.Order;
import org.example.gestion_restaurant.models.OrderItem;
import org.example.gestion_restaurant.utils.DatabaseInitializer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final Connection connection;
    private final ProductService productService;
    private final StockMovementService stockMovementService;

    public OrderService() throws SQLException {
        this.connection = DatabaseInitializer.getConnection();
        this.productService = new ProductService();
        this.stockMovementService = new StockMovementService();
    }

    public boolean createOrder(Order order) {
        String orderQuery = "INSERT INTO orders (table_number, customer_name, total_amount, status, notes, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            // Insérer la commande
            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getTableNumber());
                orderStmt.setString(2, order.getCustomerName());
                orderStmt.setDouble(3, order.getTotalAmount());
                orderStmt.setString(4, order.getStatus());
                orderStmt.setString(5, ""); // Notes
                orderStmt.setInt(6, 1); // Created by user ID (à adapter selon la session)
                
                int affectedRows = orderStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Échec de création de la commande");
                }
                
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        order.setId(orderId);
                        
                        // Insérer les items et mettre à jour le stock
                        try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                            for (OrderItem item : order.getItems()) {
                                // Vérifier et réduire le stock
                                if (!reduceStock(item.getProductId(), item.getQuantity())) {
                                    throw new SQLException("Stock insuffisant pour le produit: " + item.getProductName());
                                }
                                
                                itemStmt.setInt(1, orderId);
                                itemStmt.setInt(2, item.getProductId());
                                itemStmt.setString(3, item.getProductName());
                                itemStmt.setInt(4, item.getQuantity());
                                itemStmt.setDouble(5, item.getUnitPrice());
                                itemStmt.setDouble(6, item.getTotalPrice());
                                itemStmt.setString(7, item.getNotes());
                                itemStmt.addBatch();
                                
                                // Enregistrer le mouvement de stock
                                stockMovementService.recordStockMovement(
                                    item.getProductId(),
                                    item.getProductName(),
                                    "SORTIE",
                                    item.getQuantity(),
                                    "Vente - Commande #" + orderId,
                                    "Système"
                                );
                            }
                            itemStmt.executeBatch();
                        }
                    }
                }
            }
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY order_time DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getInt("table_number"),
                    rs.getString("customer_name"),
                    rs.getTimestamp("order_time").toLocalDateTime(),
                    rs.getDouble("total_amount"),
                    rs.getString("status"),
                    rs.getBoolean("is_paid")
                );
                
                // Charger les items de la commande
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE status = ? ORDER BY order_time DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getBoolean("is_paid")
                    );
                    
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    public List<Order> getOrdersByTable(int tableNumber) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE table_number = ? ORDER BY order_time DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tableNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getBoolean("is_paid")
                    );
                    
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String query = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markOrderAsPaid(int orderId, String paymentMethod) {
        String query = "UPDATE orders SET is_paid = true, payment_method = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, paymentMethod);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelOrder(int orderId) {
        try {
            connection.setAutoCommit(false);
            
            // Récupérer les items de la commande pour restaurer le stock
            Order order = getOrderById(orderId);
            if (order != null) {
                for (OrderItem item : order.getItems()) {
                    // Restaurer le stock
                    restoreStock(item.getProductId(), item.getQuantity());
                    
                    // Enregistrer le mouvement de stock
                    stockMovementService.recordStockMovement(
                        item.getProductId(),
                        item.getProductName(),
                        "ENTREE",
                        item.getQuantity(),
                        "Annulation commande #" + orderId,
                        "Système"
                    );
                }
            }
            
            // Marquer la commande comme annulée
            boolean success = updateOrderStatus(orderId, "Annulé");
            
            if (success) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getBoolean("is_paid")
                    );
                    
                    loadOrderItems(order);
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    private void loadOrderItems(Order order) {
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, order.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("notes")
                    );
                    order.addItem(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean reduceStock(int productId, int quantity) {
        String query = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean restoreStock(int productId, int quantity) {
        String query = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalRevenueByDate(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT SUM(total_amount) as total FROM orders WHERE order_time BETWEEN ? AND ? AND is_paid = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }

    public int getOrderCountByStatus(String status) {
        String query = "SELECT COUNT(*) as count FROM orders WHERE status = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}