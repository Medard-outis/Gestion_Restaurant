package services;

import models.Facture;
import models.FactureItem;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FactureService {
    private final Connection connection;

    public FactureService() throws SQLException {
        this.connection = DBConnection.getConnection();
    }

    public Facture createFactureWithDetails(Facture facture) {
        String factureQuery = "INSERT INTO factures (client_name, table_number, date, total_amount, is_paid, status) VALUES (?, ?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO facture_items (facture_id, product_name, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            // Insérer la facture
            try (PreparedStatement factureStmt = connection.prepareStatement(factureQuery, Statement.RETURN_GENERATED_KEYS)) {
                factureStmt.setString(1, facture.getClientName());
                factureStmt.setInt(2, facture.getTableNumber());
                factureStmt.setDate(3, Date.valueOf(facture.getDate()));
                factureStmt.setDouble(4, facture.getTotalAmount());
                factureStmt.setBoolean(5, facture.isPaid());
                factureStmt.setString(6, facture.getStatus());
                
                int affectedRows = factureStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Échec de création de la facture, aucune ligne affectée.");
                }
                
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = factureStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int factureId = generatedKeys.getInt(1);
                        facture.setId(factureId);
                        
                        // Insérer les items
                        try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                            for (FactureItem item : facture.getItems()) {
                                item.setFactureId(factureId); // Assigner l'ID de la facture à l'item
                                itemStmt.setInt(1, factureId);
                                itemStmt.setString(2, item.getProductName());
                                itemStmt.setInt(3, item.getQuantity());
                                itemStmt.setDouble(4, item.getUnitPrice());
                                itemStmt.setDouble(5, item.getTotalPrice());
                                itemStmt.addBatch();
                            }
                            itemStmt.executeBatch();
                        }
                    } else {
                        throw new SQLException("Échec de création de la facture, aucun ID obtenu.");
                    }
                }
            }
            
            connection.commit();
            return facture; // Retourner la facture complète avec son ID

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace(); // Log l'erreur de rollback
            }
            e.printStackTrace();
            return null; // Retourner null en cas d'erreur
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace(); // Log l'erreur de réinitialisation
            }
        }
    }

    public List<Facture> getAllFactures() {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM factures ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Facture facture = new Facture(
                    rs.getInt("id"),
                    rs.getString("client_name"),
                    rs.getInt("table_number"),
                    rs.getDate("date").toLocalDate(),
                    rs.getDouble("total_amount"),
                    rs.getBoolean("is_paid"),
                    rs.getString("status")
                );
                
                // Charger les items de la facture
                loadFactureItems(facture);
                factures.add(facture);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return factures;
    }

    public List<Facture> getFacturesByDate(LocalDate date) {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM factures WHERE date = ? ORDER BY id DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Facture facture = new Facture(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getInt("table_number"),
                        rs.getDate("date").toLocalDate(),
                        rs.getDouble("total_amount"),
                        rs.getBoolean("is_paid"),
                        rs.getString("status")
                    );
                    
                    loadFactureItems(facture);
                    factures.add(facture);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return factures;
    }

    public double getTotalRevenueByDate(LocalDate date) {
        String query = "SELECT SUM(total_amount) as total FROM factures WHERE date = ? AND is_paid = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            
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

    public int getOrderCountByDate(LocalDate date) {
        String query = "SELECT COUNT(id) as total FROM factures WHERE date = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));

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

    public String getTopSellingProductByDate(LocalDate date) {
        String query = "SELECT product_name, SUM(quantity) as total_quantity " +
                       "FROM facture_items fi " +
                       "JOIN factures f ON fi.facture_id = f.id " +
                       "WHERE f.date = ? " +
                       "GROUP BY product_name " +
                       "ORDER BY total_quantity DESC " +
                       "LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("product_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "-";
    }

    public boolean updateFactureStatus(int factureId, String status, boolean isPaid) {
        String query = "UPDATE factures SET status = ?, is_paid = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setBoolean(2, isPaid);
            stmt.setInt(3, factureId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFacture(int factureId) {
        String query = "DELETE FROM factures WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, factureId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadFactureItems(Facture facture) {
        String query = "SELECT * FROM facture_items WHERE facture_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, facture.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FactureItem item = new FactureItem(
                        rs.getInt("id"),
                        rs.getInt("facture_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                    );
                    facture.addItem(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}