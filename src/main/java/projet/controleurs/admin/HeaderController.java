package projet.controleurs.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

public class HeaderController implements Transmissible {

    @FXML private Label nomUtilisateurLabel;
    @FXML private Button btnGererUtilisateurs;
    @FXML private Button btnGererCours;
    @FXML private Button btnGererSalles;
    @FXML private Button btnGererEmploiDuTemps;

    private Utilisateur utilisateurConnecte; // Pour stocker l'utilisateur connecté

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
            if (nomUtilisateurLabel != null) {
                // CORRECTION ICI : Changement de getUtilisateurConnecte() en getNomUtilisateur()
                // Ceci suppose que ta classe Utilisateur a bien une méthode getNomUtilisateur()
                nomUtilisateurLabel.setText("Bonjour, " + utilisateurConnecte.getUtilisateurConnecte());
            }
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour revenir à la connexion sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }

    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        System.out.println("Naviguer vers la gestion des utilisateurs.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour changer la scène sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-utilisateur.fxml", "Gestion des Utilisateurs", utilisateurConnecte);
    }

    @FXML
    private void handleGererCours(ActionEvent event) {
        System.out.println("Naviguer vers la gestion des cours.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour changer la scène sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-cours.fxml", "Gestion des Cours", utilisateurConnecte);
    }

    @FXML
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Naviguer vers la gestion des salles.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour changer la scène sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-salles.fxml", "Gestion des Salles", utilisateurConnecte);
    }

    @FXML
    private void handleGererEmploiDuTemps(ActionEvent event) {
        System.out.println("Naviguer vers la gestion de l'emploi du temps.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour changer la scène sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-emploi-du-temps.fxml", "Gestion Emploi du Temps", utilisateurConnecte);
    }
}