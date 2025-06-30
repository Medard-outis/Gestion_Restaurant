package utils;

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
            // Étape 1 : Créer la base de données si elle n'existe pas
            try (Connection conn = DriverManager.getConnection(URL_WITHOUT_DB, USER, PASSWORD)) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("✅ Base de données vérifiée/créée");
            }

            // Étape 2 : Créer les tables nécessaires si elles n'existent pas
            try (Connection conn = DriverManager.getConnection(URL_WITH_DB, USER, PASSWORD)) {
                Statement stmt = conn.createStatement();

                // Table Products (mise à jour)
                String createProductTable = """
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        price DOUBLE NOT NULL,
                        quantity INT NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        date DATE NOT NULL
                    );
                """;

                // Table Menu
                String createMenuTable = """
                    CREATE TABLE IF NOT EXISTS menu (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        price DOUBLE NOT NULL,
                        category VARCHAR(100) NOT NULL
                    );
                """;

                // Table Users
                String createUserTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(100) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        is_admin BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """;

                // Table Factures
                String createFacturesTable = """
                    CREATE TABLE IF NOT EXISTS factures (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        client_name VARCHAR(100) NOT NULL,
                        table_number INT,
                        date DATE NOT NULL,
                        total_amount DOUBLE NOT NULL,
                        is_paid BOOLEAN DEFAULT FALSE,
                        status VARCHAR(50) DEFAULT 'EN_COURS',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """;

                // Table Facture Items
                String createFactureItemsTable = """
                    CREATE TABLE IF NOT EXISTS facture_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        facture_id INT NOT NULL,
                        product_name VARCHAR(100) NOT NULL,
                        quantity INT NOT NULL,
                        unit_price DOUBLE NOT NULL,
                        total_price DOUBLE NOT NULL,
                        FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE
                    );
                """;

                // Table Stock Movements
                String createStockMovementsTable = """
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_id INT NOT NULL,
                        product_name VARCHAR(100) NOT NULL,
                        movement_type ENUM('ENTREE', 'SORTIE') NOT NULL,
                        quantity INT NOT NULL,
                        reason VARCHAR(255),
                        date DATE NOT NULL,
                        user_action VARCHAR(100),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES products(id)
                    );
                """;

                // Table Historique Actions
                String createHistoriqueActionsTable = """
                    CREATE TABLE IF NOT EXISTS historique_actions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        action VARCHAR(255) NOT NULL,
                        produit VARCHAR(255) NOT NULL,
                        date_action DATE NOT NULL
                    );
                """;

                // Table Historique Suppression
                String createHistoriqueSuppressionTable = """
                    CREATE TABLE IF NOT EXISTS historique_suppression (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        product_id INT,
                        name VARCHAR(100),
                        motif TEXT,
                        date_suppression DATE,
                        FOREIGN KEY (product_id) REFERENCES products(id)
                    );
                """;

                // Exécution des requêtes
                stmt.executeUpdate(createProductTable);
                stmt.executeUpdate(createMenuTable);
                stmt.executeUpdate(createUserTable);
                stmt.executeUpdate(createFacturesTable);
                stmt.executeUpdate(createFactureItemsTable);
                stmt.executeUpdate(createStockMovementsTable);
                stmt.executeUpdate(createHistoriqueActionsTable);
                stmt.executeUpdate(createHistoriqueSuppressionTable);

                System.out.println("✅ Tables vérifiées/créées");

            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'initialisation de la base de données : " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_WITH_DB, USER, PASSWORD);
    }
}