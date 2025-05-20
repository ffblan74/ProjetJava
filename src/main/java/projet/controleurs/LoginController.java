package projet.controleurs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField identifiantTextField;

    @FXML
    private PasswordField motDePassePasswordField;

    @FXML
    private Label messageErreurLabel;

    @FXML
    public void handleConnexion(ActionEvent event) {
        String identifiant = identifiantTextField.getText();
        String motDePasse = motDePassePasswordField.getText();

        if (identifiant.equals("admin@mail.fr") && motDePasse.equals("admin")) {
            try {
                Parent adminAccueilRoot = FXMLLoader.load(getClass().getResource("/fxml/admin_accueil.fxml"));
                Scene adminAccueilScene = new Scene(adminAccueilRoot);
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(adminAccueilScene);
                currentStage.setTitle("Accueil Administrateur");
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                messageErreurLabel.setText("Erreur lors du chargement de l'accueil admin.");
            }
        } else {
            messageErreurLabel.setText("Identifiant ou mot de passe incorrect.");
        }
    }
}