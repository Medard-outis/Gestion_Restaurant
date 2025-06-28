package org.example.gestion_restaurant.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.gestion_restaurant.models.User;
import org.example.gestion_restaurant.services.AuthService;
import org.example.gestion_restaurant.utils.AlertUtils;
import org.example.gestion_restaurant.utils.Session;
import org.example.gestion_restaurant.utils.ValidationUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new AuthService();
        
        // Créer un utilisateur admin par défaut s'il n'existe pas
        initializeDefaultAdmin();
        
        // Configuration du ChoiceBox
        roleChoiceBox.getItems().addAll("Admin", "Utilisateur");
        roleChoiceBox.setValue("Admin");
        
        // Configuration des événements
        setupEventHandlers();
        
        // Focus sur le champ username
        usernameField.requestFocus();
    }

    private void initializeDefaultAdmin() {
        try {
            User existingAdmin = authService.getUser("admin");
            if (existingAdmin == null) {
                boolean created = authService.createUser("admin", "admin123", true);
                if (created) {
                    System.out.println("✅ Utilisateur admin par défaut créé (admin/admin123)");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'admin par défaut: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        // Validation en temps réel
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        
        // Connexion avec Enter
        passwordField.setOnAction(e -> handleLogin());
        
        // Bouton annuler
        cancelButton.setOnAction(e -> System.exit(0));
    }

    @FXML
    private void handleLogin() {
        clearError();
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleChoiceBox.getValue();

        // Validation des champs
        if (!validateInput(username, password, selectedRole)) {
            return;
        }

        try {
            // Authentification
            if (authService.authenticate(username, password)) {
                User user = authService.getUser(username);

                if (user != null) {
                    // Vérifier la correspondance du rôle
                    if (isRoleValid(user, selectedRole)) {
                        // Démarrer la session
                        Session.getInstance().setUser(user);
                        
                        // Rediriger vers le tableau de bord approprié
                        redirectToDashboard(user);
                        
                        // Log de connexion
                        System.out.println("✅ Connexion réussie: " + user.getUsername() + 
                                         " (" + (user.isAdmin() ? "Admin" : "Utilisateur") + ")");
                    } else {
                        showError("Le rôle sélectionné ne correspond pas à votre compte.");
                    }
                } else {
                    showError("Utilisateur non trouvé dans le système.");
                }
            } else {
                showError("Nom d'utilisateur ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInput(String username, String password, String selectedRole) {
        if (!ValidationUtils.isNotEmpty(username)) {
            showError("Veuillez entrer votre nom d'utilisateur.");
            usernameField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            showError("Veuillez entrer votre mot de passe.");
            passwordField.requestFocus();
            return false;
        }

        if (selectedRole == null) {
            showError("Veuillez sélectionner votre rôle.");
            roleChoiceBox.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isRoleValid(User user, String selectedRole) {
        return (user.isAdmin() && "Admin".equals(selectedRole)) ||
               (!user.isAdmin() && "Utilisateur".equals(selectedRole));
    }

    private void redirectToDashboard(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            String fxmlPath = user.isAdmin() ? "/views/admin_dashboard.fxml" : "/views/user_dashboard.fxml";
            
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource(fxmlPath)));
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles/application.css")).toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Restaurant Le Notre - " + (user.isAdmin() ? "Administration" : "Caisse"));
            stage.show();
            
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            AlertUtils.showError("Erreur de connexion", message);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void handleCancel() {
        System.exit(0);
    }
}