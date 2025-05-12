module projet.projetjava {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens projet to javafx.fxml;
    exports projet;
    exports projet.controleurs;
    opens projet.controleurs to javafx.fxml;
    exports projet.models;
    opens projet.models to javafx.fxml;
}