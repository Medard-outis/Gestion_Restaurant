module org.example.gestion_restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires com.jfoenix;
    requires org.controlsfx.controls;

    opens controller to javafx.fxml;
    opens models to javafx.base;
    exports org;

}