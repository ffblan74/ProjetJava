package projet.controleurs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projet.models.Administrateur;
import projet.models.Enseignant;
import projet.models.Etudiant;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    // Chemin vers le fichier CSV des utilisateurs
    private static final String CHEMIN_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";




    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        Stage currentStage = (Stage) emailField.getScene().getWindow();

        try {
            // Rechercher un utilisateur correspondant à l'email
            List<String[]> lignes = CRUDcsvController.rechercher(CHEMIN_UTILISATEURS, 3, email);

            for (String[] ligne : lignes) {
                if (ligne[4].equals(password)) { // Vérification du mot de passe
                    System.out.println("fdklsmfj"+"  "+ligne[4]+"  "+password);
                    // Convertir la ligne CSV en une instance de sous-classe appropriée
                    Utilisateur utilisateur = convertirDepuisCSV(ligne);
                    System.out.println("utilisateur : " + utilisateur.getNom() + " " + utilisateur.getPrenom());

                    // Définir l'utilisateur comme connecté
                    Utilisateur.connecter(utilisateur);

                    switch (utilisateur.getRole()) {
                        case ETUDIANT:
                            NavigationUtil.ouvrirNouvelleFenetre(
                                    "/projet/fxml/accueil-eleve.fxml",
                                    "Accueil Élève",
                                    currentStage,
                                    utilisateur // Passe l'utilisateur ici
                            );
                            break;
                        case ENSEIGNANT:
                            NavigationUtil.ouvrirNouvelleFenetre(
                                    "/projet/fxml/accueil-professeur.fxml",
                                    "Accueil Professeur",
                                    currentStage,
                                    utilisateur // Passe l'utilisateur ici
                            );
                            break;
                        case ADMINISTRATEUR:
                            NavigationUtil.ouvrirNouvelleFenetre(
                                    "/projet/fxml/accueil-admin-gerer-utilisateur.fxml",
                                    "Accueil Administrateur",
                                    currentStage,
                                    utilisateur // Passe l'utilisateur ici
                            );
                            break;
                        default:
                            errorLabel.setText("Rôle inconnu.");
                            break;
                    }
                    return;

                }
            }

            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la validation.");
            e.printStackTrace();
        }
    }


    public static Utilisateur convertirDepuisCSV(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Données CSV insuffisantes");
        }

        int idUtilisateur = Integer.parseInt(data[0]);
        String nom = data[1];
        String prenom = data[2];
        String email = data[3];
        String motDePasse = data[4];
        String role = data[5];

        switch (role) {
            case "ETUDIANT":
                // Gestion du groupe avec vérification des colonnes vides
                String groupe = (data.length > 6 && !data[6].trim().isEmpty()) ? data[6] : null;
                return new Etudiant(idUtilisateur, nom, prenom, email, motDePasse, groupe);

            case "ENSEIGNANT":
                // Gestion des matières enseignées avec vérification des colonnes vides
                List<String> matiereEnseignee = new ArrayList<>();
                if (data.length > 7 && !data[7].trim().isEmpty()) {
                    matiereEnseignee = Arrays.stream(
                                    data[7].replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "")
                                            .split(",")
                            )
                            .filter(matiere -> !matiere.trim().isEmpty())
                            .collect(Collectors.toList());
                }
                return new Enseignant(idUtilisateur, nom, prenom, email, motDePasse, matiereEnseignee);

            case "ADMINISTRATEUR":
                return new Administrateur(idUtilisateur, nom, prenom, email, motDePasse);

            default:
                throw new IllegalArgumentException("Rôle inconnu : " + role);
        }
    }


}
