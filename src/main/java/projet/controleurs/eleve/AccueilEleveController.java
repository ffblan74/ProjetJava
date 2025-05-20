package projet.controleurs.eleve;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Cours;
import projet.models.Utilisateur;
import projet.models.Role;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;
import projet.utils.TransmissibleStage;

import java.net.URL;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AccueilEleveController implements Transmissible, TransmissibleStage, Initializable {

    @FXML private Label welcomeLabel;
    @FXML private GridPane emploiDuTempsGrid;
    @FXML private Button logoutButton;
    @FXML private ScrollPane scrollPane;

    private Utilisateur utilisateurConnecte;
    private Stage stageCourante;

    private final String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private final LocalTime[] heures = {
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
            LocalTime.of(11, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
            LocalTime.of(17, 0)
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration initiale
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }

        if (emploiDuTempsGrid != null) {
            emploiDuTempsGrid.setGridLinesVisible(true); // Pour visualiser les lignes de la grille
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stageCourante = stage;
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur utilisateur) {
            this.utilisateurConnecte = utilisateur;
            initialiserContenuPage();
        } else {
            NavigationUtil.afficherErreur("Erreur Interne", "Utilisateur manquant",
                    "Impossible de charger les informations de l'utilisateur.");
            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", this.stageCourante, null);
        }
    }

    private void initialiserContenuPage() {
        if (utilisateurConnecte != null && utilisateurConnecte.getRole() == Role.ETUDIANT) {
            welcomeLabel.setText("Bienvenue, " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());
            setupGrid();
            chargerEmploiDuTempsEleve();
        } else {
            NavigationUtil.afficherErreur("Accès refusé", "Rôle non autorisé",
                    "Seuls les étudiants peuvent accéder à cette page.");
            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", this.stageCourante, null);
        }
    }

    private void setupGrid() {
        if (emploiDuTempsGrid == null) {
            System.err.println("Erreur: emploiDuTempsGrid est null!");
            return;
        }

        // Nettoyage de la grille
        emploiDuTempsGrid.getChildren().clear();
        emploiDuTempsGrid.getColumnConstraints().clear();
        emploiDuTempsGrid.getRowConstraints().clear();

        // Configuration des colonnes
        ColumnConstraints timeCol = new ColumnConstraints(80); // Colonne des heures
        emploiDuTempsGrid.getColumnConstraints().add(timeCol);

        for (String jour : jours) {
            ColumnConstraints cc = new ColumnConstraints(150, 150, 150); // Colonnes des jours
            cc.setHalignment(HPos.CENTER);
            emploiDuTempsGrid.getColumnConstraints().add(cc);
        }

        // Configuration des lignes
        RowConstraints headerRow = new RowConstraints(30); // Ligne d'en-tête
        emploiDuTempsGrid.getRowConstraints().add(headerRow);

        for (int i = 0; i < heures.length; i++) {
            RowConstraints rc = new RowConstraints(60); // Lignes des créneaux horaires
            emploiDuTempsGrid.getRowConstraints().add(rc);
        }

        // Ajout des en-têtes de jours
        for (int col = 0; col < jours.length; col++) {
            Label label = new Label(jours[col]);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5; -fx-alignment: CENTER;");
            GridPane.setHalignment(label, HPos.CENTER);
            emploiDuTempsGrid.add(label, col + 1, 0); // +1 pour sauter la colonne des heures
        }

        // Ajout des heures
        for (int row = 0; row < heures.length; row++) {
            Label label = new Label(heures[row].toString());
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5; -fx-alignment: CENTER;");
            GridPane.setHalignment(label, HPos.CENTER);
            emploiDuTempsGrid.add(label, 0, row + 1); // +1 pour sauter la ligne d'en-tête
        }
    }

    private void chargerEmploiDuTempsEleve() {
        try {
            List<String[]> emplois = CRUDcsvController.lire("src/main/resources/projet/csv/emplois_du_temps.csv");
            Optional<String[]> ligneEtudiant = emplois.stream()
                    .skip(1)
                    .filter(line -> line.length > 1 && line[1].equals(String.valueOf(utilisateurConnecte.getIdUtilisateur())))
                    .findFirst();

            if (ligneEtudiant.isEmpty()) {
                System.out.println("Aucun emploi du temps trouvé pour cet étudiant");
                return;
            }

            String[] ligne = ligneEtudiant.get();
            if (ligne.length < 3 || ligne[2].isEmpty()) {
                System.out.println("Données d'emploi du temps incomplètes");
                return;
            }

            List<Integer> idsCours = Arrays.stream(ligne[2].split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<String[]> coursCsv = CRUDcsvController.lire("src/main/resources/projet/csv/cours.csv");
            List<Cours> coursList = coursCsv.stream()
                    .skip(1)
                    .map(this::parseCours)
                    .filter(Objects::nonNull)
                    .filter(c -> idsCours.contains(c.getIdCours()))
                    .collect(Collectors.toList());

            for (Cours cours : coursList) {
                ajouterCoursDansGrille(cours);
            }
        } catch (Exception e) {
            NavigationUtil.afficherErreur("Erreur", "Chargement échoué",
                    "Impossible de charger l'emploi du temps.");
            e.printStackTrace();
        }
    }

    private void ajouterCoursDansGrille(Cours cours) {
        try {
            int col = jourToIndex(cours.getJour()) + 1; // +1 pour sauter la colonne des heures
            int row = heureToIndex(LocalTime.parse(cours.getHeureDebut())) + 1; // +1 pour sauter la ligne d'en-tête

            if (col < 1 || row < 1) {
                System.err.println("Position invalide pour le cours: " + cours);
                return;
            }

            Label label = new Label(String.format("%s\n%s\n%s",
                    cours.getMatiere(),
                    cours.getHeureDebut() + "-" + cours.getHeureFin(),
                    cours.getSalle()));

            label.setStyle("-fx-background-color: #CE93D8; -fx-padding: 5; -fx-border-color: black; "
                    + "-fx-border-radius: 3; -fx-background-radius: 3; -fx-alignment: CENTER; "
                    + "-fx-text-alignment: center; -fx-font-size: 12px;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMaxHeight(Double.MAX_VALUE);

            emploiDuTempsGrid.add(label, col, row);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du cours: " + cours);
            e.printStackTrace();
        }
    }

    private int jourToIndex(String jour) {
        for (int i = 0; i < jours.length; i++) {
            if (jours[i].equalsIgnoreCase(jour)) {
                return i;
            }
        }
        return -1;
    }

    private int heureToIndex(LocalTime heure) {
        for (int i = 0; i < heures.length; i++) {
            if (heures[i].equals(heure)) {
                return i;
            }
        }
        return -1;
    }

    private Cours parseCours(String[] line) {
        try {
            if (line.length < 12) {
                System.err.println("Ligne incomplète: " + Arrays.toString(line));
                return null;
            }
            return new Cours(
                    Integer.parseInt(line[0]), // idCours
                    line[1],  // matiere
                    line[2],  // jour
                    line[3],  // heureDebut
                    parseDuration(line[4]),  // duree
                    Integer.parseInt(line[5]),  // idProfesseur
                    Integer.parseInt(line[6]),  // idClasse
                    Integer.parseInt(line[7]),  // idSalle
                    line[8],  // description
                    line[9],  // professeurNomComplet
                    line[10], // salle
                    line[11]  // heureFin
            );
        } catch (Exception e) {
            System.err.println("Erreur de parsing du cours: " + Arrays.toString(line));
            return null;
        }
    }

    private Duration parseDuration(String value) {
        try {
            value = value.toLowerCase();
            if (value.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(value.replace("h", "").trim()));
            }
            if (value.endsWith("min")) {
                return Duration.ofMinutes(Long.parseLong(value.replace("min", "").trim()));
            }
            return Duration.ofHours(Long.parseLong(value));
        } catch (Exception e) {
            return Duration.ofHours(0);
        }
    }

    @FXML
    private void handleLogout() {
        Utilisateur.deconnecter();
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", stageCourante, null);
    }
}