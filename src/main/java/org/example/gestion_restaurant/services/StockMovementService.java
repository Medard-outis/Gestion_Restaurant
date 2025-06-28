package org.example.gestion_restaurant.services;

import org.example.gestion_restaurant.models.StockMovement;
import org.example.gestion_restaurant.utils.DatabaseInitializer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockMovementService {
    private final Connection connection;

    public StockMovementService() throws SQLException {
        this.connection = DatabaseInitializer.getConnection();
    }

    public boolean processStockEntry(int productId, String productName, int quantity, String reason, String user) {
        try {
            connection.setAutoCommit(false);
            
            // Enregistrer le mouvement
            boolean movementRecorded = recordStockMovement(productId, productName, "ENTREE", quantity, reason, user);
            
            if (movementRecorded) {
                // Mettre à jour le stock du produit
                boolean stockUpdated = updateProductStock(productId, quantity, true);
                
                if (stockUpdated) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
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

    public boolean processStockExit(int productId, String productName, int quantity, String reason, String user) {
        try {
            // Vérifier le stock disponible
            if (!hasEnoughStock(productId, quantity)) {
                return false;
            }
            
            connection.setAutoCommit(false);
            
            // Enregistrer le mouvement
            boolean movementRecorded = recordStockMovement(productId, productName, "SORTIE", quantity, reason, user);
            
            if (movementRecorded) {
                // Mettre à jour le stock du produit
                boolean stockUpdated = updateProductStock(productId, quantity, false);
                
                if (stockUpdated) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
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

    public boolean recordStockMovement(int productId, String productName, String movementType, 
                                     int quantity, String reason, String user) {
        String query = "INSERT INTO stock_movements (product_id, product_name, movement_type, quantity, reason, date, user_action) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setString(2, productName);
            stmt.setString(3, movementType);
            stmt.setInt(4, quantity);
            stmt.setString(5, reason);
            stmt.setDate(6, Date.valueOf(LocalDate.now()));
            stmt.setString(7, user);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateProductStock(int productId, int quantity, boolean isEntry) {
        String query = isEntry ? 
            "UPDATE products SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?" :
            "UPDATE products SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasEnoughStock(int productId, int requestedQuantity) {
        String query = "SELECT quantity FROM products WHERE id = ? AND is_active = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int currentStock = rs.getInt("quantity");
                    return currentStock >= requestedQuantity;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    public List<StockMovement> getAllMovements() {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT * FROM stock_movements ORDER BY date DESC, created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                StockMovement movement = new StockMovement(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("movement_type"),
                    rs.getInt("quantity"),
                    rs.getString("reason"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("user_action")
                );
                movements.add(movement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return movements;
    }

    public List<StockMovement> getMovementsByDate(LocalDate date) {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE date = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getString("reason"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("user_action")
                    );
                    movements.add(movement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return movements;
    }

    public List<StockMovement> getMovementsByType(String movementType) {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE movement_type = ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movementType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getString("reason"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("user_action")
                    );
                    movements.add(movement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return movements;
    }

    public List<StockMovement> getMovementsByProduct(int productId) {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getString("reason"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("user_action")
                    );
                    movements.add(movement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return movements;
    }

    public int getTotalEntriesForPeriod(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT SUM(quantity) as total FROM stock_movements WHERE movement_type = 'ENTREE' AND date BETWEEN ? AND ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    public int getTotalExitsForPeriod(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT SUM(quantity) as total FROM stock_movements WHERE movement_type = 'SORTIE' AND date BETWEEN ? AND ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}