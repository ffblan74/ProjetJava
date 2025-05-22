package projet.utils;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import projet.models.Cours;
import projet.models.Salle;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

public class GrilleUtil {

    // Jours de la semaine (lundi à vendredi)
    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

    // Créneaux 30 min entre 8h et 17h30 inclus
    private static final String[] HEURES_30MIN = {
            "08:00", "08:30", "09:00", "09:30",
            "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30",
            "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30"
    };

    public static void initialiserGrille(GridPane grilleEmploi) {
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
            jourLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-alignment: center;");
            jourLabel.setTextAlignment(TextAlignment.CENTER);
            grilleEmploi.add(jourLabel, j + 1, 0);
        }

        // Ajouter lignes horaires + cellules vides
        for (int i = 0; i < HEURES_30MIN.length; i++) {
            VBox heureCell = new VBox();
            heureCell.setAlignment(javafx.geometry.Pos.TOP_LEFT); // Alignement en haut à gauche

            Label heureLabel = new Label(HEURES_30MIN[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 5 0 5;");
            VBox.setMargin(heureLabel, new javafx.geometry.Insets(-8, 0, 0, 0));  // Marges négatives pour remonter

            heureCell.getChildren().add(heureLabel);
            heureCell.setMinHeight(30); // Assurez-vous que ce n'est pas trop grand

            grilleEmploi.add(heureCell, 0, i + 1);

            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(30);
            rc.setPrefHeight(30);
            grilleEmploi.getRowConstraints().add(rc);

            for (int j = 0; j < JOURS.length; j++) {
                VBox cellule = creerCelluleVide();
                grilleEmploi.add(cellule, j + 1, i + 1);
            }
        }
    }

    public static void afficherGrille(GridPane grilleEmploi, List<Cours> coursList, LocalDate dateActuelle) {
        initialiserGrille(grilleEmploi); // Réinitialiser la grille d'abord

        if (coursList != null) {
            for (Cours cours : coursList) {
                LocalDate dateCours = cours.getDate();
                if (estDansLaSemaineActuelle(dateCours, dateActuelle)) {
                    ajouterCoursALaGrille(grilleEmploi, cours);
                }
            }
        }
    }

    private static void ajouterCoursALaGrille(GridPane grilleEmploi, Cours cours) {
        LocalDate dateCours = cours.getDate();
        int jourIndex = dateCours.getDayOfWeek().getValue() - 1;

        // Vérification des indices pour éviter les erreurs d'affichage
        if (jourIndex < 0 || jourIndex >= JOURS.length) {
            return; // Jour non valide
        }

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut());
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin());
        int span = finIndex - debutIndex;

        if (debutIndex < 0 || finIndex < 0 || span <= 0) {
            return; // Intervalle de temps non valide
        }

        VBox cellule = new VBox(5);
        cellule.setStyle("-fx-background-color: #2196F3; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150);
        cellule.setPrefHeight(span * 30 - 10);
        cellule.setMaxWidth(Double.MAX_VALUE);

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label classe = new Label(cours.getClasse());
        classe.setStyle("-fx-text-fill: white;");

        Label salle = new Label("Salle: " + cours.getNumeroSalle());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 11;");

        cellule.getChildren().addAll(matiere, classe, salle);

        grilleEmploi.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null &&
                        GridPane.getColumnIndex(node) != null &&
                        GridPane.getRowIndex(node) >= debutIndex + 1 &&
                        GridPane.getRowIndex(node) < finIndex + 1 &&
                        GridPane.getColumnIndex(node) == jourIndex + 1
        );

        grilleEmploi.add(cellule, jourIndex + 1, debutIndex + 1);
        GridPane.setRowSpan(cellule, span);
    }

    private static int trouverIndexHeure30Min(String heure) {
        if (heure == null) return -1;
        for (int i = 0; i < HEURES_30MIN.length; i++) {
            if (HEURES_30MIN[i].equals(heure)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean estDansLaSemaineActuelle(LocalDate date, LocalDate dateActuelle) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int semaineActuelle = dateActuelle.get(weekFields.weekOfWeekBasedYear());
        int semaineDate = date.get(weekFields.weekOfWeekBasedYear());
        return semaineDate == semaineActuelle;
    }

    private static VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa;");
        cellule.setPrefHeight(30);
        return cellule;
    }
}