package org;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.DatabaseInitializer;

import java.util.Objects;

public class Main extends Application {

    // Variables for dragging the window
    double x,y = 0;
    @Override
    public void start(Stage primaryStage) throws Exception{

         DatabaseInitializer.initialize();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/auth.fxml")));
        primaryStage.initStyle(StageStyle.UNDECORATED);

        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });


        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - x);
            primaryStage.setY(event.getScreenY() - y);
        });

        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
