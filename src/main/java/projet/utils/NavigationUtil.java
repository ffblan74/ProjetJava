package projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {

    public static void ouvrirNouvelleFenetre(String cheminFXML, String titre, Stage currentStage, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(cheminFXML));
            Parent root = loader.load();

            // Récupérer le contrôleur et lui transmettre les données
            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            // Configurer la scène et l'afficher
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Méthode pour afficher un message d'erreur à l'utilisateur
    public static void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void afficherInformation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null); // Pas d'en-tête pour un simple message d'information
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean afficherConfirmation(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
        alerte.titleProperty().set(titre);
        alerte.headerTextProperty().set(null); // Pas d'en-tête
        alerte.contentTextProperty().set(message);

        return alerte.showAndWait().orElse(null) == ButtonType.OK;
    }
}
