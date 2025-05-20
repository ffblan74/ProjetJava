package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Role;
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
    @FXML private Button creerUtilisateurButton; // Ce bouton deviendra "Créer" ou "Modifier"
    @FXML private Button annulerCreationButton;

    // TODO: Si tu as ajouté des champs FXML pour 'groupe', 'emploiDuTempsId', 'matiereEnseignee', déclare-les ici :
    // @FXML private TextField groupeField;
    // @FXML private TextField emploiDuTempsIdField;
    // @FXML private TextField matiereEnseigneeField;


    private ObservableList<String> roles = FXCollections.observableArrayList("ETUDIANT", "ENSEIGNANT", "ADMINISTRATEUR");

    private Utilisateur utilisateurConnecte; // L'utilisateur admin connecté (pour revenir à l'accueil admin)
    private Utilisateur utilisateurAModifier; // L'utilisateur que l'on est en train de modifier
    private String[] ligneCSVOriginaleAModifier; // La ligne CSV complète de l'utilisateur à modifier (9 champs)

    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String CSV_EN_TETE = "idUtilisateur;nom;prenom;email;motDePasse;role;groupe;emploiDuTempsId;matiereEnseignee";


    @FXML
    public void initialize() {
        roleComboBox.setItems(roles);
        // Si tu as des champs supplémentaires, tu peux les initialiser ici aussi
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            // C'est l'utilisateur admin qui se connecte au début
            this.utilisateurConnecte = (Utilisateur) data;
            initialiserPage(); // Cela va juste faire un print pour l'instant
        } else if (data instanceof Object[] && ((Object[]) data).length == 2) {
            // C'est un tableau d'objets pour la modification : {Utilisateur, String[]}
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
            // Récupérer l'utilisateur connecté via la méthode statique si aucune donnée n'a été transmise
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
            roleComboBox.setValue(utilisateurAModifier.getRole().name());

            creerUtilisateurButton.setText("Modifier l'utilisateur");

            // TODO: Si tu as des champs pour groupe, emploiDuTempsId, matiereEnseignee dans ton FXML, pré-remplis-les ici
            // Assure-toi que ligneCSVOriginaleAModifier a assez d'éléments avant d'y accéder.
            // if (ligneCSVOriginaleAModifier.length > 6) groupeField.setText(ligneCSVOriginaleAModifier[6]);
            // if (ligneCSVOriginaleAModifier.length > 7) emploiDuTempsIdField.setText(ligneCSVOriginaleAModifier[7]);
            // if (ligneCSVOriginaleAModifier.length > 8) matiereEnseigneeField.setText(ligneCSVOriginaleAModifier[8]);
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

        Role roleEnum = Role.valueOf(roleStr.toUpperCase()); // Assure-toi que le rôle est en majuscules

        try {
            // Lecture de toutes les lignes pour pouvoir réécrire ou trouver le prochain ID
            List<String[]> toutesLesLignes = CRUDcsvController.lire(CHEMIN_FICHIER_UTILISATEURS);
            List<String[]> lignesSansEnTete = (toutesLesLignes.isEmpty() || toutesLesLignes.get(0)[0].equals("idUtilisateur"))
                    ? new ArrayList<>(toutesLesLignes.subList(1, toutesLesLignes.size())) // Exclure l'en-tête
                    : new ArrayList<>(toutesLesLignes); // Pas d'en-tête, toutes les lignes sont des données


            if (utilisateurAModifier == null) { // Mode Création
                int prochainId = calculerProchainId(lignesSansEnTete);

                // Récupère les valeurs pour les 3 champs spécifiques (vides à la création si pas de champs FXML)
                String groupe = ""; // TODO: remplacer par groupeField.getText() si tu as le champ
                String emploiDuTempsId = ""; // TODO: remplacer par emploiDuTempsIdField.getText() si tu as le champ
                String matiereEnseignee = ""; // TODO: remplacer par matiereEnseigneeField.getText() si tu as le champ

                Utilisateur nouvelUtilisateur = new Utilisateur(prochainId, nom, prenom, email, password, roleEnum);

                // Générer la ligne CSV complète en utilisant la méthode toCSVArray
                String[] nouvelleLigneCSV = nouvelUtilisateur.toCSVArray(groupe, emploiDuTempsId, matiereEnseignee);

                // Ajouter l'en-tête si le fichier est vide
                if (toutesLesLignes.isEmpty() || (toutesLesLignes.size() == 1 && toutesLesLignes.get(0)[0].equals("idUtilisateur"))) {
                    CRUDcsvController.ajouter(CHEMIN_FICHIER_UTILISATEURS, CSV_EN_TETE.split(";"));
                }
                CRUDcsvController.ajouter(CHEMIN_FICHIER_UTILISATEURS, nouvelleLigneCSV);
                NavigationUtil.afficherInformation("Succès", "Utilisateur créé avec succès !");

            } else { // Mode Modification
                int idAModifier = utilisateurAModifier.getIdUtilisateur();
                // Si le champ mot de passe est vide, on garde l'ancien mot de passe
                String motDePasseFinal = password.isEmpty() ? utilisateurAModifier.getMotDePasse() : password;

                // Récupère les 3 derniers champs de la ligne CSV originale pour les conserver
                // Sauf si tu as des champs pour eux dans le FXML, dans ce cas utilise les valeurs des champs.
                String groupeModifie = ligneCSVOriginaleAModifier.length > 6 ? ligneCSVOriginaleAModifier[6] : "";
                String emploiDuTempsIdModifie = ligneCSVOriginaleAModifier.length > 7 ? ligneCSVOriginaleAModifier[7] : "";
                String matiereEnseigneeModifie = ligneCSVOriginaleAModifier.length > 8 ? ligneCSVOriginaleAModifier[8] : "";

                // TODO: Si tu as des champs pour 'groupe', 'emploiDuTempsId', 'matiereEnseignee', utilise leurs valeurs ici:
                // String groupeModifie = groupeField.getText();
                // String emploiDuTempsIdModifie = emploiDuTempsIdField.getText();
                // String matiereEnseigneeModifie = matiereEnseigneeField.getText();


                Utilisateur utilisateurMaj = new Utilisateur(idAModifier, nom, prenom, email, motDePasseFinal, roleEnum);

                // Générer la ligne CSV mise à jour, en conservant les 3 derniers champs
                String[] ligneCSVMaj = utilisateurMaj.toCSVArray(groupeModifie, emploiDuTempsIdModifie, matiereEnseigneeModifie);

                CRUDcsvController.mettreAJour(CHEMIN_FICHIER_UTILISATEURS, 0, String.valueOf(idAModifier), ligneCSVMaj);
                NavigationUtil.afficherInformation("Succès", "Utilisateur mis à jour avec succès !");
            }
            retournerAccueilAdmin(); // Retourner à l'accueil admin après l'opération
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
        // Le mot de passe ne doit pas être vide pour la création, mais peut l'être pour la modification si on ne le change pas.
        if (utilisateurAModifier == null && (password == null || password.isEmpty())) {
            NavigationUtil.afficherErreur("Le mot de passe ne peut pas être vide lors de la création.");
            return false;
        }
        if (!password.isEmpty() && password.length() < 6) { // Vérifie la longueur si le mot de passe est saisi
            NavigationUtil.afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        return true;
    }

    @FXML
    private void retournerAccueilAdmin() {
        try {
            // Ferme la fenêtre actuelle de création/modification
            Stage stageActuel = (Stage) creerUtilisateurButton.getScene().getWindow();
            stageActuel.close();

            // Puisque la fenêtre AccueilAdmin est ouverte en arrière-plan,
            // tu peux lui envoyer un signal pour qu'elle se rafraîchisse.
            // Cela dépend de la manière dont tu gères les stages et les contrôleurs.
            // Si tu veux la recréer, alors le code ci-dessous est correct,
            // mais l'utilisateur connecté doit venir du Utilisateur.getUtilisateurConnecte()
            // pour éviter de transmettre l'utilisateur modifié par erreur.

            // Solution robuste : rouvrir et transmettre l'utilisateur ADMIN connecté (via la classe statique)
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/accueil-admin-gerer-utilisateur.fxml",
                    "Accueil Admin",
                    null, // Il n'y a plus de stage à fermer depuis cette méthode
                    Utilisateur.getUtilisateurConnecte() // IMPORTANT: Transmet l'admin connecté
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

    // Initialise la page (juste un print pour l'instant)
    private void initialiserPage() {
        if (utilisateurConnecte != null) {
            System.out.println("Utilisateur connecté à CreerUtilisateurController: " + utilisateurConnecte.getNom() + " " + utilisateurConnecte.getPrenom() + " (Rôle: " + utilisateurConnecte.getRole() + ").");
        }
    }
}