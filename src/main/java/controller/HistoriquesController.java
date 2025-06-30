package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import models.Facture;
import models.StockMovement;
// Assuming a UserActivity model exists
// import models.UserActivity;
import services.FactureService;
import services.StockMovementService;
// import services.UserActivityService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HistoriquesController implements Initializable {

    // Common
    @FXML private TabPane historyTabPane;

    // Ventes Tab
    @FXML private TextField searchFieldVentes;
    @FXML private DatePicker filterDatePickerVentes;
    @FXML private Label totalRevenueLabel;
    @FXML private TableView<Facture> ventesTable;
    @FXML private TableColumn<Facture, String> colClientName;
    @FXML private TableColumn<Facture, Integer> colTableNumber;
    @FXML private TableColumn<Facture, String> colDate;
    @FXML private TableColumn<Facture, Double> colMontant;
    @FXML private TableColumn<Facture, String> colStatus;

    // Stock Tab
    @FXML private TextField searchFieldStock;
    @FXML private DatePicker filterDatePickerStock;
    @FXML private ChoiceBox<String> filterTypeChoiceBox;
    @FXML private TableView<StockMovement> stockTable;
    @FXML private TableColumn<StockMovement, String> colProductName;
    @FXML private TableColumn<StockMovement, String> colMovementType;
    @FXML private TableColumn<StockMovement, Integer> colQuantity;
    @FXML private TableColumn<StockMovement, String> colReason;
    @FXML private TableColumn<StockMovement, String> colMovementDate;
    @FXML private TableColumn<StockMovement, String> colUser;

    // User Activity Tab (assuming model and service exist)
    @FXML private TableView<Object> userActivityTable; // Replace Object with UserActivity
    @FXML private TableColumn<Object, String> colUserActivity;
    @FXML private TableColumn<Object, String> colAction;
    @FXML private TableColumn<Object, String> colTimestamp;


    private FactureService factureService;
    private StockMovementService stockMovementService;
    // private UserActivityService userActivityService;

    private ObservableList<Facture> ventesData = FXCollections.observableArrayList();
    private ObservableList<StockMovement> stockData = FXCollections.observableArrayList();
    // private ObservableList<UserActivity> userActivityData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            factureService = new FactureService();
            stockMovementService = new StockMovementService();
            // userActivityService = new UserActivityService();

            setupVentesTab();
            setupStockTab();
            // setupUserActivityTab();

            loadAllData();

        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupVentesTab() {
        colClientName.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colTableNumber.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        ventesTable.setItems(ventesData);

        searchFieldVentes.textProperty().addListener((obs, old, val) -> filterVentes());
        filterDatePickerVentes.valueProperty().addListener((obs, old, val) -> filterVentes());
    }

    private void setupStockTab() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colMovementType.setCellValueFactory(new PropertyValueFactory<>("movementType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colMovementDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userAction"));
        stockTable.setItems(stockData);

        filterTypeChoiceBox.getItems().addAll("Tous", "ENTREE", "SORTIE");
        filterTypeChoiceBox.setValue("Tous");

        searchFieldStock.textProperty().addListener((obs, old, val) -> filterStockMovements());
        filterDatePickerStock.valueProperty().addListener((obs, old, val) -> filterStockMovements());
        filterTypeChoiceBox.valueProperty().addListener((obs, old, val) -> filterStockMovements());
    }

    private void loadAllData() {
        loadVentesData();
        loadStockData();
        // loadUserActivityData();
    }

    private void loadVentesData() {
        try {
            ventesData.setAll(factureService.getAllFactures());
            updateTotalRevenue();
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des ventes", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadStockData() {
        try {
            stockData.setAll(stockMovementService.getAllMovements());
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des mouvements de stock", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void filterVentes() {
        try {
            List<Facture> allFactures = factureService.getAllFactures();
            String searchText = searchFieldVentes.getText().toLowerCase();
            LocalDate selectedDate = filterDatePickerVentes.getValue();

            List<Facture> filtered = allFactures.stream()
                .filter(facture -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        facture.getClientName().toLowerCase().contains(searchText) ||
                        String.valueOf(facture.getTableNumber()).contains(searchText);

                    boolean matchesDate = selectedDate == null ||
                        facture.getDate().equals(selectedDate);

                    return matchesSearch && matchesDate;
                })
                .collect(Collectors.toList());

            ventesData.setAll(filtered);
            updateTotalRevenue();

        } catch (Exception e) {
            showAlert("Erreur lors du filtrage des ventes.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void filterStockMovements() {
        try {
            List<StockMovement> allMovements = stockMovementService.getAllMovements();
            String searchText = searchFieldStock.getText().toLowerCase();
            LocalDate selectedDate = filterDatePickerStock.getValue();
            String selectedType = filterTypeChoiceBox.getValue();

            List<StockMovement> filtered = allMovements.stream()
                .filter(movement -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        movement.getProductName().toLowerCase().contains(searchText) ||
                        (movement.getUserAction() != null && movement.getUserAction().toLowerCase().contains(searchText));

                    boolean matchesDate = selectedDate == null ||
                        movement.getDate().equals(selectedDate);

                    boolean matchesType = "Tous".equals(selectedType) ||
                        movement.getMovementType().equals(selectedType);

                    return matchesSearch && matchesDate && matchesType;
                })
                .collect(Collectors.toList());

            stockData.setAll(filtered);

        } catch (Exception e) {
            showAlert("Erreur lors du filtrage des mouvements de stock.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateTotalRevenue() {
        double total = ventesData.stream()
            .mapToDouble(Facture::getTotalAmount)
            .sum();
        totalRevenueLabel.setText(String.format("Chiffre d'affaires affiché: %.0f FCFA", total));
    }

    @FXML
    private void handleRefresh() {
        searchFieldVentes.clear();
        filterDatePickerVentes.setValue(null);
        searchFieldStock.clear();
        filterDatePickerStock.setValue(null);
        filterTypeChoiceBox.setValue("Tous");
        loadAllData();
        showAlert("Données actualisées", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleExportVentes() {
        exportToCSV(ventesTable, "historique_ventes.csv");
    }

    @FXML
    private void handleExportStock() {
        exportToCSV(stockTable, "historique_stock.csv");
    }

    private <T> void exportToCSV(TableView<T> tableView, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                // Write header
                List<String> headers = tableView.getColumns().stream()
                    .map(TableColumnBase::getText)
                    .collect(Collectors.toList());
                writer.println(String.join(";", headers));

                // Write data rows
                for (T item : tableView.getItems()) {
                    List<String> row = tableView.getColumns().stream()
                        .map(col -> {
                            Object cellData = col.getCellData(item);
                            // Correctly wrap cell data in double quotes for CSV
                            return cellData != null ? "\"" + cellData.toString().replace("\"", "\"\"") + "\"" : "";
                        })
                        .collect(Collectors.toList());
                    writer.println(String.join(";", row));
                }
                showAlert("Exportation réussie !", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Erreur lors de l'exportation.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}