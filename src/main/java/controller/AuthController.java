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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.User;
import services.AuthService;

public class AuthController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ImageView exit;
    @FXML private ImageView minimize;
    @FXML private ImageView maximize;

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

        // Gestion des boutons réduire, maximiser/restaurer et fermer
        if (exit != null) {
            exit.setOnMouseClicked(event -> {
                Stage stage = (Stage) exit.getScene().getWindow();
                stage.close();
            });
        }
        if (minimize != null) {
            minimize.setOnMouseClicked(event -> {
                Stage stage = (Stage) minimize.getScene().getWindow();
                stage.setIconified(true);
            });
        }
        if (maximize != null) {
            maximize.setOnMouseClicked(event -> {
                Stage stage = (Stage) maximize.getScene().getWindow();
                stage.setMaximized(!stage.isMaximized());
            });
        }
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        if (authService.authenticate(username, password)) {
            User user = authService.getUser(username);

            if (user != null) {
                // Suppression de la vérification du rôle
                utils.Session.getInstance().setUser(user);
                redirectToDashboard(user);
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