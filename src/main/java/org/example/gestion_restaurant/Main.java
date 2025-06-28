package org.example.gestion_restaurant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.gestion_restaurant.utils.DatabaseInitializer;

import java.util.Objects;

public class Main extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser la base de données
        DatabaseInitializer.initialize();

        // Charger la vue de connexion
        Parent root = FXMLLoader.load(Objects.requireNonNull(
            getClass().getResource("/views/auth.fxml")));
        
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Restaurant Le Notre - Gestion");

        // Permettre le déplacement de la fenêtre
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        Scene scene = new Scene(root, 1150, 660);
        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/styles/application.css")).toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}