package projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class NavigationUtil {

    public static void ouvrirNouvelleFenetre(String cheminFXML, String titre, Stage currentStage, Object data) {
        try {
            System.out.println("Tentative d'ouverture de nouvelle fenêtre (ferme l'ancienne) : " + cheminFXML);

            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("Fichier FXML introuvable : " + cheminFXML);
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

            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre : " + e.getMessage());
        }
    }

    public static void ouvrirFenetreModale(String cheminFXML, String titre, Stage parentStage, Object data) {
        try {
            System.out.println("Tentative d'ouverture de fenêtre modale (sans retour) : " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("Fichier FXML introuvable : " + cheminFXML);
                afficherErreur("Impossible de charger la fenêtre. Fichier introuvable : " + cheminFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            Stage childStage = new Stage();
            childStage.setTitle(titre);
            childStage.setScene(new Scene(root));
            childStage.initOwner(parentStage);
            childStage.initModality(Modality.WINDOW_MODAL);
            childStage.showAndWait();

            System.out.println("Fenêtre modale (sans retour) fermée. Retour au parent.");

        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML de la fenêtre modale : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre modale : " + e.getMessage());
        }
    }

    public static void ouvrirFenetreModaleAvecRetour(String cheminFXML, String titre, Stage parentStage, Object data, Consumer<Object> callback) {
        try {
            System.out.println("Tentative d'ouverture de fenêtre modale avec retour : " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("Fichier FXML introuvable : " + cheminFXML);
                afficherErreur("Impossible de charger la fenêtre. Fichier introuvable : " + cheminFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(data);
            }

            Stage childStage = new Stage();
            childStage.setTitle(titre);
            childStage.setScene(new Scene(root));
            childStage.initOwner(parentStage);
            childStage.initModality(Modality.WINDOW_MODAL);

            childStage.setOnHidden(event -> {
                if (controller instanceof TransmissibleRetour) {
                    Object returnedData = ((TransmissibleRetour) controller).getDonneesRetour();
                    if (callback != null) {
                        callback.accept(returnedData);
                    }
                } else if (callback != null) {
                    callback.accept(null);
                }
            });

            childStage.showAndWait();

            System.out.println("Fenêtre modale avec retour fermée. Retour au parent.");

        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML de la fenêtre modale : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger la fenêtre modale : " + e.getMessage());
        }
    }

    public static void changerScene(Stage stage, String cheminFXML, String titre, Object data) {
        try {
            System.out.println("Tentative de changement de scène : " + cheminFXML);
            URL resourceUrl = NavigationUtil.class.getResource(cheminFXML);
            if (resourceUrl == null) {
                System.err.println("Fichier FXML introuvable : " + cheminFXML);
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
            System.out.println("Scène changée avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML (changer scène) : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de changer la scène : " + e.getMessage());
        }
    }

    public static void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
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
}