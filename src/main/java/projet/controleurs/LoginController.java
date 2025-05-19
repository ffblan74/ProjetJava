package projet.controleurs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        Stage currentStage = (Stage) emailField.getScene().getWindow();

        boolean loggedIn = false;

        try (InputStream inputStream = getClass().getResourceAsStream("/projet/csv/utilisateurs.csv");
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            boolean firstLine = true; // Pour ignorer l'en-tête
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length == 6) {
                    String storedEmail = parts[3];
                    String storedPassword = parts[4];
                    String role = parts[5];

                    if (email.equals(storedEmail) && password.equals(storedPassword)) {
                        loggedIn = true;
                        if (role.equals("ETUDIANT")) {
                            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/accueil-eleve.fxml", "Accueil Élève", currentStage);
                        } else if (role.equals("ENSEIGNANT")) {
                            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/accueil-professeur.fxml", "Accueil Professeur", currentStage);
                        } else if (role.equals("ADMINISTRATEUR")) {
                            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/accueil-admin.fxml", "Accueil Admin", currentStage);
                        }
                        break; // Utilisateur trouvé et connecté
                    }
                }
            }

        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la lecture du fichier utilisateurs.csv.");
            e.printStackTrace();
            return;
        } catch (NullPointerException e) {
            errorLabel.setText("Fichier utilisateurs.csv non trouvé.");
            e.printStackTrace();
            return;
        }

        if (!loggedIn) {
            errorLabel.setText("Invalid credentials.");
        }
    }
}