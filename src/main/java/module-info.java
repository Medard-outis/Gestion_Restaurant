module org.example.gestion_restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires com.jfoenix;
    requires org.controlsfx.controls;

    exports org.example.gestion_restaurant;
    exports org.example.gestion_restaurant.controllers;
    exports org.example.gestion_restaurant.models;
    exports org.example.gestion_restaurant.services;
    exports org.example.gestion_restaurant.utils;

    opens org.example.gestion_restaurant.controllers to javafx.fxml;
    opens org.example.gestion_restaurant.models to javafx.base;
}