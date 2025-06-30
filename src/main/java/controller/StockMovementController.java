package controller;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Product;
import models.StockMovement;
import services.ProductService;
import services.StockMovementService;
import utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class StockMovementController implements Initializable {

    @FXML private ChoiceBox<String> productChoiceBox;
    @FXML private TextField quantityField;
    @FXML private TextArea reasonTextArea;
    @FXML private RadioButton entreeRadio;
    @FXML private RadioButton sortieRadio;
    @FXML private DatePicker datePicker;
    @FXML private ToggleGroup movementTypeGroup;

    @FXML private TableView<StockMovement> movementsTable;
    @FXML private TableColumn<StockMovement, String> colProduct;
    @FXML private TableColumn<StockMovement, String> colType;
    @FXML private TableColumn<StockMovement, Integer> colQuantity;
    @FXML private TableColumn<StockMovement, String> colReason;
    @FXML private TableColumn<StockMovement, String> colDate;
    @FXML private TableColumn<StockMovement, String> colUser;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> filterTypeChoiceBox;

    @FXML private JFXButton processButton;
    @FXML private JFXButton clearButton;

    private ProductService productService;
    private StockMovementService stockMovementService;
    private ObservableList<StockMovement> movementsData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            productService = new ProductService();
            stockMovementService = new StockMovementService();
            movementsData = FXCollections.observableArrayList();

            setupUI();
            setupTable();
            loadProducts();
            loadMovements();
            
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupUI() {
        // Configuration des RadioButtons
        entreeRadio.setSelected(true);

        // Configuration du DatePicker
        datePicker.setValue(LocalDate.now());

        // Configuration du filtre
        filterTypeChoiceBox.getItems().addAll("Tous", "ENTREE", "SORTIE");
        filterTypeChoiceBox.setValue("Tous");
        filterTypeChoiceBox.setOnAction(e -> filterMovements());

        // Listener pour la recherche
        filterField.textProperty().addListener((obs, oldVal, newVal) -> filterMovements());
    }

    private void setupTable() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("movementType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userAction"));

        // Style pour les types de mouvement
        colType.setCellFactory(column -> new TableCell<StockMovement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ENTREE".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("SORTIE".equals(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        movementsTable.setItems(movementsData);
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productChoiceBox.getItems().clear();
            for (Product product : products) {
                productChoiceBox.getItems().add(product.getName());
            }
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des produits", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadMovements() {
        try {
            List<StockMovement> movements = stockMovementService.getAllMovements();
            movementsData.setAll(movements);
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des mouvements", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProcessMovement() {
        try {
            if (!validateFields()) return;

            String productName = productChoiceBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            String reason = reasonTextArea.getText().trim();
            String movementType = entreeRadio.isSelected() ? "ENTREE" : "SORTIE";
            String user = Session.getInstance().getUser() != null ? 
                         Session.getInstance().getUser().getUsername() : "Système";

            // Trouver l'ID du produit
            Product selectedProduct = findProductByName(productName);
            if (selectedProduct == null) {
                showAlert("Produit non trouvé", Alert.AlertType.ERROR);
                return;
            }

            boolean success;
            if ("ENTREE".equals(movementType)) {
                success = stockMovementService.processStockEntry(
                    selectedProduct.getId(), productName, quantity, reason, user
                );
            } else {
                success = stockMovementService.processStockExit(
                    selectedProduct.getId(), productName, quantity, reason, user
                );
            }

            if (success) {
                showAlert("Mouvement de stock traité avec succès", Alert.AlertType.INFORMATION);
                loadMovements();
                clearFields();
            } else {
                if ("SORTIE".equals(movementType)) {
                    showAlert("Stock insuffisant pour cette sortie", Alert.AlertType.ERROR);
                } else {
                    showAlert("Erreur lors du traitement du mouvement", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer une quantité valide", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearFields() {
        clearFields();
    }

    private void filterMovements() {
        String searchText = filterField.getText().toLowerCase();
        String typeFilter = filterTypeChoiceBox.getValue();

        try {
            List<StockMovement> allMovements = stockMovementService.getAllMovements();
            List<StockMovement> filtered = allMovements.stream()
                .filter(movement -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        movement.getProductName().toLowerCase().contains(searchText) ||
                        (movement.getReason() != null && movement.getReason().toLowerCase().contains(searchText)) ||
                        (movement.getUserAction() != null && movement.getUserAction().toLowerCase().contains(searchText));
                    
                    boolean matchesType = "Tous".equals(typeFilter) || 
                        movement.getMovementType().equals(typeFilter);
                    
                    return matchesSearch && matchesType;
                })
                .toList();
            
            movementsData.setAll(filtered);
        } catch (Exception e) {
            showAlert("Erreur lors du filtrage", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private Product findProductByName(String name) {
        try {
            List<Product> products = productService.getAllProducts();
            return products.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validateFields() {
        if (productChoiceBox.getValue() == null || productChoiceBox.getValue().isEmpty()) {
            showAlert("Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return false;
        }
        if (quantityField.getText().isEmpty()) {
            showAlert("Veuillez entrer une quantité", Alert.AlertType.WARNING);
            return false;
        }
        if (reasonTextArea.getText().trim().isEmpty()) {
            showAlert("Veuillez entrer une raison pour ce mouvement", Alert.AlertType.WARNING);
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                showAlert("La quantité doit être positive", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer une quantité valide", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void clearFields() {
        productChoiceBox.getSelectionModel().clearSelection();
        quantityField.clear();
        reasonTextArea.clear();
        entreeRadio.setSelected(true);
        datePicker.setValue(LocalDate.now());
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}