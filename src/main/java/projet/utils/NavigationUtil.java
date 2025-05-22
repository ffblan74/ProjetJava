package projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality; // NOUVEAU : Import pour Modality
import javafx.stage.Stage;
import java.net.URL;

import java.io.IOException;
import java.util.Optional;

public class NavigationUtil {

    // Cette méthode existe déjà dans votre code.
    // Elle est destinée aux cas où vous voulez ouvrir une nouvelle fenêtre ET fermer l'ancienne.
    // Pour votre cas de "création de cours modale", cette méthode NE DOIT PAS être utilisée,
    // car elle ferme la fenêtre d'accueil.
    public static void ouvrirNouvelleFenetre(String cheminFXML, String titre, Stage currentStage, Object data) {
        try {
            System.out.println("DEBUG (NavigationUtil): Tentative d'ouverture de nouvelle fenêtre (ouvrirNouvelleFenetre - *ferme l'ancienne*): " + cheminFXML);

            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("ERREUR (NavigationUtil): Fichier FXML introuvable - " + cheminFXML);
                afficherErreur("Impossible de charger la fenêtre. Fichier introuvable : " + cheminFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

            // C'est cette ligne qui ferme la fenêtre d'accueil si vous l'utilisez pour "Ajouter Cours".
            // POUR LE CAS DE LA FENÊTRE MODALE D'AJOUT DE COURS, C'EST POURQUOI NOUS UTILISONS LA NOUVELLE MÉTHODE CI-DESSOUS.
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre : " + e.getMessage());
        }
    }

    // Cette méthode est celle que vous allez utiliser pour la fenêtre de création de cours.
    // Elle ouvre une fenêtre enfant MODALE, qui bloque l'interactivité avec la fenêtre parente
    // et ne ferme PAS la fenêtre parente.
    public static void ouvrirFenetreModale(String cheminFXML, String titre, Stage parentStage, Object data) {
        try {
            System.out.println("DEBUG (NavigationUtil): Tentative d'ouverture de fenêtre MODALE: " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("ERREUR (NavigationUtil): Fichier FXML introuvable - " + cheminFXML);
                afficherErreur("Impossible de charger la fenêtre. Fichier introuvable : " + cheminFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            Stage childStage = new Stage(); // C'est la nouvelle fenêtre enfant modale
            childStage.setTitle(titre);
            childStage.setScene(new Scene(root));
            childStage.initOwner(parentStage); // Définit la fenêtre parente
            childStage.initModality(Modality.WINDOW_MODAL); // Rend la fenêtre modale par rapport au parent
            childStage.showAndWait(); // Affiche la fenêtre et bloque l'exécution jusqu'à sa fermeture

            System.out.println("DEBUG (NavigationUtil): Fenêtre modale fermée (showAndWait). Retour au parent.");

        } catch (IOException e) {
            System.err.println("ERREUR (NavigationUtil): Erreur de chargement FXML de la fenêtre modale : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre modale : " + e.getMessage());
        }
    }

    // Cette méthode change la scène de la Stage ACTUELLE, elle ne crée pas de nouvelle fenêtre.
    // Elle est utilisée pour la navigation au sein de la même fenêtre (ex: Login vers Accueil).
    public static void changerScene(Stage stage, String cheminFXML, String titre, Object data) {
        try {
            System.out.println("DEBUG (NavigationUtil): Tentative de changement de scène: " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("ERREUR (NavigationUtil): Fichier FXML introuvable - " + cheminFXML);
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
            System.out.println("DEBUG (NavigationUtil): Scène changée avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML (changer scène) : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de changer la scène : " + e.getMessage());
        }
    }

    // Méthodes pour afficher des alertes (inchangées)
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean afficherConfirmation(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
        alerte.titleProperty().set(titre);
        alerte.headerTextProperty().set(null);
        alerte.contentTextProperty().set(message);

        return alerte.showAndWait().orElse(null) == ButtonType.OK;
    }

    public static void afficherSucces(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static boolean demanderConfirmation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOui;
    }
}