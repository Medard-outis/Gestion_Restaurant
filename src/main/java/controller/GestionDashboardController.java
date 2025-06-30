package controller;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Product;
import services.ProductService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GestionDashboardController implements Initializable {

    @FXML private JFXButton printButton;
    @FXML private ChoiceBox<String> filterChoiceBox;
    @FXML private ChoiceBox<String> categorieChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Product> productsTable;
    @FXML private TextField designationField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private TextField filterField;

    @FXML private TableColumn<Product, String> colDesignation;
    @FXML private TableColumn<Product, String> colCategorie;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Double> colPrixUnitaire;
    @FXML private TableColumn<Product, Double> colPrixTotal;
    @FXML private TableColumn<Product, String> colDate;

    @FXML private VBox tableContainer; // Needed for printing

    private ProductService productService;
    private Product selectedProduct;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            productService = new ProductService();
            filterChoiceBox.getItems().addAll("Tous", "Aliments", "Boissons", "Desserts");
            categorieChoiceBox.getItems().addAll("Aliments", "Boissons", "Desserts");

            colDesignation.setCellValueFactory(new PropertyValueFactory<>("name"));
            colCategorie.setCellValueFactory(new PropertyValueFactory<>("category"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("price"));
            colPrixTotal.setCellValueFactory(data -> new ReadOnlyDoubleWrapper(data.getValue().getPrice() * data.getValue().getQuantity()).asObject());
            colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));

            // Appliquer le style à la colonne de stock
            colStock.setCellFactory(column -> new TableCell<Product, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        Product product = getTableView().getItems().get(getIndex());
                        if (product.getQuantity() == 0) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else if (product.getQuantity() < 10) {
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            filterChoiceBox.getSelectionModel().selectFirst();
            filterField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());
            filterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterProducts());

            productsTable.setOnMouseClicked(this::onRowSelect);
            refreshProductTable();

        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void onRowSelect(MouseEvent event) {
        selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            designationField.setText(selectedProduct.getName());
            categorieChoiceBox.setValue(selectedProduct.getCategory());
            quantityField.setText(String.valueOf(selectedProduct.getQuantity()));
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
            datePicker.setValue(selectedProduct.getDate());
        }
    }

    @FXML
    private void handleAddProduct() {
        try {
            if (!validateFields()) return;

            // Vérifier si le produit existe déjà
            String productName = designationField.getText().trim();
            boolean productExists = productService.getAllProducts().stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(productName));

            if (productExists) {
                showAlert("Un produit avec ce nom existe déjà. Veuillez utiliser un autre nom.", Alert.AlertType.ERROR);
                return;
            }

            Product product = new Product(
                    designationField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(quantityField.getText()),
                    categorieChoiceBox.getValue(),
                    datePicker.getValue()
            );
            if (productService.addProduct(product)) {
                productService.logHistorique("Ajout", product.getName(), LocalDate.now());
                refreshProductTable();
                clearFields();
                showAlert("Produit ajouté avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer des valeurs numériques valides");
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateProduct() {
        if (selectedProduct == null) {
            showAlert("Aucun produit sélectionné");
            return;
        }
        try {
            if (!validateFields()) return;
            selectedProduct.setName(designationField.getText());
            selectedProduct.setCategory(categorieChoiceBox.getValue());
            selectedProduct.setQuantity(Integer.parseInt(quantityField.getText()));
            selectedProduct.setPrice(Double.parseDouble(priceField.getText()));
            selectedProduct.setDate(datePicker.getValue());
            if (productService.updateProduct(selectedProduct)) {
                productService.logHistorique("Modification", selectedProduct.getName(), LocalDate.now());
                refreshProductTable();
                clearFields();
                showAlert("Produit mis à jour", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur de mise à jour");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProduct() {
        if (selectedProduct == null) {
            showAlert("Aucun produit sélectionné");
            return;
        }
        TextInputDialog motifDialog = new TextInputDialog();
        motifDialog.setTitle("Suppression du produit");
        motifDialog.setHeaderText("Motif de la suppression");
        motifDialog.setContentText("Veuillez entrer le motif :");
        motifDialog.showAndWait().ifPresent(motif -> {
            if (motif.trim().isEmpty()) {
                showAlert("Le motif est obligatoire");
                return;
            }
            try {
                boolean historiqueOk = productService.enregistrerSuppressionHistorique(
                        selectedProduct.getId(),
                        selectedProduct.getName(),
                        motif,
                        LocalDate.now()
                );
                if (historiqueOk && productService.deleteProduct(selectedProduct.getId())) {
                    refreshProductTable();
                    clearFields();
                    showAlert("Produit supprimé et motif enregistré", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur lors de la suppression ou de l’enregistrement");
                }
            } catch (Exception e) {
                showAlert("Erreur lors de la suppression");
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handlePrintTable() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(productsTable.getScene().getWindow())) {
            boolean success = job.printPage(tableContainer);
            if (success) {
                job.endJob();
                showAlert("Impression réussie", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Échec de l'impression");
            }
        }
    }

    private boolean validateFields() {
        String designation = designationField.getText();
        String categorie = categorieChoiceBox.getValue();
        String quantiteStr = quantityField.getText();
        String prixStr = priceField.getText();
        LocalDate date = datePicker.getValue();

        if (designation == null || designation.trim().isEmpty()) {
            showAlert("Le champ Désignation ne peut pas être vide.", Alert.AlertType.WARNING);
            return false;
        }
        if (categorie == null || categorie.isEmpty()) {
            showAlert("Veuillez sélectionner une catégorie.", Alert.AlertType.WARNING);
            return false;
        }
        if (quantiteStr == null || quantiteStr.trim().isEmpty()) {
            showAlert("Le champ Quantité ne peut pas être vide.", Alert.AlertType.WARNING);
            return false;
        }
        if (prixStr == null || prixStr.trim().isEmpty()) {
            showAlert("Le champ Prix Unitaire ne peut pas être vide.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int quantite = Integer.parseInt(quantiteStr);
            if (quantite < 0) {
                showAlert("La quantité ne peut pas être négative.", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("La quantité doit être un nombre entier valide.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double prix = Double.parseDouble(prixStr);
            if (prix < 0) {
                showAlert("Le prix ne peut pas être négatif.", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Le prix doit être un nombre valide.", Alert.AlertType.WARNING);
            return false;
        }

        if (date == null) {
            showAlert("Veuillez sélectionner une date.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void refreshProductTable() {
        try {
            productsTable.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des produits");
            e.printStackTrace();
        }
    }

    private void filterProducts() {
        String keyword = filterField.getText().toLowerCase();
        String filter = filterChoiceBox.getValue();
        List<Product> filtered = productService.getAllProducts().stream()
                .filter(p -> (keyword.isEmpty() ||
                        p.getName().toLowerCase().contains(keyword) ||
                        p.getCategory().toLowerCase().contains(keyword)) &&
                        ("Tous".equals(filter) || p.getCategory().equals(filter)))
                .collect(Collectors.toList());
        productsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void clearFields() {
        designationField.clear();
        categorieChoiceBox.getSelectionModel().clearSelection();
        quantityField.clear();
        priceField.clear();
        datePicker.setValue(null);
        selectedProduct = null;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        showAlert(message, Alert.AlertType.ERROR);
    }
}
