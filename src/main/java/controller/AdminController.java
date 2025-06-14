package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.AuthService;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;

    @FXML private CheckBox dashboardCheckBox;
    @FXML private CheckBox billingCheckBox;
    @FXML private CheckBox stockCheckBox;

    @FXML private Button dashboardButton;
    @FXML private Button logoutButton;

    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new AuthService();
    }

    /**
     * Méthode appelée lorsqu'on clique sur "Créer un utilisateur"
     */
    @FXML
    private void handleCreateUser() {
        String username = newUsernameField.getText();
        String password = newPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires");
            return;
        }

        boolean success = authService.createUser(username, password, false);

        if (success) {
            showAlert("Succès", "Utilisateur créé avec succès");
            newUsernameField.clear();
            newPasswordField.clear();
        } else {
            showAlert("Erreur", "Nom d'utilisateur déjà utilisé");
        }
    }

    /**
     * Méthode appelée lors du clic sur "Tableau de bord"
     */
    @FXML
    private void handleDashboardButton() {
        try {
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/dashboard.fxml"))); // à adapter selon ton fichier
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le tableau de bord");
            e.printStackTrace();
        }
    }

    /**
     * Méthode appelée lors du clic sur "Déconnexion"
     */
    @FXML
    private void handleLogout() {
        try {
            // ✅ Récupération de la scène à partir d’un composant non nul
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // ✅ Chargement de la nouvelle vue (login)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (NullPointerException e) {
            showAlert("Erreur", "Échec de déconnexion : bouton ou scène introuvable.");
            e.printStackTrace();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }


    /**
     * Affiche une alerte à l'utilisateur
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
