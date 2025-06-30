package controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class DashBoardController implements Initializable {

    @FXML private Button logoutButton;
    @FXML private ImageView Exit;
    @FXML private ImageView Reduire;
    @FXML private StackPane contentArea;

    // Ajout des fx:id pour les boutons du menu
    @FXML private JFXButton gestionButton;
    @FXML private JFXButton utilisateursButton;
    @FXML private JFXButton historiquesButton;
    @FXML private JFXButton mouvementsStockButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        if (user != null) {
            System.out.println("Bienvenue " + user.getUsername());
            // Appliquer les restrictions basées sur le rôle
            applyRoleRestrictions(user);
        }

        // Gérer les actions des icônes
        Exit.setOnMouseClicked(event -> System.exit(0));
        Reduire.setOnMouseClicked(event -> {
            Stage stage = (Stage) Reduire.getScene().getWindow();
            if (stage != null) {
                stage.setIconified(true);
            }
        });

        // Charger la vue d'accueil par défaut
        try {
           loadHomePage();
        } catch (IOException ex) {
            Logger.getLogger(DashBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadHomePage() throws IOException {
        Parent fxml = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/homeDashboard.fxml")));
        contentArea.getChildren().setAll(fxml);
    }

    public void home(ActionEvent actionEvent) throws IOException {
        loadHomePage();
    }

    public void facturations(ActionEvent actionEvent) throws IOException {
        loadView("/views/factureDashboard.fxml");
    }

    public void gestion(ActionEvent actionEvent) throws IOException {
        if (isAdmin()) {
            loadView("/views/gestionDashboard.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    public void utilisateurs(ActionEvent actionEvent) throws IOException {
        if (isAdmin()) {
            loadView("/views/gestion_users.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    public void historiques(ActionEvent actionEvent) throws IOException {
        if (isAdmin()) {
            loadView("/views/historiques.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    public void mouvementsStock(ActionEvent actionEvent) throws IOException {
        if (isAdmin()) {
            loadView("/views/stockMovement.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    private void loadView(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        contentArea.getChildren().setAll(root);
    }

    private boolean isAdmin() {
        User currentUser = Session.getInstance().getUser();
        // La logique est maintenant basée sur le booléen isAdmin() du modèle User
        return currentUser != null && currentUser.isAdmin();
    }

    private void applyRoleRestrictions(User user) {
        boolean isUserAdmin = user.isAdmin();
        // Désactiver et masquer les boutons pour les non-admins
        gestionButton.setVisible(isUserAdmin);
        gestionButton.setManaged(isUserAdmin);

        utilisateursButton.setVisible(isUserAdmin);
        utilisateursButton.setManaged(isUserAdmin);

        historiquesButton.setVisible(isUserAdmin);
        historiquesButton.setManaged(isUserAdmin);

        mouvementsStockButton.setVisible(isUserAdmin);
        mouvementsStockButton.setManaged(isUserAdmin);
    }

    private void showAccessDeniedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Accès Refusé");
        alert.setHeaderText("Permissions insuffisantes");
        alert.setContentText("Cette section est réservée aux administrateurs.");
        alert.showAndWait();
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