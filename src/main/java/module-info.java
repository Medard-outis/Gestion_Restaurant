module org.example.gestion_restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jbcrypt;
    requires com.jfoenix;

    // Exportez le package contenant vos contrôleurs
    exports controller to javafx.fxml;
    exports org;

    // Ouvrez le package contenant vos fichiers FXML si nécessaire
    opens models to javafx.base; // 👈 autorise javafx.base à accéder au package models
    opens controller to javafx.fxml;
    opens org to javafx.fxml;
    exports controllers to javafx.fxml;
    opens controllers to javafx.fxml;

}