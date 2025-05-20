package projet.utils;

import javafx.stage.Stage;

/**
 * Interface permettant de transmettre l'objet Stage de la fenêtre au contrôleur.
 */
public interface TransmissibleStage {
    /**
     * Méthode pour définir l'objet Stage de la fenêtre actuelle dans le contrôleur.
     * @param stage L'objet Stage de la fenêtre.
     */
    void setStage(Stage stage);
}