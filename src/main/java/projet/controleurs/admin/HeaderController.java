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
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
            if (nomUtilisateurLabel != null) {
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
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Naviguer vers la gestion des salles.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour changer la scène sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-salles.fxml", "Gestion des Salles", utilisateurConnecte);
    }

}