package projet.controleurs.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

public class AccueilAdminController implements Transmissible {

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
    private Label nomUtilisateurLabel; // Référence au Label (dans la vue)

    private Utilisateur utilisateurConnecte; // Stocke l'utilisateur connecté


    @FXML
    public void initialize() {
        // Initialisation de la page (si besoin)
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;

            // Afficher le nom de l'utilisateur dans le Label
            afficherNomUtilisateur();
        } else {
            System.err.println("Données incorrectes transmises au contrôleur AccueilAdminController.");
        }
    }

    /**
     * Met à jour le label pour afficher le nom de l'utilisateur.
     */
    private void afficherNomUtilisateur() {
        if (utilisateurConnecte != null) {
            nomUtilisateurLabel.setText("Bonjour, " + utilisateurConnecte.getNom() + " " + utilisateurConnecte.getPrenom());
        } else {
            nomUtilisateurLabel.setText("Utilisateur inconnu");
        }
    }


    /**
     * Initialise la page en fonction de l'utilisateur connecté.
     */
    private void initialiserPage() {
        if (utilisateurConnecte != null) {
            System.out.println("Bienvenue sur la page d'accueil admin, " + utilisateurConnecte.getRole() + " " + utilisateurConnecte.getNom() + " !");
        } else {
            System.err.println("Erreur : aucun utilisateur connecté !");
        }
    }

    /**
     * Gestion du bouton "Créer un cours".
     */
    @FXML
    private void handleCreerCours(ActionEvent event) {
        System.out.println("Bouton Créer un cours cliqué !");
        // Exemple d'ouverture d'une nouvelle fenêtre pour créer un cours :
        // NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/creer-cours.fxml", "Créer un cours", (Stage) creerCoursButton.getScene().getWindow(), utilisateurConnecte);
    }

    /**
     * Gestion du bouton "Modifier l'emploi du temps".
     */
    @FXML
    private void handleModifierEmploiDuTemps(ActionEvent event) {
        System.out.println("Bouton Modifier l'emploi du temps cliqué !");
    }

    /**
     * Gestion du bouton "Gérer les créneaux".
     */
    @FXML
    private void handleGererCreneaux(ActionEvent event) {
        System.out.println("Bouton Gérer les créneaux cliqué !");
    }

    /**
     * Gestion du bouton "Affecter des enseignants".
     */
    @FXML
    private void handleAffecterEnseignants(ActionEvent event) {
        System.out.println("Bouton Affecter des enseignants cliqué !");
    }

    /**
     * Gestion du bouton "Gérer les salles".
     */
    @FXML
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Bouton Gérer les salles cliqué !");
    }

    /**
     * Gestion du bouton "Contrôler les conflits".
     */
    @FXML
    private void handleControlerConflits(ActionEvent event) {
        System.out.println("Bouton Contrôler les conflits cliqué !");
    }

    /**
     * Gestion du bouton "Statistiques des salles".
     */
    @FXML
    private void handleStatistiquesSalles(ActionEvent event) {
        System.out.println("Bouton Statistiques des salles cliqué !");
    }

    /**
     * Gestion du bouton "Statistiques enseignants".
     */
    @FXML
    private void handleStatistiquesEnseignants(ActionEvent event) {
        System.out.println("Bouton Statistiques enseignants cliqué !");
    }

    /**
     * Gestion du bouton "Gérer les utilisateurs".
     * Cette action redirige vers la fenêtre permettant de gérer les utilisateurs en
     * transmettant les données de l'utilisateur connecté.
     */
    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        System.out.println("Bouton Gérer les utilisateurs cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/creer-utilisateur.fxml", // Chemin de la nouvelle page
                "Créer un utilisateur",                // Titre de la fenêtre
                (Stage) gererUtilisateursButton.getScene().getWindow(), // Stage actuel
                utilisateurConnecte                    // Transmission de l'utilisateur connecté
        );
    }
}
