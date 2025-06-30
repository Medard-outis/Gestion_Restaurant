package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Product;
import services.FactureService;
import services.ProductService;
import utils.Session;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label dailySalesLabel;
    @FXML private Label dailyOrdersLabel;
    @FXML private Label topProductLabel;

    // Tableau pour le stock faible
    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;

    private FactureService factureService;
    private ProductService productService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            factureService = new FactureService();
            productService = new ProductService();

            setupDashboard();
            loadDashboardData();

        } catch (SQLException e) {
            e.printStackTrace();
            // Afficher une alerte à l'utilisateur en cas d'erreur de BDD
            welcomeLabel.setText("Erreur de connexion à la base de données.");
        }
    }

    private void setupDashboard() {
        // Message de bienvenue et date
        String userName = Session.getInstance().getUser() != null ? Session.getInstance().getUser().getUsername() : "Utilisateur";
        welcomeLabel.setText("Bonjour, " + userName + " !");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // Configuration du tableau de stock faible
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Appliquer le style de couleur pour le stock
        colStock.setCellFactory(column -> new TableCell<>() {
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
                        setStyle("");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        LocalDate today = LocalDate.now();

        try {
            // Indicateurs de ventes
            double dailyRevenue = factureService.getTotalRevenueByDate(today);
            dailySalesLabel.setText(String.format("%.0f FCFA", dailyRevenue));

            int dailyOrders = factureService.getOrderCountByDate(today);
            dailyOrdersLabel.setText(String.valueOf(dailyOrders));

            String topProduct = factureService.getTopSellingProductByDate(today);
            topProductLabel.setText(topProduct != null ? topProduct : "-");

            // Données du stock faible
            List<Product> lowStockProducts = productService.getAllProducts().stream()
                    .filter(p -> p.getQuantity() < 10)
                    .collect(Collectors.toList());

            lowStockTable.setItems(FXCollections.observableArrayList(lowStockProducts));

        } catch (Exception e) {
            e.printStackTrace();
            // Gérer les erreurs d'affichage
            dailySalesLabel.setText("Erreur");
            dailyOrdersLabel.setText("Erreur");
            topProductLabel.setText("Erreur");
        }
    }

    // Cette méthode peut être appelée depuis le DashBoardController pour rafraîchir les données
    public void refreshData() {
        loadDashboardData();
    }
}