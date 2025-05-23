package projet.controleurs.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

public class HeaderController implements Transmissible {

    @FXML private Label nomUtilisateurLabel;
    @FXML private Button btnGererUtilisateurs;
    @FXML private Button btnGererSalles;
    @FXML private Button btnGererEmploiDuTemps;
    @FXML private ImageView imageView;

    private Utilisateur utilisateurConnecte;

    @FXML
    public void initialize() {
        Image image = new Image(getClass().getResourceAsStream("/projet/images/SuperM.png"));
        imageView.setImage(image);
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
            if (nomUtilisateurLabel != null && utilisateurConnecte != null) {
                nomUtilisateurLabel.setText("Bonjour, " +
                        utilisateurConnecte.getPrenom() + " " +
                        utilisateurConnecte.getNom());
            }
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }

    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-utilisateur.fxml",
                "Gestion des Utilisateurs", utilisateurConnecte);
    }

    @FXML
    private void handleGererSalles(ActionEvent event) {
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-gerer-salles.fxml",
                "Gestion des Salles", utilisateurConnecte);
    }

    @FXML
    private void handleGererEmploiDuTemps(ActionEvent event) {
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/accueil-admin-emploi-du-temps.fxml",
                "Gestion Emploi du Temps", utilisateurConnecte);
    }
}