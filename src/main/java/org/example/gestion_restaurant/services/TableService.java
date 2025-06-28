package org.example.gestion_restaurant.services;

import org.example.gestion_restaurant.models.Table;
import org.example.gestion_restaurant.utils.DatabaseInitializer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableService {
    private final Connection connection;

    public TableService() throws SQLException {
        this.connection = DatabaseInitializer.getConnection();
    }

    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM restaurant_tables ORDER BY number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Table table = new Table(
                    rs.getInt("id"),
                    rs.getInt("number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                );
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<Table> getAvailableTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM restaurant_tables WHERE status = 'Libre' ORDER BY number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Table table = new Table(
                    rs.getInt("id"),
                    rs.getInt("number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                );
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public Table getTableByNumber(int number) {
        String query = "SELECT * FROM restaurant_tables WHERE number = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, number);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Table(
                        rs.getInt("id"),
                        rs.getInt("number"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateTableStatus(int tableNumber, String status) {
        String query = "UPDATE restaurant_tables SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE number = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, tableNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addTable(Table table) {
        String query = "INSERT INTO restaurant_tables (number, capacity, status) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, table.getNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    table.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTable(Table table) {
        String query = "UPDATE restaurant_tables SET number = ?, capacity = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, table.getNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setInt(4, table.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTable(int id) {
        String query = "DELETE FROM restaurant_tables WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTableCount() {
        String query = "SELECT COUNT(*) as count FROM restaurant_tables";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getOccupiedTableCount() {
        String query = "SELECT COUNT(*) as count FROM restaurant_tables WHERE status = 'Occupée'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Table> getTablesByCapacity(int minCapacity) {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM restaurant_tables WHERE capacity >= ? AND status = 'Libre' ORDER BY capacity, number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, minCapacity);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Table table = new Table(
                        rs.getInt("id"),
                        rs.getInt("number"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                    );
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
}