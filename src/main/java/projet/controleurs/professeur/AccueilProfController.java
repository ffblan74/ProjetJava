package projet.controleurs.professeur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.RowConstraints;

import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.controleurs.CRUDcsvController;

import java.time.LocalDate;
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

    // Jours de la semaine (lundi à vendredi)
    private final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

    // Créneaux 30 min entre 8h et 17h30 inclus
    private final String[] HEURES_30MIN = {
            "08:00", "08:30", "09:00", "09:30",
            "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", // Si pause déjeuner pas utilisée, tu peux gérer différemment
            "13:00", "13:30", "14:00", "14:30",
            "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30"
    };

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private Utilisateur utilisateurConnecte;
    private List<Cours> listeCours;

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();

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
                    // S'assurer que le cours appartient à l'enseignant connecté
                    if (utilisateurConnecte != null && cours.getEnseignantId() == utilisateurConnecte.getIdUtilisateur()) {
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
        int jourIndex = dateCours.getDayOfWeek().getValue() - 1; // Lundi=1 (ajusté pour être 0-indexed)

        // Vérifier que le jour est dans notre tableau de JOURS (Lundi à Vendredi)
        if (jourIndex < 0 || jourIndex >= JOURS.length) {
            System.err.println("Cours hors plage jours ouvrés (L-V): " + cours.getMatiere() + " le " + dateCours.getDayOfWeek());
            return;
        }

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut());
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin());

        if (debutIndex < 0 || finIndex < 0 || debutIndex >= HEURES_30MIN.length || finIndex > HEURES_30MIN.length) {
            System.err.println("Heures de cours invalides ou hors plage pour " + cours.getMatiere() + " : " + cours.getHeureDebut() + "-" + cours.getHeureFin());
            return; // Heures invalides ou hors plage horaires
        }

        int span = finIndex - debutIndex;
        if (span <= 0) span = 1; // Minimum 1 case, même si heure de début et fin sont identiques (devrait être évité par validation)

        VBox cellule = new VBox(5);
        cellule.setStyle("-fx-background-color: #2196F3; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150);
        // Ajuster la hauteur pour tenir compte du span et du padding/spacing
        cellule.setPrefHeight(span * grilleEmploi.getRowConstraints().get(debutIndex + 1).getPrefHeight() - 10); // -10 pour la marge/espacement visuel
        cellule.setMaxWidth(Double.MAX_VALUE);

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label classe = new Label(cours.getClasse());
        classe.setStyle("-fx-text-fill: white;");

        // Assurez-vous que la salle est disponible via un getter dans Cours ou Salle
        // Pour l'instant, cours.getSalle() n'est pas directement disponible dans le modèle Cours que tu as fourni.
        // Il faudrait charger la salle par son ID. Pour simplifier, je mets l'ID de la salle pour l'instant.
        // Si tu veux le numéro de salle, il faudra ajouter une logique pour le récupérer depuis 'listeSalles'
        // dans le CreerCoursProfController ou le charger ici aussi.
        Label salle = new Label("Salle ID: " + cours.getSalleId());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 11;");

        cellule.getChildren().addAll(matiere, classe, salle);

        // Supprimer la cellule vide sous-jacente pour éviter superposition
        // Il est important de bien cibler la cellule vide à supprimer.
        grilleEmploi.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null &&
                        GridPane.getColumnIndex(node) != null &&
                        GridPane.getRowIndex(node) == debutIndex + 1 && // +1 car la ligne 0 est l'en-tête des jours
                        GridPane.getColumnIndex(node) == jourIndex + 1 && // +1 car la colonne 0 est les heures
                        node instanceof VBox // S'assurer qu'on supprime une cellule VBox (vide)
        );

        grilleEmploi.add(cellule, jourIndex + 1, debutIndex + 1);
        GridPane.setRowSpan(cellule, span);
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
        int anneeActuelle = dateActuelle.getYear();
        int semaineDate = date.get(weekFields.weekOfWeekBasedYear());
        int anneeDate = date.getYear();

        // Comparer aussi l'année pour éviter les confusions de numéros de semaine sur différentes années
        return semaineDate == semaineActuelle && anneeDate == anneeActuelle;
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
        // Filtrer les cours pour la semaine actuelle et l'enseignant connecté pour les stats
        long nbCoursCetteSemaine = listeCours.stream()
                .filter(cours -> estDansLaSemaineActuelle(cours.getDate()))
                .count();

        labelStats.setText("Nombre de cours cette semaine : " + nbCoursCetteSemaine);
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
    private void ajouterCours() {
        // This is the correct call based on the error message you provided:
        // required: java.lang.String,java.lang.String,javafx.stage.Stage,java.lang.Object
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/creer_cours_professeur.fxml", // 1st arg: FXML path (String)
                "Créer un Nouveau Cours",                          // 2nd arg: Title (String)
                (Stage) grilleEmploi.getScene().getWindow(),       // 3rd arg: Current Stage to close (Stage)
                utilisateurConnecte                               // 4th arg: Data to pass (Object)
        );
        // After the creation form potentially closes and saves, reload and refresh
        chargerCours();
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