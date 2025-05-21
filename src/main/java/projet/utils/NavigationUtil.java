package projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.net.URL;

import java.io.IOException;
import java.util.Optional;

public class NavigationUtil {

    public static void ouvrirNouvelleFenetre(String cheminFXML, String titre, Stage currentStage, Object data) {
        try {
            // Débogage : imprimer le chemin exact
            System.out.println("Chemin FXML tenté : " + cheminFXML);

            // Vérifier si la ressource existe réellement
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("ERREUR : Fichier FXML introuvable - " + cheminFXML);
                afficherErreur("Impossible de charger la fenêtre. Fichier introuvable : " + cheminFXML);
                return;
            }

            // Utiliser l'URL trouvée pour charger
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            // Reste du code inchangé
            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre : " + e.getMessage());
        }
    }

    public static void changerScene(Stage stage, String cheminFXML, String titre, Object data) {
        try {
            System.out.println("Chemin FXML tenté (changer scène) : " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("ERREUR : Fichier FXML introuvable - " + cheminFXML);
                afficherErreur("Impossible de changer la scène. Fichier introuvable : " + cheminFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }


            stage.setTitle(titre);
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML (changer scène) : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de changer la scène : " + e.getMessage());
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

    public static void afficherSucces(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Ou AlertType.CONFIRMATION selon le besoin
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static boolean demanderConfirmation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null); // Pas d'en-tête, on utilise le titre
        alert.setContentText(message);

        // Personnalisation des boutons (optionnel)
        ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        // Affichage de la boîte de dialogue et attente de la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOui;
    }

}
