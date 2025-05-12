package projet;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Créer un Label simple
        Label label = new Label("Hello, JavaFX!");

        // Créer un conteneur (layout) et y ajouter le label
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Créer une scène avec le conteneur et définir la taille
        Scene scene = new Scene(root, 300, 200);

        // Définir la scène dans la fenêtre
        primaryStage.setTitle("Test JavaFX");
        primaryStage.setScene(scene);

        // Afficher la fenêtre
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);  // Lancer l'application JavaFX
    }
}
