module projet {
    requires javafx.controls;
    requires javafx.fxml;
    requires itextpdf;

    opens projet_java.model to com.fasterxml.jackson.databind;
    opens projet_java.view to javafx.fxml;

    exports projet;
    exports projet.models;
    exports projet.controleurs;
    exports projet.utils;
}
