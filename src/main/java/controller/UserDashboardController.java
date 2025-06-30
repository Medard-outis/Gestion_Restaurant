package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.User;
import utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDashboardController implements Initializable {

    @FXML private Button logoutButton;
    @FXML private ImageView Exit;
    @FXML private StackPane contentArea;
    @FXML private ImageView minimize;
    @FXML private ImageView maximize;
    @FXML private ImageView exit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        if (user != null) {
            System.out.println("Bienvenue " + user.getUsername());
        }

        // Gestion des boutons universels
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

        // Fermer l'application
        Exit.setOnMouseClicked(event -> {
            System.exit(0);
        });

        // Charger la vue d'accueil par défaut
        try {
           Parent fxml = FXMLLoader.load(getClass().getResource("/views/homeDashboard.fxml"));
           contentArea.getChildren().setAll(fxml);
        } catch (IOException ex) {
            Logger.getLogger(UserDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void home(javafx.event.ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/homeDashboard.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(root);
    }

    public void facturations (javafx.event.ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/factureDashboard.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(root);
    }

    @FXML
    private void handleLogout() throws IOException {
        // Vider la session
        Session.getInstance().clear();

        // Rediriger vers la page de connexion
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/auth.fxml")));
        stage.setScene(new Scene(root));
        stage.show();
    }
}