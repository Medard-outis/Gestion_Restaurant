package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.User;
import services.AuthService;

import java.time.LocalDate;
import java.util.List;

public class GestionUserController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Boolean> isAdminCol;
    @FXML private TableColumn<User, LocalDate> createdAtCol;
    @FXML private TableColumn<User, Void> actionsCol;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox isAdminCheckbox;
    @FXML private TextField searchField;

    private final AuthService authService = new AuthService();
    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        isAdminCol.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Configurer la colonne d'actions
        setupActionsColumn();

        loadUsers();

        userTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                User selectedUser = userTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    populateForm(selectedUser);
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(deleteButton);
                    buttons.setSpacing(10);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadUsers() {
        List<User> users = authService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    @FXML
    public void addUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        boolean isAdmin = isAdminCheckbox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.");
            return;
        }

        boolean success = authService.createUser(username, password, isAdmin);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Utilisateur ajouté avec succès.");
            clearForm();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur : Le nom d'utilisateur existe déjà.");
        }
    }

    @FXML
    public void updateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un utilisateur dans le tableau.");
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        boolean isAdmin = isAdminCheckbox.isSelected();

        if (newUsername.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Le nom d'utilisateur ne peut pas être vide.");
            return;
        }

        boolean success = authService.updateUser(
                selected.getUsername(), newUsername, newPassword, isAdmin
        );

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Utilisateur mis à jour.");
            clearForm();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour de l'utilisateur.");
        }
    }

    private void deleteUser(User user) {
        if (user == null) return;

        // Confirmation de suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Voulez-vous vraiment supprimer l'utilisateur : " + user.getUsername() + " ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = authService.deleteUser(user.getUsername());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Utilisateur supprimé avec succès.");
                    clearForm();
                    loadUsers();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur lors de la suppression de l'utilisateur.");
                }
            }
        });
    }

    @FXML
    public void filterUsers() {
        String keyword = searchField.getText().toLowerCase().trim();
        userTable.setItems(userList.filtered(user ->
                user.getUsername().toLowerCase().contains(keyword)
        ));
    }

    private void populateForm(User user) {
        if (user != null) {
            usernameField.setText(user.getUsername());
            passwordField.clear(); // Toujours effacer le champ de mot de passe
            isAdminCheckbox.setSelected(user.isAdmin());
        }
    }

    @FXML
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        isAdminCheckbox.setSelected(false);
        userTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
