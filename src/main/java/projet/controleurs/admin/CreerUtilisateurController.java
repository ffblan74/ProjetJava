package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.List;

public class CreerUtilisateurController implements Transmissible {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button creerUtilisateurButton;

    @FXML
    private Button annulerCreationButton;

    private ObservableList<String> roles = FXCollections.observableArrayList("ETUDIANT", "ENSEIGNANT", "ADMINISTRATEUR");

    // Utilisateur actuellement connecté
    private Utilisateur utilisateurConnecte;

    @FXML
    public void initialize() {
        // Initialisation des rôles disponibles dans le ComboBox
        roleComboBox.setItems(roles);
    }

    @Override
    public void transmettreDonnees(Object data) {
        // Si les données transmises sont un utilisateur, les enregistrer
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
            initialiserPage();
        }
    }

    /**
     * Initialise la page en fonction de l'utilisateur connecté.
     * Par exemple, afficher un message de bienvenue ou appliquer des restrictions.
     */
    private void initialiserPage() {
        if (utilisateurConnecte != null) {
            System.out.println("Utilisateur connecté : " + utilisateurConnecte.getNom() + " " + utilisateurConnecte.getPrenom());

            // Si l'utilisateur connecté n'est pas ADMINISTRATEUR, interdire l'accès
            if (!"ADMINISTRATEUR".equals(utilisateurConnecte.getRole())) {
                NavigationUtil.afficherErreur("Vous n'avez pas les droits pour accéder à cette page.");
                retournerAccueilAdmin();
            }
        } else {
            // Si aucun utilisateur connecté, rediriger par défaut
            System.out.println("Aucun utilisateur connecté. Redirection.");
            retournerAccueilAdmin();
        }
    }

    @FXML
    private void handleCreerUtilisateur(ActionEvent event) {
        // Récupération des données des champs
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Validation des champs
        if (!champsValides(nom, prenom, email, password, role)) {
            return;
        }

        System.out.println("Création d'utilisateur : " + nom + " " + prenom + " (" + email + ") avec le rôle " + role);

        String cheminFichier = "src/main/resources/projet/csv/utilisateurs.csv";

        try {
            // Lire les utilisateurs existants
            List<String[]> utilisateurs = CRUDcsvController.lire(cheminFichier);
            int prochainId = utilisateurs.isEmpty() ? 1 : calculerProchainId(utilisateurs);

            // Ajouter l'utilisateur
            CRUDcsvController.ajouter(cheminFichier, genererNouvelleLigneUtilisateur(prochainId, nom, prenom, email, password, role));
            System.out.println("Utilisateur ajouté avec succès.");

            // Retour vers l'accueil admin
            retournerAccueilAdmin();

        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            NavigationUtil.afficherErreur("Erreur lors de la création de l'utilisateur.");
        }
    }

    private boolean champsValides(String nom, String prenom, String email, String password, String role) {
        // Validation des champs
        if (nom == null || nom.isEmpty() ||
                prenom == null || prenom.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                role == null) {
            NavigationUtil.afficherErreur("Veuillez remplir tous les champs.");
            return false;
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            NavigationUtil.afficherErreur("Adresse email invalide.");
            return false;
        }
        if (password.length() < 6) {
            NavigationUtil.afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        return true;
    }

    private void retournerAccueilAdmin() {
        // Logique ici pour naviguer vers AccueilAdmin
        System.out.println("Bouton Annuler cliqué : retour à l'accueil admin");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/accueil-admin.fxml",
                "Accueil Admin",
                (Stage) creerUtilisateurButton.getScene().getWindow(),
                utilisateurConnecte
        );
    }

    private int calculerProchainId(List<String[]> utilisateurs) {
        try {
            int dernierId = Integer.parseInt(utilisateurs.get(utilisateurs.size() - 1)[0]);
            return dernierId + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String[] genererNouvelleLigneUtilisateur(int id, String nom, String prenom, String email, String password, String role) {
        return new String[]{String.valueOf(id), nom, prenom, email, password, role, "", "", "", ""};
    }

}
