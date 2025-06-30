package services;

import models.Product;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private final Connection connection;

    public ProductService() throws SQLException {
        this.connection = DBConnection.getConnection();
        initTable();
    }

    private void initTable() throws SQLException {
        Statement stmt = connection.createStatement();
        // Corriger initTable()
        stmt.executeUpdate("""
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL CHECK(price >= 0),
    quantity INT NOT NULL CHECK(quantity >= 0),
    category VARCHAR(50) NOT NULL,
    date DATE NOT NULL
)

""");
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM products");
             ResultSet rs = stmt.executeQuery()) {
            // Et corriger getAllProducts()
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getDate("date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addProduct(Product p) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO products (name, price, quantity, category, date) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getPrice());
            stmt.setInt(3, p.getQuantity());
            stmt.setString(4, p.getCategory());
            stmt.setDate(5, Date.valueOf(p.getDate())); // Conversion LocalDate -> sql.Date
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product p) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE products SET name=?, price=?, quantity=?, category=?,date=? WHERE id=?")) {
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getPrice()); // prix en 2e
            stmt.setInt(3, p.getQuantity()); // quantité en 3e
            stmt.setString(4, p.getCategory());
            stmt.setDate(5, Date.valueOf(p.getDate()));
            stmt.setInt(6, p.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void logHistorique(String action, String nomProduit, LocalDate date) {
        String sql = "INSERT INTO historique_actions (action, produit, date_action) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setString(2, nomProduit);
            stmt.setDate(3, Date.valueOf(date));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Product getProductByName(String name) throws SQLException {
        String query = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getDate("date").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    public boolean enregistrerSuppressionHistorique(int productId, String name, String motif, LocalDate dateSuppression) {
        String sql = "INSERT INTO historique_suppression (product_id, name, motif, date_suppression) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setString(2, name);
            stmt.setString(3, motif);
            stmt.setDate(4, java.sql.Date.valueOf(dateSuppression));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM products WHERE id=?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
