package projet.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.List;


import java.io.IOException;
import projet.models.*;
import projet.controleurs.professeur.*;

public class NavigationUtil {

    /**
     * Ouvre une nouvelle fenêtre FXML et transmet des données et le Stage à son contrôleur.
     * Gère la réutilisation ou la création d'un Stage, et la fermeture de l'ancien si nécessaire.
     *
     * @param fxmlPath Le chemin vers le fichier FXML à charger (ex: "/projet/fxml/maPage.fxml").
     * @param title Le titre de la nouvelle fenêtre.
     * @param previousStage Le Stage de la fenêtre précédente (peut être null si c'est la première fenêtre).
     * @param dataToTransmit L'objet de données à transmettre au contrôleur (peut être null).
     */
    public static void ouvrirNouvelleFenetre(String fxmlPath, String title, Stage previousStage, Object dataToTransmit) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load(); // Charge le FXML, construit le graphe de scène

            Object controller = loader.getController();

            // 1. Transmet les données si le contrôleur implémente Transmissible
            if (controller instanceof Transmissible) {
                ((Transmissible) controller).transmettreDonnees(dataToTransmit);
            }

            Stage newStage;
            // 2. Décide de réutiliser le Stage précédent ou d'en créer un nouveau
            // On réutilise le previousStage si c'est la même fenêtre qui est mise à jour,
            // ou si on veut simplement changer le contenu FXML de la même fenêtre.
            // Si previousStage est null (premier écran) ou fermé, on en crée un nouveau.
            if (previousStage != null) { // && previousStage.isShowing() n'est pas nécessaire ici si on veut le réutiliser même s'il était caché
                newStage = previousStage;
            } else {
                newStage = new Stage();
            }

            // 3. Configure la scène et l'associe au Stage
            Scene scene = new Scene(root);
            newStage.setScene(scene);
            newStage.setTitle(title);
            newStage.show(); // Affiche la fenêtre (rend le Stage non-null et accessible via getScene().getWindow())

            // 4. Transmet le Stage au contrôleur si celui-ci implémente TransmissibleStage
            // C'est crucial : le Stage est passé *après* que la Scene est attachée au Stage et que show() a été appelé.
            if (controller instanceof TransmissibleStage) {
                ((TransmissibleStage) controller).setStage(newStage);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la fenêtre FXML : " + fxmlPath);
            e.printStackTrace();
            // Appel correct de la méthode afficherErreur avec 3 arguments
            afficherErreur("Erreur de chargement", "Impossible de charger la page.", "Une erreur est survenue lors du chargement de la page: " + e.getMessage());
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur avec un titre, un en-tête et un contenu spécifiques.
     * @param title Le titre de la boîte de dialogue d'erreur.
     * @param header Le texte d'en-tête de la boîte de dialogue (peut être null).
     * @param content Le message principal de l'erreur.
     */
    public static void afficherErreur(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Surcharge de afficherErreur pour compatibilité, si seulement un message est donné.
     * Définit un titre et un en-tête par défaut.
     * @param message Le message principal de l'erreur.
     */
    public static void afficherErreur(String message) {
        afficherErreur("Erreur", null, message); // Appelle la version à 3 arguments
    }

    /**
     * Affiche une boîte de dialogue de succès ou d'information avec un titre, un en-tête et un contenu spécifiques.
     * @param title Le titre de la boîte de dialogue.
     * @param header Le texte d'en-tête de la boîte de dialogue (peut être null).
     * @param content Le message principal.
     */
    public static void afficherSucces(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION); // Ou AlertType.CONFIRMATION selon le besoin
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation avec un titre et un message,
     * et retourne true si l'utilisateur clique sur "Oui", false sinon.
     * @param titre Le titre de la boîte de dialogue de confirmation.
     * @param message Le message à afficher dans la boîte de dialogue.
     * @return true si l'utilisateur clique sur "Oui", false sinon.
     */
    public static boolean demanderConfirmation(String titre, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
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
    private static boolean sauvegardeEffectuee = false;

    /**
     * Initialise et affiche la fenêtre de création ou modification de cours.
     * Appelle un contrôleur FXML dédié avec les données et callbacks nécessaires.
     *
     * @param stage        Le stage à utiliser pour afficher la fenêtre.
     * @param cours        Le cours à modifier (ou null pour un ajout).
     * @param chargerEtudiants Callback pour charger les étudiants.
     * @param chargerSalles    Callback pour charger les salles.
     * @param genererIdCours   Callback pour générer un nouvel ID de cours.
     */
    public static void initialiserEtAfficherCreerCour(Stage stage,
                                                      Cours cours,
                                                      Supplier<List<Etudiant>> chargerEtudiants,
                                                      Supplier<List<Salle>> chargerSalles,
                                                      Supplier<Integer> genererIdCours) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/projet/fxml/creercour-professeur.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof CreerCourController) {
                CreerCourController ctrl = (CreerCourController) controller;
                ctrl.setDialogStage(stage);
                ctrl.setEtudiantDataLoader(chargerEtudiants);
                ctrl.setSalleDataLoader(chargerSalles);
                ctrl.setProchainIdCoursSupplier(genererIdCours);
                ctrl.setCours(cours);
            }

            stage.setScene(new Scene(root));
            stage.showAndWait(); // Attend que la fenêtre soit fermée

        } catch (IOException e) {
            afficherErreur("Erreur", "Chargement du formulaire échoué", e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean estSauvegarde() {
        return sauvegardeEffectuee;
    }
}
