package projet.projetjava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projet.projetjava.controleurs.LoginController;

public class TimetableApp extends Application {

    private GestionnaireEmploiDuTemps gestionnaire = new GestionnaireEmploiDuTemps();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ajouter des utilisateurs de test (à remplacer par votre logique de chargement)
        gestionnaire.ajouterUtilisateur(new Etudiant("etudiant1", "pass1", "Alice"));
        gestionnaire.ajouterUtilisateur(new Enseignant("enseignant1", "pass2", "Bob"));
        gestionnaire.ajouterUtilisateur(new Administrateur("admin1", "pass3", "Charlie"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vues/login.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setGestionnaireEmploiDuTemps(gestionnaire);

        primaryStage.setTitle("Gestion Emploi du Temps");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}