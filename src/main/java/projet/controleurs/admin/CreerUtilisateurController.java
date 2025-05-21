package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Role; // Import de l'enum Role
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreerUtilisateurController implements Transmissible {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button creerUtilisateurButton;
    @FXML private Button annulerCreationButton;

    // Champs FXML pour le groupe
    @FXML private Label groupeLabel;
    @FXML private TextField groupeField;

    // ObservableList pour la ComboBox
    private ObservableList<String> roles = FXCollections.observableArrayList("ETUDIANT", "ENSEIGNANT", "ADMINISTRATEUR");

    private Utilisateur utilisateurConnecte;
    private Utilisateur utilisateurAModifier;
    private String[] ligneCSVOriginaleAModifier; // La ligne CSV complète de l'utilisateur à modifier

    // IMPORTANT : L'en-tête doit correspondre EXACTEMENT aux 9 champs de ta méthode toCSVArray
    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String CSV_EN_TETE = "idUtilisateur;nom;prenom;email;motDePasse;role;groupe;emploiDuTempsId;matiereEnseignee";


    @FXML
    public void initialize() {
        roleComboBox.setItems(roles);

        // Initialiser les champs groupe cachés par défaut
        groupeLabel.setVisible(false);
        groupeLabel.setManaged(false); // Important pour ne pas prendre d'espace dans le layout
        groupeField.setVisible(false);
        groupeField.setManaged(false); // Important pour ne pas prendre d'espace dans le layout

        // Ajouter un listener à la ComboBox des rôles pour gérer la visibilité du champ groupe
        roleComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean isStudent = "ETUDIANT".equals(newValue);
            groupeLabel.setVisible(isStudent);
            groupeLabel.setManaged(isStudent);
            groupeField.setVisible(isStudent);
            groupeField.setManaged(isStudent);

            // Réinitialiser le champ groupe si le rôle change pour éviter des valeurs résiduelles
            if (!isStudent) {
                groupeField.setText("");
            }
        });
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
        } else if (data instanceof Object[] && ((Object[]) data).length == 2) {
            Object[] transmittedData = (Object[]) data;
            if (transmittedData[0] instanceof Utilisateur && transmittedData[1] instanceof String[]) {
                this.utilisateurAModifier = (Utilisateur) transmittedData[0];
                this.ligneCSVOriginaleAModifier = (String[]) transmittedData[1];
                preRemplirChampsPourModification();
            } else {
                System.err.println("Type de données incorrectes dans le tableau transmis pour modification.");
                NavigationUtil.afficherErreur("Données de modification invalides.");
                retournerAccueilAdmin();
            }
        } else {
            System.err.println("Données inattendues transmises au contrôleur CreerUtilisateurController.");
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            if (this.utilisateurConnecte == null) {
                NavigationUtil.afficherErreur("Aucun utilisateur administrateur connecté. Redirection.");
                retournerAccueilAdmin();
            }
        }
    }

    private void preRemplirChampsPourModification() {
        if (utilisateurAModifier != null) {
            nomField.setText(utilisateurAModifier.getNom());
            prenomField.setText(utilisateurAModifier.getPrenom());
            emailField.setText(utilisateurAModifier.getEmail());
            passwordField.setText(""); // Ne pré-remplis PAS le mot de passe pour des raisons de sécurité

            // Assure que le rôle de l'utilisateur à modifier est bien un String avant de le passer à setValue
            roleComboBox.setValue(utilisateurAModifier.getRole().name()); // Convertir l'enum Role en String

            // Gérer la visibilité et le contenu du champ groupe pour la modification
            boolean isStudent = utilisateurAModifier.getRole().name().equals("ETUDIANT");
            groupeLabel.setVisible(isStudent);
            groupeLabel.setManaged(isStudent);
            groupeField.setVisible(isStudent);
            groupeField.setManaged(isStudent);

            // Si c'est un étudiant, pré-remplir le champ groupe depuis la ligne CSV originale
            // (car 'groupe' n'est pas une propriété de l'objet Utilisateur dans ton modèle actuel)
            if (isStudent && ligneCSVOriginaleAModifier.length > 6) { // L'index 6 correspond à 'groupe'
                String groupeOriginal = ligneCSVOriginaleAModifier[6].trim();
                if (!groupeOriginal.equalsIgnoreCase("None") && !groupeOriginal.isEmpty()) {
                    groupeField.setText(groupeOriginal);
                } else {
                    groupeField.setText("");
                }
            } else {
                groupeField.setText(""); // Vider si ce n'est pas un étudiant ou pas de groupe initial
            }

            creerUtilisateurButton.setText("Modifier l'utilisateur");
        }
    }

    @FXML
    private void handleCreerUtilisateur(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String roleStr = roleComboBox.getValue();

        if (!champsValides(nom, prenom, email, password, roleStr)) {
            return;
        }

        // Conversion du String du ComboBox vers l'enum Role pour le constructeur Utilisateur
        Role roleEnum = Role.valueOf(roleStr.toUpperCase());

        // Récupération des valeurs pour les champs spécifiques (groupe, emploiDuTempsId, matiereEnseignee)
        // Ces valeurs seront passées à la méthode toCSVArray, car ce ne sont pas des champs de l'objet Utilisateur
        String groupe = ""; // Sera rempli pour les étudiants
        String emploiDuTempsId = ""; // Pas de champ FXML pour l'instant, donc vide par défaut
        String matiereEnseignee = ""; // Pas de champ FXML pour l'instant, donc vide par défaut

        if ("ETUDIANT".equals(roleStr)) {
            groupe = groupeField.getText().trim();
            if (groupe.isEmpty()) {
                NavigationUtil.afficherErreur("Veuillez saisir la lettre de la classe/groupe pour l'étudiant.");
                return;
            }
            if (!groupe.matches("^[A-Z]$")) { // Validation: une seule lettre majuscule (A-Z)
                NavigationUtil.afficherErreur("Le groupe doit être une seule lettre majuscule (Ex: A, B, C).");
                return;
            }
        }
        // Pour les enseignants, matiereEnseignee reste vide/None pour l'instant (pas de champ FXML)
        // Pour les administrateurs, emploiDuTempsId et matiereEnseignee restent vides/None

        try {
            List<String[]> toutesLesLignes = CRUDcsvController.lire(CHEMIN_FICHIER_UTILISATEURS);
            List<String[]> lignesSansEnTete = (toutesLesLignes.isEmpty() || toutesLesLignes.get(0)[0].equals("idUtilisateur"))
                    ? new ArrayList<>(toutesLesLignes.subList(1, toutesLesLignes.size()))
                    : new ArrayList<>(toutesLesLignes);

            if (utilisateurAModifier == null) { // Mode Création
                int prochainId = calculerProchainId(lignesSansEnTete);

                // Instancier le nouvel Utilisateur avec le rôle ENUM
                Utilisateur nouvelUtilisateur = new Utilisateur(prochainId, nom, prenom, email, password, roleEnum);

                // Ajouter l'en-tête si le fichier est vide ou n'a que l'en-tête
                if (toutesLesLignes.isEmpty() || (toutesLesLignes.size() == 1 && toutesLesLignes.get(0)[0].equals("idUtilisateur"))) {
                    CRUDcsvController.ajouter(CHEMIN_FICHIER_UTILISATEURS, CSV_EN_TETE.split(";"));
                }
                // Passer les arguments spécifiques à toCSVArray()
                CRUDcsvController.ajouter(CHEMIN_FICHIER_UTILISATEURS, nouvelUtilisateur.toCSVArray(groupe, emploiDuTempsId, matiereEnseignee));
                NavigationUtil.afficherInformation("Succès", "Utilisateur créé avec succès !");

            } else { // Mode Modification
                int idAModifier = utilisateurAModifier.getIdUtilisateur();
                String motDePasseFinal = password.isEmpty() ? utilisateurAModifier.getMotDePasse() : password;

                // Récupérer les valeurs existantes pour emploiDuTempsId et matiereEnseignee
                // Celles-ci doivent venir de la ligne CSV originale car elles ne sont pas dans l'objet Utilisateur
                String emploiDuTempsIdModifie = ligneCSVOriginaleAModifier.length > 7 ? ligneCSVOriginaleAModifier[7] : "";
                String matiereEnseigneeModifie = ligneCSVOriginaleAModifier.length > 8 ? ligneCSVOriginaleAModifier[8] : "";

                // Le 'groupe' vient soit du champ, soit il est vidé si le rôle n'est plus étudiant
                String groupeModifie = groupe; // Initialiser avec la valeur récupérée ci-dessus pour ETUDIANT

                if (!"ETUDIANT".equals(roleStr)) {
                    groupeModifie = ""; // Vider le groupe si ce n'est plus un étudiant
                }

                // Instancier l'Utilisateur mis à jour avec le rôle ENUM
                Utilisateur utilisateurMaj = new Utilisateur(idAModifier, nom, prenom, email, motDePasseFinal, roleEnum);

                // Appeler mettreAJour avec les arguments spécifiques pour toCSVArray()
                CRUDcsvController.mettreAJour(CHEMIN_FICHIER_UTILISATEURS, 0, String.valueOf(idAModifier), utilisateurMaj.toCSVArray(groupeModifie, emploiDuTempsIdModifie, matiereEnseigneeModifie));
                NavigationUtil.afficherInformation("Succès", "Utilisateur mis à jour avec succès !");
            }
            retournerAccueilAdmin();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'opération utilisateur : " + e.getMessage());
            NavigationUtil.afficherErreur("Erreur lors de la création/modification de l'utilisateur.");
        }
    }

    private boolean champsValides(String nom, String prenom, String email, String password, String role) {
        if (nom == null || nom.isEmpty() ||
                prenom == null || prenom.isEmpty() ||
                email == null || email.isEmpty() ||
                role == null) {
            NavigationUtil.afficherErreur("Veuillez remplir les champs obligatoires (Nom, Prénom, Email, Rôle).");
            return false;
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            NavigationUtil.afficherErreur("Adresse email invalide.");
            return false;
        }
        if (utilisateurAModifier == null && (password == null || password.isEmpty())) {
            NavigationUtil.afficherErreur("Le mot de passe ne peut pas être vide lors de la création.");
            return false;
        }
        if (!password.isEmpty() && password.length() < 6) {
            NavigationUtil.afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        return true;
    }

    @FXML
    private void retournerAccueilAdmin() {
        try {
            Stage stageActuel = (Stage) creerUtilisateurButton.getScene().getWindow();
            stageActuel.close();

            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/accueil-admin-gerer-utilisateur.fxml",
                    "Accueil Admin",
                    null,
                    Utilisateur.getUtilisateurConnecte()
            );

        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtil.afficherErreur("Impossible de retourner à l'accueil admin.");
        }
    }

    private int calculerProchainId(List<String[]> utilisateursSansEnTete) {
        int dernierId = 0;
        for (String[] ligne : utilisateursSansEnTete) {
            try {
                int currentId = Integer.parseInt(ligne[0].trim());
                if (currentId > dernierId) {
                    dernierId = currentId;
                }
            } catch (NumberFormatException e) {
                System.err.println("ID utilisateur non numérique trouvé lors du calcul du prochain ID : " + ligne[0]);
            }
        }
        return dernierId + 1;
    }
}