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

    public boolean createFacture(Facture facture) {
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
                    throw new SQLException("Échec de création de la facture");
                }
                
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = factureStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int factureId = generatedKeys.getInt(1);
                        facture.setId(factureId);
                        
                        // Insérer les items
                        try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                            for (FactureItem item : facture.getItems()) {
                                itemStmt.setInt(1, factureId);
                                itemStmt.setString(2, item.getProductName());
                                itemStmt.setInt(3, item.getQuantity());
                                itemStmt.setDouble(4, item.getUnitPrice());
                                itemStmt.setDouble(5, item.getTotalPrice());
                                itemStmt.addBatch();
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