package controller;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Facture;
import models.FactureItem;
import models.Product;
import services.FactureService;
import services.ProductService;
import services.StockMovementService;
import utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class FactureDashboardController implements Initializable {

    @FXML private TextField clientNameField;
    @FXML private ChoiceBox<String> productChoiceBox;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<Integer> tableChoiceBox;
    @FXML private TextField totalGeneralField;

    @FXML private TableView<FactureItem> factureItemsTable;
    @FXML private TableColumn<FactureItem, String> colDesignation;
    @FXML private TableColumn<FactureItem, Integer> colQuantity;
    @FXML private TableColumn<FactureItem, Double> colUnitPrice;
    @FXML private TableColumn<FactureItem, Double> colTotalPrice;
    @FXML private TableColumn<FactureItem, Void> colActions;

    private ProductService productService;
    private FactureService factureService;
    private StockMovementService stockMovementService; // Service pour gérer les mouvements de stock
    private ObservableList<FactureItem> factureItems;
    private Facture currentFacture;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            productService = new ProductService();
            factureService = new FactureService();
            stockMovementService = new StockMovementService(); // Initialiser le service
            factureItems = FXCollections.observableArrayList();
            currentFacture = new Facture();

            setupUI();
            setupTable();
            loadProducts();
            
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupUI() {
        datePicker.setValue(LocalDate.now());
        for (int i = 1; i <= 20; i++) {
            tableChoiceBox.getItems().add(i);
        }
        tableChoiceBox.setValue(1);
        totalGeneralField.setEditable(false);
        productChoiceBox.setOnAction(e -> updateProductInfo());
    }

    private void setupTable() {
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        setupActionsColumn();

        factureItemsTable.setItems(factureItems);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteButton.setOnAction(event -> {
                    FactureItem item = getTableView().getItems().get(getIndex());
                    if (item != null) {
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirmer la suppression");
                        confirmation.setHeaderText("Voulez-vous vraiment supprimer l'article : " + item.getProductName() + " ?");
                        confirmation.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                handleDeleteItem(item);
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                }
            }
        });
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productChoiceBox.getItems().clear();
            
            for (Product product : products) {
                if (product.getQuantity() > 0) { // Seulement les produits en stock
                    productChoiceBox.getItems().add(product.getName());
                }
            }
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des produits", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateProductInfo() {
        String selectedProductName = productChoiceBox.getValue();
        if (selectedProductName != null) {
            try {
                List<Product> products = productService.getAllProducts();
                Product selectedProduct = products.stream()
                    .filter(p -> p.getName().equals(selectedProductName))
                    .findFirst()
                    .orElse(null);
                
                if (selectedProduct != null) {
                    unitPriceField.setText(String.valueOf(selectedProduct.getPrice()));
                }
            } catch (Exception e) {
                showAlert("Erreur lors de la récupération des informations produit", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            if (!validateItemFields()) return;

            String productName = productChoiceBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            double unitPrice = Double.parseDouble(unitPriceField.getText());

            if (!checkStockAvailability(productName, quantity)) {
                showAlert("Stock insuffisant pour ce produit.", Alert.AlertType.WARNING);
                return;
            }

            FactureItem item = new FactureItem(0, productName, quantity, unitPrice);
            factureItems.add(item);

            updateTotalGeneral();
            clearItemFields();
            showAlert("Article ajouté avec succès", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer des valeurs numériques valides", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur lors de l'ajout de l'article", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleDeleteItem(FactureItem item) {
        if (item == null) return;
        factureItems.remove(item);
        updateTotalGeneral();
    }

    @FXML
    private void handleSaveFacture() {
        try {
            if (!validateFactureFields()) return;

            currentFacture.setClientName(clientNameField.getText());
            currentFacture.setTableNumber(tableChoiceBox.getValue());
            currentFacture.setDate(datePicker.getValue());
            currentFacture.setStatus("Payée"); // La facture est considérée comme payée lors de l'enregistrement
            currentFacture.setIsPaid(true);
            currentFacture.setItems(factureItems);

            Facture createdFacture = factureService.createFactureWithDetails(currentFacture);

            if (createdFacture != null) {
                // Étape cruciale : Déduire les articles vendus du stock
                updateStockAfterSale(createdFacture);

                showAlert("Facture enregistrée et stock mis à jour avec succès !", Alert.AlertType.INFORMATION);
                clearAllFields();
                loadProducts(); // Recharger les produits pour mettre à jour la liste (ceux en rupture disparaîtront)
                currentFacture = new Facture();
            } else {
                showAlert("Erreur lors de la création de la facture", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Erreur lors de la sauvegarde : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Met à jour le stock pour chaque article vendu dans une facture.
     * @param facture La facture qui a été payée.
     */
    private void updateStockAfterSale(Facture facture) {
        String user = Session.getInstance().getUser() != null ? Session.getInstance().getUser().getUsername() : "Système";
        String reason = "Vente - Facture #" + facture.getId();

        for (FactureItem item : facture.getItems()) {
            try {
                Product product = productService.getProductByName(item.getProductName());
                if (product != null) {
                    stockMovementService.processStockExit(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        reason,
                        user
                    );
                }
            } catch (SQLException e) {
                // Log l'erreur mais ne bloque pas le processus pour les autres articles
                System.err.println("Erreur lors de la mise à jour du stock pour le produit : " + item.getProductName());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handlePrintFacture() {
        if (factureItems.isEmpty()) {
            showAlert("Aucun article à imprimer", Alert.AlertType.WARNING);
            return;
        }

        // TODO: Implémenter l'impression de la facture
        showAlert("Fonctionnalité d'impression en cours de développement", Alert.AlertType.INFORMATION);
    }

    private boolean validateItemFields() {
        if (productChoiceBox.getValue() == null || productChoiceBox.getValue().isEmpty()) {
            showAlert("Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return false;
        }
        if (quantityField.getText().isEmpty()) {
            showAlert("Veuillez entrer une quantité", Alert.AlertType.WARNING);
            return false;
        }
        if (unitPriceField.getText().isEmpty()) {
            showAlert("Veuillez entrer un prix unitaire", Alert.AlertType.WARNING);
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(unitPriceField.getText());
            if (quantity <= 0 || price <= 0) {
                showAlert("La quantité et le prix doivent être positifs", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer des valeurs numériques valides", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateFactureFields() {
        if (clientNameField.getText().isEmpty()) {
            showAlert("Veuillez entrer le nom du client", Alert.AlertType.WARNING);
            return false;
        }
        if (factureItems.isEmpty()) {
            showAlert("Veuillez ajouter au moins un article", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean checkStockAvailability(String productName, int requestedQuantity) {
        try {
            List<Product> products = productService.getAllProducts();
            Product product = products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .orElse(null);
            
            return product != null && product.getQuantity() >= requestedQuantity;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateTotalGeneral() {
        double total = factureItems.stream()
            .mapToDouble(FactureItem::getTotalPrice)
            .sum();
        totalGeneralField.setText(String.format("%.0f FCFA", total));
        currentFacture.setTotalAmount(total);
    }

    private void clearItemFields() {
        productChoiceBox.getSelectionModel().clearSelection();
        quantityField.clear();
        unitPriceField.clear();
    }

    @FXML
    private void clearAllFields() {
        clientNameField.clear();
        tableChoiceBox.setValue(1);
        datePicker.setValue(LocalDate.now());
        factureItems.clear();
        totalGeneralField.setText("0");
        clearItemFields();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

