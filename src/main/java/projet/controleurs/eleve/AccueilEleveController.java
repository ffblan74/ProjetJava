package projet.controleurs.eleve;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;

import projet.controleurs.CRUDcsvController;
import projet.models.Etudiant;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;

public class AccueilEleveController {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private ComboBox<String> filtreMatiere;

    private LocalDate dateActuelle;

    private final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

    private final String[] HEURES_30MIN = {
            "08:00", "08:30", "09:00", "09:30",
            "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30",
            "13:00", "13:30", "14:00", "14:30",
            "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30"
    };

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private Etudiant utilisateurConnecte;
    private List<Cours> listeCours;

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();

        if (Utilisateur.getUtilisateurConnecte() != null) {
            this.utilisateurConnecte = (Etudiant) Etudiant.getUtilisateurConnecte();
        }
        setEleve(utilisateurConnecte);

        initialiserGrille();
        afficherSemaine();
    }

    public void setEleve(Utilisateur eleve) {
        this.utilisateurConnecte = (Etudiant) eleve;
        labelBienvenue.setText("Bienvenue, " + eleve.getPrenom() + " " + eleve.getNom());
        chargerCours();
    }

    private void chargerCours() {
        try {
            listeCours = new ArrayList<>();
            List<String[]> lignes = CRUDcsvController.lire(CHEMIN_COURS);
            if (!lignes.isEmpty() && lignes.get(0)[0].equalsIgnoreCase("idCours")) {
                lignes.remove(0);
            }

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    // On ne garde que les cours de la classe de l'élève
                    if (cours.getClasse().equals(utilisateurConnecte.getGroupe())) {
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing cours: " + e.getMessage());
                }
            }

            initialiserFiltres();
            actualiserCours();
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours: " + e.getMessage());
        }
    }

    private void initialiserFiltres() {
        filtreMatiere.setOnAction(e -> actualiserCours());

        List<String> matieres = new ArrayList<>();

        for (Cours cours : listeCours) {
            if (!matieres.contains(cours.getMatiere())) {
                matieres.add(cours.getMatiere());
            }
        }

        filtreMatiere.getItems().setAll(matieres);
    }

    private void initialiserGrille() {
        grilleEmploi.getChildren().clear();
        grilleEmploi.getColumnConstraints().clear();
        grilleEmploi.getRowConstraints().clear();

        // Colonnes: 0 = heures, 1-5 = jours
        for (int i = 0; i < 6; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            if (i == 0) {
                cc.setMinWidth(80);
                cc.setPrefWidth(80);
            } else {
                cc.setMinWidth(150);
                cc.setPrefWidth(150);
            }
            cc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            grilleEmploi.getColumnConstraints().add(cc);
        }

        // Ligne 0 : en-têtes jours
        grilleEmploi.add(new Label("Horaires"), 0, 0);
        for (int j = 0; j < JOURS.length; j++) {
            Label jourLabel = new Label(JOURS[j]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            jourLabel.setTextAlignment(TextAlignment.CENTER);
            grilleEmploi.add(jourLabel, j + 1, 0);
        }

        // Ajouter lignes horaires + cellules vides
        for (int i = 0; i < HEURES_30MIN.length; i++) {
            // Ligne horaires
            Label heureLabel = new Label(HEURES_30MIN[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            grilleEmploi.add(heureLabel, 0, i + 1);

            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(30);
            rc.setPrefHeight(30);
            grilleEmploi.getRowConstraints().add(rc);

            // Cellules vides
            for (int j = 0; j < JOURS.length; j++) {
                VBox cellule = creerCelluleVide();
                grilleEmploi.add(cellule, j + 1, i + 1);
            }
        }
    }

    private void actualiserCours() {
        initialiserGrille();

        if (listeCours != null) {
            for (Cours cours : listeCours) {
                try {
                    LocalDate dateCours = cours.getDate();

                    boolean matchMatiere = filtreMatiere.getValue() == null || filtreMatiere.getValue().equals(cours.getMatiere());

                    if (estDansLaSemaineActuelle(dateCours) && matchMatiere) {
                        ajouterCoursALaGrille(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur ajout cours grille: " + e.getMessage());
                }
            }
        }

        afficherStats();
    }

    private void ajouterCoursALaGrille(Cours cours) {
        LocalDate dateCours = cours.getDate();
        int jourIndex = dateCours.getDayOfWeek().getValue() - 1; // Lundi=1

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut());
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin());

        if (jourIndex < 0 || jourIndex >= JOURS.length || debutIndex < 0 || finIndex < 0) {
            return; // Hors plage horaires ou jour non affiché
        }

        int span = finIndex - debutIndex;
        if (span <= 0) span = 1;

        VBox cellule = new VBox(5);
        cellule.setStyle("-fx-background-color: #2196F3; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150);
        cellule.setPrefHeight(span * 30 - 10);
        cellule.setMaxWidth(Double.MAX_VALUE);

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label classe = new Label(cours.getClasse());
        classe.setStyle("-fx-text-fill: white;");

        Label salle = new Label("Salle: " + cours.getSalle());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 11;");

        cellule.getChildren().addAll(matiere, classe, salle);

        grilleEmploi.getChildren().removeIf(node ->
                javafx.scene.layout.GridPane.getRowIndex(node) != null &&
                        javafx.scene.layout.GridPane.getColumnIndex(node) != null &&
                        javafx.scene.layout.GridPane.getRowIndex(node) == debutIndex + 1 &&
                        javafx.scene.layout.GridPane.getColumnIndex(node) == jourIndex + 1
        );

        grilleEmploi.add(cellule, jourIndex + 1, debutIndex + 1);
        javafx.scene.layout.GridPane.setRowSpan(cellule, span);
    }

    private int trouverIndexHeure30Min(String heure) {
        if (heure == null) return -1;
        for (int i = 0; i < HEURES_30MIN.length; i++) {
            if (HEURES_30MIN[i].equals(heure)) {
                return i;
            }
        }
        return -1;
    }

    private boolean estDansLaSemaineActuelle(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int semaineActuelle = dateActuelle.get(weekFields.weekOfWeekBasedYear());
        int semaineDate = date.get(weekFields.weekOfWeekBasedYear());
        return semaineDate == semaineActuelle;
    }

    private VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa;");
        cellule.setPrefHeight(30);
        return cellule;
    }

    private void afficherSemaine() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int numeroSemaine = dateActuelle.get(weekFields.weekOfWeekBasedYear());
        labelSemaine.setText("Semaine " + numeroSemaine);
    }

    private void afficherStats() {
        int nbCours = listeCours == null ? 0 : listeCours.size();
        labelStats.setText("Nombre de cours cette semaine : " + nbCours);
    }

    @FXML
    private void semainePrecedente() {
        dateActuelle = dateActuelle.minusWeeks(1);
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // Utilise changerScene pour revenir à la connexion sur la même fenêtre
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }
}
