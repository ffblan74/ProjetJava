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
    exports projet.controleurs.professeur;
    exports projet.controleurs.eleve;
    exports projet.models;
    exports projet.utils;

    opens projet to javafx.fxml;
    opens projet.controleurs to javafx.fxml;
    opens projet.controleurs.admin to javafx.fxml;
    opens projet.controleurs.professeur to javafx.fxml;
    opens projet.controleurs.eleve to javafx.fxml; // <--- NOUVEL OPEN
    opens projet.models to javafx.fxml, javafx.base; // javafx.base est important pour TableView
    opens projet.utils to javafx.fxml;
}