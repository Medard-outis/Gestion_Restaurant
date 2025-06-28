package org.example.gestion_restaurant.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String DB_NAME = "restaurant_db";
    private static final String URL_WITHOUT_DB = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
    private static final String URL_WITH_DB = "jdbc:mysql://localhost:3306/" + DB_NAME + "?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void initialize() {
        try {
            // Créer la base de données si elle n'existe pas
            createDatabase();
            
            // Créer les tables
            createTables();
            
            System.out.println("✅ Base de données initialisée avec succès");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'initialisation de la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL_WITHOUT_DB, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                             " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ Base de données vérifiée/créée");
        }
    }

    private static void createTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL_WITH_DB, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Table Users
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    is_admin BOOLEAN DEFAULT FALSE,
                    created_at DATE NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT TRUE
                )
            """);

            // Table Categories
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) UNIQUE NOT NULL,
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Table Products
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL CHECK(price >= 0),
                    quantity INT NOT NULL CHECK(quantity >= 0),
                    category VARCHAR(50) NOT NULL,
                    description TEXT,
                    date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT TRUE,
                    INDEX idx_category (category),
                    INDEX idx_name (name)
                )
            """);

            // Table Tables
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS restaurant_tables (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    number INT UNIQUE NOT NULL,
                    capacity INT NOT NULL CHECK(capacity > 0),
                    status ENUM('Libre', 'Occupée', 'Réservée', 'En nettoyage') DEFAULT 'Libre',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            // Table Orders
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    table_number INT NOT NULL,
                    customer_name VARCHAR(100),
                    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                    status ENUM('En attente', 'En préparation', 'Prêt', 'Servi', 'Annulé') DEFAULT 'En attente',
                    is_paid BOOLEAN DEFAULT FALSE,
                    payment_method VARCHAR(50),
                    notes TEXT,
                    created_by INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (created_by) REFERENCES users(id),
                    INDEX idx_table_number (table_number),
                    INDEX idx_status (status),
                    INDEX idx_order_time (order_time)
                )
            """);

            // Table Order Items
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    quantity INT NOT NULL CHECK(quantity > 0),
                    unit_price DECIMAL(10,2) NOT NULL,
                    total_price DECIMAL(10,2) NOT NULL,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                    FOREIGN KEY (product_id) REFERENCES products(id),
                    INDEX idx_order_id (order_id),
                    INDEX idx_product_id (product_id)
                )
            """);

            // Table Stock Movements
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS stock_movements (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    product_id INT NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    movement_type ENUM('ENTREE', 'SORTIE') NOT NULL,
                    quantity INT NOT NULL CHECK(quantity > 0),
                    reason VARCHAR(255),
                    reference_order_id INT,
                    date DATE NOT NULL,
                    user_action VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (product_id) REFERENCES products(id),
                    FOREIGN KEY (reference_order_id) REFERENCES orders(id),
                    INDEX idx_product_id (product_id),
                    INDEX idx_movement_type (movement_type),
                    INDEX idx_date (date)
                )
            """);

            // Table Factures (pour compatibilité)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS factures (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT UNIQUE,
                    client_name VARCHAR(100) NOT NULL,
                    table_number INT,
                    date DATE NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    is_paid BOOLEAN DEFAULT FALSE,
                    status VARCHAR(50) DEFAULT 'EN_COURS',
                    payment_method VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (order_id) REFERENCES orders(id),
                    INDEX idx_date (date),
                    INDEX idx_status (status)
                )
            """);

            // Table Facture Items (pour compatibilité)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS facture_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    facture_id INT NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    quantity INT NOT NULL,
                    unit_price DECIMAL(10,2) NOT NULL,
                    total_price DECIMAL(10,2) NOT NULL,
                    FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE
                )
            """);

            // Table Historique Actions
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS historique_actions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    action VARCHAR(255) NOT NULL,
                    produit VARCHAR(255) NOT NULL,
                    date_action DATE NOT NULL,
                    user_id INT,
                    details TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    INDEX idx_date_action (date_action),
                    INDEX idx_action (action)
                )
            """);

            // Table Historique Suppression
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS historique_suppression (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    product_id INT,
                    name VARCHAR(100),
                    motif TEXT,
                    date_suppression DATE,
                    user_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    INDEX idx_date_suppression (date_suppression)
                )
            """);

            // Insérer des catégories par défaut
            stmt.executeUpdate("""
                INSERT IGNORE INTO categories (name, description) VALUES 
                ('Aliments', 'Plats principaux et accompagnements'),
                ('Boissons', 'Boissons chaudes et froides'),
                ('Desserts', 'Desserts et pâtisseries'),
                ('Entrées', 'Entrées et amuse-bouches'),
                ('Snacks', 'Collations et en-cas')
            """);

            // Insérer des tables par défaut
            stmt.executeUpdate("""
                INSERT IGNORE INTO restaurant_tables (number, capacity) VALUES 
                (1, 2), (2, 2), (3, 4), (4, 4), (5, 6), 
                (6, 6), (7, 8), (8, 2), (9, 4), (10, 4),
                (11, 2), (12, 2), (13, 4), (14, 4), (15, 6),
                (16, 6), (17, 8), (18, 2), (19, 4), (20, 4)
            """);

            System.out.println("✅ Tables créées avec succès");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_WITH_DB, USER, PASSWORD);
    }
}