package services;

import models.User;
import utils.DBConnection;
import utils.DatabaseInitializer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private final Connection connection;

    public AuthService() {
        try {
            DatabaseInitializer.initialize(); // Crée BDD + tables si besoin
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la BDD", e);
        }
    }

    public boolean authenticate(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return BCrypt.checkpw(password, hashedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUser(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getBoolean("is_admin"),
                        rs.getDate("createdAt").toLocalDate()

                        );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createUser(String username, String password, boolean isAdmin) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "INSERT INTO users (username, password, is_admin) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setBoolean(3, isAdmin);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getBoolean("is_admin"),
                        rs.getDate("createdAt").toLocalDate()
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUser(String oldUsername, String newUsername, String newPassword, boolean isAdmin) {
        String query = newPassword.isEmpty()
                ? "UPDATE users SET username = ?, is_admin = ? WHERE username = ?"
                : "UPDATE users SET username = ?, password = ?, is_admin = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newUsername);
            if (!newPassword.isEmpty()) {
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // Hasher le mot de passe
                stmt.setString(2, hashed);
                stmt.setBoolean(3, isAdmin);
                stmt.setString(4, oldUsername);
            } else {
                stmt.setBoolean(2, isAdmin);
                stmt.setString(3, oldUsername);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
