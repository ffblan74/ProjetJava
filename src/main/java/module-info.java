module projet.projetjava {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens projet.projetjava to javafx.fxml;
    exports projet.projetjava;
}