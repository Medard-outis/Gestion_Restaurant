package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.User;
import services.AuthService;

public class AuthController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private ImageView Exit;
    @FXML private ImageView Reduire;

    private AuthService authService;

    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new AuthService();
        
        // Créer un utilisateur admin par défaut s'il n'existe pas
        try {
            User existingAdmin = authService.getUser("admin");
            if (existingAdmin == null) {
                authService.createUser("admin", "admin123", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        roleChoiceBox.getItems().addAll("Admin", "Utilisateur");
        roleChoiceBox.setValue("Admin");


        // Fermer l'application
        Exit.setOnMouseClicked(event -> {
            System.exit(0);
        });

        // Réduire la fenêtre
        Reduire.setOnMouseClicked(event -> {
            Stage stage = (Stage) Reduire.getScene().getWindow();
            stage.setIconified(true);
        });
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleChoiceBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        if (authService.authenticate(username, password)) {
            User user = authService.getUser(username);

            if (user != null) {
                // Vérifier la correspondance du rôle
                boolean roleMatches = (user.isAdmin() && "Admin".equals(selectedRole)) ||
                                    (!user.isAdmin() && "Utilisateur".equals(selectedRole));

                if (roleMatches) {
                    utils.Session.getInstance().setUser(user);
                    redirectToDashboard(user);
                } else {
                    showAlert("Erreur", "Le rôle sélectionné ne correspond pas à l'utilisateur.");
                }
            } else {
                showAlert("Erreur", "Utilisateur non trouvé");
            }
        } else {
            showAlert("Erreur d'authentification", "Identifiants incorrects");
        }
    }



    private void redirectToDashboard(User user) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root;

        if (user.isAdmin()) {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/dashboard.fxml")));
        } else {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/user_dashboard.fxml")));
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}