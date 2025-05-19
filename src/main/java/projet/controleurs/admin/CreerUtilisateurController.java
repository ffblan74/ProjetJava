package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.controleurs.NavigationUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class CreerUtilisateurController {

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

    @FXML
    public void initialize() {
        roleComboBox.setItems(roles);
    }

    @FXML
    private void handleCreerUtilisateur(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Validation des champs
        if (!champsValides(email, password)) {
            return;
        }

        System.out.println("Tentative de création de l'utilisateur : " + prenom + " " + nom +
                " (" + email + ") avec le rôle : " + role);

        String cheminFichier = "src/main/resources/projet/csv/utilisateurs.csv";

        try {
            // Lire les utilisateurs existants
            List<String[]> utilisateurs = CRUDcsvController.lire(cheminFichier);

            // Déterminer le prochain ID
            int prochainId = 1;
            if (!utilisateurs.isEmpty()) {
                try {
                    // Récupérer le dernier ID de la dernière ligne
                    String[] derniereLigne = utilisateurs.get(utilisateurs.size() - 1);
                    String dernierIdStr = derniereLigne[0]; // Id est dans la première colonne
                    if (dernierIdStr.matches("\\d+")) {
                        prochainId = Integer.parseInt(dernierIdStr) + 1;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la détermination du prochain ID : " + e.getMessage());
                }
            }

            // Créer la nouvelle ligne pour l'utilisateur
            String[] nouvelleLigne = {
                    String.valueOf(prochainId),
                    nom,
                    prenom,
                    email,
                    password,
                    role
            };

            // Ajouter la nouvelle ligne au fichier CSV
            CRUDcsvController.ajouter(cheminFichier, nouvelleLigne);

            System.out.println("Utilisateur ajouté au fichier CSV avec l'ID : " + prochainId);

            // Retour à l'accueil admin
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/accueil-admin.fxml",
                    "Accueil Admin",
                    (Stage) creerUtilisateurButton.getScene().getWindow()
            );

        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier CSV : " + e.getMessage());
        }
    }

    private boolean champsValides(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("Veuillez entrer une adresse email valide.");
            return false;
        }

        // Validation longueur du mot de passe
        if (password.length() < 6) {
            afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        return true;
    }

    // Méthode pour afficher un message d'erreur à l'utilisateur
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnulerCreation(ActionEvent event) {
        System.err.println("Annuler la création de l'utilisateur.");
        try {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/accueil-admin.fxml", "Accueil Admin", (Stage) annulerCreationButton.getScene().getWindow());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affiche de la page d'accueil: " + e.getMessage());
        }

    }
}