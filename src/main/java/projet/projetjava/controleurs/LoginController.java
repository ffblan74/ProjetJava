package projet.projetjava.controleurs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projet.projetjava.GestionnaireEmploiDuTemps;
import projet.projetjava.Utilisateur; // Import de la classe Utilisateur
import projet.projetjava.Etudiant;
import projet.projetjava.Enseignant;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField identifiantTextField;

    @FXML
    private PasswordField motDePassePasswordField;

    @FXML
    private Button connexionButton;

    @FXML
    private Label messageErreurLabel;

    private GestionnaireEmploiDuTemps gestionnaireEmploiDuTemps;

    public void setGestionnaireEmploiDuTemps(GestionnaireEmploiDuTemps gestionnaire) {
        this.gestionnaireEmploiDuTemps = gestionnaire;
    }

    @FXML
    public void handleConnexion(ActionEvent event) throws IOException {
        String identifiant = identifiantTextField.getText();
        String motDePasse = motDePassePasswordField.getText();

        Utilisateur utilisateurConnecte = null;
        if (gestionnaireEmploiDuTemps != null) {
            for (Utilisateur utilisateur : gestionnaireEmploiDuTemps.getAllUtilisateurs()) {
                if (utilisateur.getIdentifiant().equals(identifiant) && utilisateur.getMotDePasse().equals(motDePasse)) {
                    utilisateurConnecte = utilisateur;
                    break;
                }
            }
        } else {
            messageErreurLabel.setText("Erreur: Gestionnaire non initialisé.");
            return;
        }

        if (utilisateurConnecte != null) {
            messageErreurLabel.setText("");
            // Rediriger l'utilisateur en fonction de son rôle
            String role = utilisateurConnecte.getRole();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root;
            FXMLLoader loader;

            switch (role) {
                case "Etudiant":
                    loader = new FXMLLoader(getClass().getResource("/vues/etudiant_emploi_du_temps.fxml"));
                    root = loader.load();
                    EtudiantEmploiDuTempsController etudiantController = loader.getController();
                    etudiantController.setEtudiant((Etudiant) utilisateurConnecte);
                    etudiantController.setGestionnaireEmploiDuTemps(gestionnaireEmploiDuTemps);
                    break;
                case "Enseignant":
                    loader = new FXMLLoader(getClass().getResource("/vues/enseignant_emploi_du_temps.fxml"));
                    root = loader.load();
                    EnseignantEmploiDuTempsController enseignantController = loader.getController();
                    enseignantController.setEnseignant((Enseignant) utilisateurConnecte);
                    enseignantController.setGestionnaireEmploiDuTemps(gestionnaireEmploiDuTemps);
                    break;
                case "Administrateur":
                    loader = new FXMLLoader(getClass().getResource("/vues/administrateur_accueil.fxml"));
                    root = loader.load();
                    AdministrateurAccueilController adminController = loader.getController();
                    adminController.setGestionnaireEmploiDuTemps(gestionnaireEmploiDuTemps);
                    break;
                default:
                    messageErreurLabel.setText("Rôle d'utilisateur inconnu.");
                    return;
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } else {
            messageErreurLabel.setText("Identifiant ou mot de passe incorrect.");
        }
    }
}