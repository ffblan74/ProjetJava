package projet.controleurs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;


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

        if (email.endsWith("@eleve.isep.fr") && password.equals("eleve")) {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/accueil-eleve.fxml", "Accueil Élève", currentStage);
        } else if (email.endsWith("@professeur.isep.fr") && password.equals("prof")) {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/accueil-professeur.fxml", "Accueil Professeur", currentStage);
        } else if (email.endsWith("@admin.isep.fr") && password.equals("admin")) {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/accueil-admin.fxml", "Accueil Admin", currentStage);
        } else {
            errorLabel.setText("Invalid credentials.");
        }

    }


}
