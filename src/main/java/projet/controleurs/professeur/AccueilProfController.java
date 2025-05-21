package projet.controleurs.professeur;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.controleurs.CRUDcsvController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;

public class AccueilProfController {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private ComboBox<String> filtreClasse;
    @FXML private ComboBox<String> filtreMatiere;

    private LocalDate dateActuelle;
    private final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private final String[] HEURES = {"8:00", "9:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"};
    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private Utilisateur utilisateurConnecte;
    private List<Cours> listeCours;

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();

        // Initialisation de l'utilisateur connecté
        if (Utilisateur.getUtilisateurConnecte() != null) {
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        }
        setEnseignant(utilisateurConnecte);

        initialiserGrille();
        afficherSemaine();
    }

    public void setEnseignant(Utilisateur enseignant) {
        this.utilisateurConnecte = enseignant;
        labelBienvenue.setText("Bienvenue, " + enseignant.getPrenom() + " " + enseignant.getNom());
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
                    if (cours.getEnseignantId() == utilisateurConnecte.getIdUtilisateur()) {
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
        filtreClasse.setOnAction(e -> actualiserCours());
        filtreMatiere.setOnAction(e -> actualiserCours());

        List<String> classes = new ArrayList<>();
        List<String> matieres = new ArrayList<>();

        for (Cours cours : listeCours) {
            if (!classes.contains(cours.getClasse())) {
                classes.add(cours.getClasse());
            }
            if (!matieres.contains(cours.getMatiere())) {
                matieres.add(cours.getMatiere());
            }
        }

        filtreClasse.getItems().setAll(classes);
        filtreMatiere.getItems().setAll(matieres);
    }

    private void initialiserGrille() {
        grilleEmploi.getChildren().clear();

        // En-têtes des jours
        for (int j = 0; j < JOURS.length; j++) {
            Label jourLabel = new Label(JOURS[j]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            grilleEmploi.add(jourLabel, j + 1, 0);
        }

        // En-têtes des heures
        for (int i = 0; i < HEURES.length; i++) {
            Label heureLabel = new Label(HEURES[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            grilleEmploi.add(heureLabel, 0, i + 1);
        }

        // Cellules vides
        for (int i = 0; i < HEURES.length; i++) {
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

                    boolean matchClasse = filtreClasse.getValue() == null || filtreClasse.getValue().equals(cours.getClasse());
                    boolean matchMatiere = filtreMatiere.getValue() == null || filtreMatiere.getValue().equals(cours.getMatiere());

                    if (estDansLaSemaineActuelle(dateCours) && matchClasse && matchMatiere) {
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
        int jourIndex = dateCours.getDayOfWeek().getValue() - 1;
        int heureIndex = trouverIndexHeure(cours.getHeureDebut());

        if (heureIndex >= 0 && jourIndex >= 0 && jourIndex < JOURS.length) {
            VBox cellule = new VBox(5);
            cellule.setStyle("-fx-background-color: #a34c6c; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");

            Label labelMatiere = new Label(cours.getMatiere());
            labelMatiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            Label labelSalle = new Label("Salle: " + cours.getSalleId() + " | Classe: " + cours.getClasse());
            labelSalle.setStyle("-fx-text-fill: white;");

            Label labelHoraire = new Label(cours.getHeureDebut() + " - " + cours.getHeureFin());
            labelHoraire.setStyle("-fx-text-fill: white;");

            cellule.getChildren().addAll(labelMatiere, labelSalle, labelHoraire);
            grilleEmploi.add(cellule, jourIndex + 1, heureIndex + 1);
        }
    }

    private VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-padding: 5; -fx-min-height: 60;");
        return cellule;
    }

    private boolean estDansLaSemaineActuelle(LocalDate dateCours) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return dateCours.get(weekFields.weekOfWeekBasedYear()) == dateActuelle.get(weekFields.weekOfWeekBasedYear())
                && dateCours.getYear() == dateActuelle.getYear();
    }

    private int trouverIndexHeure(String heure) {
        for (int i = 0; i < HEURES.length; i++) {
            if (HEURES[i].equals(heure)) {
                return i;
            }
        }
        return -1;
    }

    private void afficherSemaine() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int numeroSemaine = dateActuelle.get(weekFields.weekOfWeekBasedYear());
        String periode = dateActuelle.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        labelSemaine.setText("Semaine " + numeroSemaine + " (" + periode + ")");
        actualiserCours();
    }

    private void afficherStats() {
        long total = listeCours.stream()
                .filter(c -> {
                    try {
                        return estDansLaSemaineActuelle(c.getDate());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        labelStats.setText("Cours cette semaine : " + total);
    }

    @FXML
    private void semainePrecedente() {
        dateActuelle = dateActuelle.minusWeeks(1);
        afficherSemaine();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        afficherSemaine();
    }

    @FXML
    private void ajouterCours() {
        Stage stage = (Stage) labelBienvenue.getScene().getWindow();
        NavigationUtil.changerScene(
                stage,
                "/projet/fxml/professeur/ajouter-cours.fxml",
                "Ajouter un cours",
                utilisateurConnecte
        );
    }

    @FXML
    private void deconnexion() {
        Stage stage = (Stage) labelBienvenue.getScene().getWindow();
        NavigationUtil.changerScene(stage, "/projet/fxml/login.fxml", "Connexion", null);
    }
}
