package org.example.gestion_restaurant.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.gestion_restaurant.models.User;
import org.example.gestion_restaurant.utils.AlertUtils;
import org.example.gestion_restaurant.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController implements Initializable {

    @FXML private Button logoutButton;
    @FXML private ImageView exitIcon;
    @FXML private Label menuLabel;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        if (user != null) {
            updateWelcomeMessage(user);
            System.out.println("✅ Dashboard initialisé pour: " + user.getUsername());
        }

        // Configuration des événements
        setupEventHandlers();
        
        // Charger la vue d'accueil par défaut
        loadDefaultView();
    }

    private void setupEventHandlers() {
        // Fermer l'application
        if (exitIcon != null) {
            exitIcon.setOnMouseClicked(event -> handleExit());
        }
    }

    private void updateWelcomeMessage(User user) {
        if (welcomeLabel != null) {
            String role = user.isAdmin() ? "Administrateur" : "Caissier";
            welcomeLabel.setText("Bienvenue " + user.getUsername() + " (" + role + ")");
        }
    }

    private void loadDefaultView() {
        try {
            Parent fxml = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/views/home_dashboard.fxml")));
            contentArea.getChildren().setAll(fxml);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue d'accueil", ex);
            AlertUtils.showError("Erreur", "Impossible de charger la vue d'accueil");
        }
    }

    @FXML
    public void loadHomeView() {
        loadView("/views/home_dashboard.fxml", "Accueil");
    }

    @FXML
    public void loadProductsView() {
        loadView("/views/products_dashboard.fxml", "Gestion des Produits");
    }

    @FXML
    public void loadOrdersView() {
        loadView("/views/orders_dashboard.fxml", "Gestion des Commandes");
    }

    @FXML
    public void loadTablesView() {
        loadView("/views/tables_dashboard.fxml", "Gestion des Tables");
    }

    @FXML
    public void loadStockView() {
        loadView("/views/stock_dashboard.fxml", "Gestion du Stock");
    }

    @FXML
    public void loadUsersView() {
        // Vérifier les permissions admin
        if (!Session.getInstance().isAdmin()) {
            AlertUtils.showWarning("Accès refusé", "Seuls les administrateurs peuvent gérer les utilisateurs.");
            return;
        }
        loadView("/views/users_dashboard.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    public void loadReportsView() {
        loadView("/views/reports_dashboard.fxml", "Rapports et Historiques");
    }

    @FXML
    public void loadSettingsView() {
        loadView("/views/settings_dashboard.fxml", "Paramètres");
    }

    private void loadView(String fxmlPath, String viewName) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource(fxmlPath)));
            contentArea.getChildren().setAll(root);
            
            LOGGER.info("Vue chargée: " + viewName);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue: " + viewName, e);
            AlertUtils.showError("Erreur", "Impossible de charger la vue: " + viewName);
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertUtils.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            try {
                // Vider la session
                User currentUser = Session.getInstance().getUser();
                Session.getInstance().clear();
                
                // Log de déconnexion
                if (currentUser != null) {
                    System.out.println("✅ Déconnexion: " + currentUser.getUsername());
                }

                // Rediriger vers la page de connexion
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/views/auth.fxml")));
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm());
                
                stage.setScene(scene);
                stage.setTitle("Restaurant Le Notre - Connexion");
                stage.show();
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la déconnexion", e);
                AlertUtils.showError("Erreur", "Erreur lors de la déconnexion: " + e.getMessage());
            }
        }
    }

    private void handleExit() {
        if (AlertUtils.showConfirmation("Fermer l'application", 
                "Êtes-vous sûr de vouloir fermer l'application ?")) {
            System.exit(0);
        }
    }
}