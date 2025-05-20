module projet.projetjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.commons.csv;

    exports projet;
    exports projet.controleurs;
    exports projet.controleurs.admin;
    exports projet.controleurs.professeur; // <<-- Assurez-vous que c'est là
    exports projet.models;
    exports projet.utils;

    opens projet to javafx.fxml;
    opens projet.controleurs to javafx.fxml;
    opens projet.controleurs.admin to javafx.fxml;
    opens projet.controleurs.professeur to javafx.fxml; // <<-- Assurez-vous que c'est là
    opens projet.models to javafx.fxml, javafx.base;
    opens projet.utils to javafx.fxml;
}