package projet.controleurs.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.NavigationUtil;

public class AccueilAdminController {

    @FXML
    private Button creerCoursButton;
    @FXML
    private Button modifierEmploiDuTempsButton;
    @FXML
    private Button gererCreneauxButton;
    @FXML
    private Button affecterEnseignantsButton;
    @FXML
    private Button gererSallesButton;
    @FXML
    private Button controlerConflitsButton;
    @FXML
    private Button statistiquesSallesButton;
    @FXML
    private Button statistiquesEnseignantsButton;
    @FXML
    private Button gererUtilisateursButton;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleCreerCours(ActionEvent event) {
        System.out.println("Bouton Créer un cours cliqué !");

    }

    @FXML
    private void handleModifierEmploiDuTemps(ActionEvent event) {
        System.out.println("Bouton Modifier l'emploi du temps cliqué !");

    }

    @FXML
    private void handleGererCreneaux(ActionEvent event) {
        System.out.println("Bouton Gérer les créneaux cliqué !");
    }

    @FXML
    private void handleAffecterEnseignants(ActionEvent event) {
        System.out.println("Bouton Affecter des enseignants cliqué !");
    }

    @FXML
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Bouton Gérer les salles cliqué !");
    }

    @FXML
    private void handleControlerConflits(ActionEvent event) {
        System.out.println("Bouton Contrôler les conflits cliqué !");
    }

    @FXML
    private void handleStatistiquesSalles(ActionEvent event) {
        System.out.println("Bouton Statistiques des salles cliqué !");
    }

    @FXML
    private void handleStatistiquesEnseignants(ActionEvent event) {
        System.out.println("Bouton Statistiques enseignants cliqué !");
    }

    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        System.out.println("Bouton Gérer les utilisateurs cliqué");
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/creer-utilisateur.fxml", "Créer un utilisateur", (Stage) gererUtilisateursButton.getScene().getWindow());
    }
}