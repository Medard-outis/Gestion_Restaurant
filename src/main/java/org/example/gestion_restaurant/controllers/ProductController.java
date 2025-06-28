package org.example.gestion_restaurant.controllers;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.gestion_restaurant.models.Product;
import org.example.gestion_restaurant.services.ProductService;
import org.example.gestion_restaurant.utils.AlertUtils;
import org.example.gestion_restaurant.utils.ValidationUtils;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ChoiceBox<String> categoryChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> filterChoiceBox;

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Integer> colQuantity;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colTotalValue;
    @FXML private TableColumn<Product, String> colDate;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private ProductService productService;
    private Product selectedProduct;
    private ObservableList<Product> productList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            productService = new ProductService();
            setupUI();
            setupTable();
            loadProducts();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void setupUI() {
        // Configuration des catégories
        categoryChoiceBox.getItems().addAll("Aliments", "Boissons", "Desserts", "Entrées", "Snacks");
        
        // Configuration des filtres
        filterChoiceBox.getItems().addAll("Tous", "Aliments", "Boissons", "Desserts", "Entrées", "Snacks", "Stock faible", "Rupture de stock");
        filterChoiceBox.setValue("Tous");
        
        // Date par défaut
        datePicker.setValue(LocalDate.now());
        
        // Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());
        filterChoiceBox.setOnAction(e -> filterProducts());
        
        // Validation en temps réel
        setupValidation();
    }

    private void setupValidation() {
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldVal);
            }
        });
        
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(oldVal);
            }
        });
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colTotalValue.setCellValueFactory(data -> 
            new ReadOnlyDoubleWrapper(data.getValue().getPrice() * data.getValue().getQuantity()).asObject());
        colDate.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDate().toString()));

        // Style pour les quantités
        colQuantity.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item == 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item < 10) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        productsTable.setOnMouseClicked(this::onRowSelect);
    }

    private void onRowSelect(MouseEvent event) {
        selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            populateFields(selectedProduct);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    private void populateFields(Product product) {
        nameField.setText(product.getName());
        categoryChoiceBox.setValue(product.getCategory());
        quantityField.setText(String.valueOf(product.getQuantity()));
        priceField.setText(String.valueOf(product.getPrice()));
        datePicker.setValue(product.getDate());
    }

    @FXML
    private void handleAddProduct() {
        if (!validateFields()) return;

        try {
            Product product = createProductFromFields();
            if (productService.addProduct(product)) {
                AlertUtils.showSuccess("Produit ajouté avec succès");
                loadProducts();
                clearFields();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'ajouter le produit");
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateProduct() {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un produit à modifier");
            return;
        }

        if (!validateFields()) return;

        try {
            updateProductFromFields(selectedProduct);
            if (productService.updateProduct(selectedProduct)) {
                AlertUtils.showSuccess("Produit mis à jour avec succès");
                loadProducts();
                clearFields();
            } else {
                AlertUtils.showError("Erreur", "Impossible de mettre à jour le produit");
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProduct() {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un produit à supprimer");
            return;
        }

        if (!AlertUtils.confirmDelete(selectedProduct.getName())) {
            return;
        }

        String reason = AlertUtils.showTextInput("Motif de suppression", 
            "Veuillez entrer le motif de la suppression:", "").orElse("");
        
        if (reason.trim().isEmpty()) {
            AlertUtils.showWarning("Motif requis", "Le motif de suppression est obligatoire");
            return;
        }

        try {
            boolean historiqueOk = productService.enregistrerSuppressionHistorique(
                selectedProduct.getId(),
                selectedProduct.getName(),
                reason,
                LocalDate.now()
            );

            if (historiqueOk && productService.deleteProduct(selectedProduct.getId())) {
                AlertUtils.showSuccess("Produit supprimé avec succès");
                loadProducts();
                clearFields();
            } else {
                AlertUtils.showError("Erreur", "Impossible de supprimer le produit");
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        if (!ValidationUtils.isValidProductName(nameField.getText())) {
            AlertUtils.showWarning("Validation", "Veuillez entrer un nom de produit valide");
            nameField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidPrice(priceField.getText())) {
            AlertUtils.showWarning("Validation", "Veuillez entrer un prix valide");
            priceField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidQuantity(quantityField.getText())) {
            AlertUtils.showWarning("Validation", "Veuillez entrer une quantité valide");
            quantityField.requestFocus();
            return false;
        }

        if (categoryChoiceBox.getValue() == null) {
            AlertUtils.showWarning("Validation", "Veuillez sélectionner une catégorie");
            categoryChoiceBox.requestFocus();
            return false;
        }

        if (datePicker.getValue() == null) {
            AlertUtils.showWarning("Validation", "Veuillez sélectionner une date");
            datePicker.requestFocus();
            return false;
        }

        return true;
    }

    private Product createProductFromFields() {
        return new Product(
            nameField.getText().trim(),
            Double.parseDouble(priceField.getText()),
            Integer.parseInt(quantityField.getText()),
            categoryChoiceBox.getValue(),
            datePicker.getValue()
        );
    }

    private void updateProductFromFields(Product product) {
        product.setName(nameField.getText().trim());
        product.setPrice(Double.parseDouble(priceField.getText()));
        product.setQuantity(Integer.parseInt(quantityField.getText()));
        product.setCategory(categoryChoiceBox.getValue());
        product.setDate(datePicker.getValue());
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productList = FXCollections.observableArrayList(products);
            productsTable.setItems(productList);
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors du chargement des produits");
            e.printStackTrace();
        }
    }

    private void filterProducts() {
        if (productList == null) return;

        String searchText = searchField.getText().toLowerCase();
        String filter = filterChoiceBox.getValue();

        List<Product> filtered = productList.stream()
            .filter(product -> {
                // Filtre de recherche
                boolean matchesSearch = searchText.isEmpty() ||
                    product.getName().toLowerCase().contains(searchText) ||
                    product.getCategory().toLowerCase().contains(searchText);

                // Filtre de catégorie/statut
                boolean matchesFilter = switch (filter) {
                    case "Tous" -> true;
                    case "Stock faible" -> product.isLowStock();
                    case "Rupture de stock" -> product.isOutOfStock();
                    default -> product.getCategory().equals(filter);
                };

                return matchesSearch && matchesFilter;
            })
            .collect(Collectors.toList());

        productsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        quantityField.clear();
        categoryChoiceBox.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        selectedProduct = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        productsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleClearFields() {
        clearFields();
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
        searchField.clear();
        filterChoiceBox.setValue("Tous");
    }
}