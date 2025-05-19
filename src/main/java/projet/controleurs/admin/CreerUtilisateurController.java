package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import projet.controleurs.NavigationUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

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
            return; // Arrête l'exécution si les validations échouent
        }

        System.out.println("Tentative de création de l'utilisateur : " + prenom + " " + nom +
                " (" + email + ") avec le rôle : " + role);

        String cheminFichier = "src/main/resources/projet/csv/utilisateurs.csv";
        int prochainId = 1;

        try {
            // Lire le dernier ID depuis le fichier (si le fichier existe)
            if (Files.exists(Paths.get(cheminFichier))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
                    String ligne;
                    String dernierIdStr = null;
                    while ((ligne = reader.readLine()) != null) {
                        String[] parts = ligne.split(";");
                        if (parts.length > 0 && parts[0].matches("\\d+")) {
                            dernierIdStr = parts[0];
                        }
                    }
                    if (dernierIdStr != null) {
                        prochainId = Integer.parseInt(dernierIdStr) + 1;
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture de l'ID : " + e.getMessage());
                    // Continuer avec l'ID par défaut
                }
            }

            String ligneCSV = String.join(";", Arrays.asList(
                    String.valueOf(prochainId),
                    nom,
                    prenom,
                    email,
                    password,
                    role
            ));

            if (!Files.exists(Paths.get(cheminFichier))) {
                Files.write(Paths.get(cheminFichier), "idUtilisateur;nom;prenom;email;motDePasse;role\n".getBytes(), StandardOpenOption.CREATE);
            }

            Files.write(Paths.get(cheminFichier), ("\n" + ligneCSV).getBytes(), StandardOpenOption.APPEND);

            System.out.println("Utilisateur ajouté au fichier CSV avec l'ID : " + prochainId);

            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/accueil-admin.fxml", "Accueil Admin", (Stage) creerUtilisateurButton.getScene().getWindow());

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