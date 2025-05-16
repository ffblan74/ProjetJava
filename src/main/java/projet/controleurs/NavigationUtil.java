package projet.controleurs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtil {

    public static void ouvrirNouvelleFenetre(String cheminFXML, String titre, Stage stageActuel) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(cheminFXML));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

            if (stageActuel != null) {
                stageActuel.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
