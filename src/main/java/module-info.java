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
    exports projet.controleurs.admin;
    opens projet.controleurs.admin to javafx.fxml;
    exports projet.utils;
    opens projet.utils to javafx.fxml;
    exports projet.controleurs.professeur;
    opens projet.controleurs.professeur to javafx.fxml;
    exports projet.controleurs.eleve;
    opens projet.controleurs.eleve to javafx.fxml;
}