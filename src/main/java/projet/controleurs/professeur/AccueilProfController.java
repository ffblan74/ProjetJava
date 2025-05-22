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
import javafx.scene.paint.Color; // Import pour les couleurs

import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.controleurs.CRUDcsvController; // Assurez-vous que c'est le bon contrôleur CRUD (pour lire)

import java.time.DayOfWeek; // Pour obtenir le jour de la semaine
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Pour formater les dates
import java.time.temporal.WeekFields;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Optional; // Pour Optional<Color>
import java.util.HashMap; // Pour stocker les couleurs des matières
import java.util.Map; // Pour Map

public class AccueilProfController {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private ComboBox<String> filtreClasse;
    @FXML private ComboBox<String> filtreMatiere;

    private LocalDate dateActuelle;
    private LocalDate premierJourSemaineActuelle; // Pour stocker le lundi de la semaine affichée

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
    private Utilisateur utilisateurConnecte;
    private List<Cours> listeCours;

    private Map<String, String> matiereColors = new HashMap<>(); // Pour stocker les couleurs des matières

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();
        // S'assurer que dateActuelle est le lundi de la semaine courante
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY);

        if (Utilisateur.getUtilisateurConnecte() != null) {
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        }
        setEnseignant(utilisateurConnecte); // Cette méthode appelle déjà chargerCours() et actualiserCours()

        // Définir les couleurs pour certaines matières
        matiereColors.put("Mathématiques", "#7F00FF"); // Violet
        matiereColors.put("Physique", "#4CAF50");    // Vert
        matiereColors.put("Informatique", "#FF5733"); // Orange
        matiereColors.put("Chimie", "#33B5E5"); // Bleu clair
        matiereColors.put("Histoire", "#E91E63"); // Rose foncé
        matiereColors.put("Français", "#00BCD4"); // Cyan
        // Ajoutez d'autres matières et leurs couleurs si nécessaire

        initialiserGrille(); // Appelé une fois pour la structure de base
        afficherSemaine();   // Met à jour le label de la semaine
        actualiserCours();   // Remplit la grille avec les cours
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
                lignes.remove(0); // Supprimer l'en-tête
            }

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    // Filtrer les cours par l'enseignant connecté
                    if (utilisateurConnecte != null && cours.getEnseignantId() == utilisateurConnecte.getIdUtilisateur()) {
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing cours (AccueilProfController): " + e.getMessage() + " Ligne: " + String.join(";", ligne));
                    // Considérez d'afficher une alerte si c'est une erreur critique, ou de loguer.
                }
            }

            initialiserFiltres();
            // actualiserCours() est appelé après le chargement pour initialiser l'affichage
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours (AccueilProfController): " + e.getMessage());
            // Afficher une alerte ou un message d'erreur à l'utilisateur si le fichier est introuvable
        }
    }

    private void initialiserFiltres() {
        // Clear previous items to avoid duplicates on re-initialization
        filtreClasse.getItems().clear();
        filtreMatiere.getItems().clear();

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

        // Ajouter une option "Tous" aux filtres
        filtreClasse.getItems().add(0, "Toutes les classes");
        filtreMatiere.getItems().add(0, "Toutes les matières");

        filtreClasse.getItems().addAll(classes);
        filtreMatiere.getItems().addAll(matieres);

        // Sélectionner "Tous" par défaut
        filtreClasse.getSelectionModel().selectFirst();
        filtreMatiere.getSelectionModel().selectFirst();

        // Les handlers sont initialisés ici pour éviter de les ajouter plusieurs fois
        filtreClasse.setOnAction(e -> actualiserCours());
        filtreMatiere.setOnAction(e -> actualiserCours());
    }

    private void initialiserGrille() {
        grilleEmploi.getChildren().clear();
        grilleEmploi.getColumnConstraints().clear();
        grilleEmploi.getRowConstraints().clear();

        // Colonne pour les horaires (index 0)
        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setMinWidth(80);
        cc0.setPrefWidth(80);
        cc0.setHgrow(javafx.scene.layout.Priority.NEVER); // Ne pas étirer
        grilleEmploi.getColumnConstraints().add(cc0);

        // Colonnes pour les jours (index 1 à 5)
        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(150);
            cc.setPrefWidth(150);
            cc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            grilleEmploi.getColumnConstraints().add(cc);
        }

        // Cellule "Horaires"
        Label horairesLabel = new Label("Horaires");
        horairesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #6a1b9a; -fx-text-fill: white;"); // Violet
        horairesLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        horairesLabel.setAlignment(javafx.geometry.Pos.CENTER);
        grilleEmploi.add(horairesLabel, 0, 0);


        // En-têtes de jour (Lundi, Mardi, ...) avec les dates
        LocalDate dateDuJour = premierJourSemaineActuelle;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM"); // Format pour les dates

        for (int j = 0; j < JOURS.length; j++) {
            VBox dayHeader = new VBox(2); // VBox pour le jour et la date
            dayHeader.setAlignment(javafx.geometry.Pos.CENTER);
            dayHeader.setStyle("-fx-background-color: #6a1b9a; -fx-padding: 5; -fx-background-radius: 5;"); // Violet

            Label jourLabel = new Label(JOURS[j]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            jourLabel.setTextAlignment(TextAlignment.CENTER);

            Label dateLabel = new Label(dateDuJour.format(dateFormatter));
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
            dateLabel.setTextAlignment(TextAlignment.CENTER);

            dayHeader.getChildren().addAll(jourLabel, dateLabel);
            grilleEmploi.add(dayHeader, j + 1, 0);
            dateDuJour = dateDuJour.plusDays(1); // Passer au jour suivant
        }

        // Lignes pour les horaires et les cellules vides
        // La première ligne (index 0) est pour les en-têtes de jour/horaires.
        // Les lignes suivantes (i + 1) sont pour les heures.
        for (int i = 0; i < HEURES_30MIN.length; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(40); // Augmenter la hauteur pour un meilleur visuel
            rc.setPrefHeight(40);
            grilleEmploi.getRowConstraints().add(rc);

            Label heureLabel = new Label(HEURES_30MIN[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #6a1b9a; -fx-text-fill: white;"); // Violet
            heureLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            heureLabel.setAlignment(javafx.geometry.Pos.CENTER);
            grilleEmploi.add(heureLabel, 0, i + 1);

            for (int j = 0; j < JOURS.length; j++) {
                VBox cellule = creerCelluleVide();
                grilleEmploi.add(cellule, j + 1, i + 1);
            }
        }
    }

    private void actualiserCours() {
        // Important: Recréer la grille à chaque fois pour effacer les anciens cours
        // et mettre à jour les dates des jours.
        initialiserGrille();

        if (listeCours != null) {
            String selectedClasse = filtreClasse.getValue();
            String selectedMatiere = filtreMatiere.getValue();

            for (Cours cours : listeCours) {
                try {
                    LocalDate dateCours = cours.getDate();

                    // Filtrage
                    boolean matchClasse = (selectedClasse == null || selectedClasse.equals("Toutes les classes") || selectedClasse.equals(cours.getClasse()));
                    boolean matchMatiere = (selectedMatiere == null || selectedMatiere.equals("Toutes les matières") || selectedMatiere.equals(cours.getMatiere()));

                    if (estDansLaSemaineActuelle(dateCours) && matchClasse && matchMatiere) {
                        ajouterCoursALaGrille(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'ajout d'un cours à la grille (vérifier la date/heure ou l'ordre CSV): " + e.getMessage() + " Cours: " + cours.getMatiere() + " - " + cours.getDate());
                }
            }
        }
        afficherStats();
    }


    private void ajouterCoursALaGrille(Cours cours) {
        LocalDate dateCours = cours.getDate();
        // Calculer l'index du jour de la semaine par rapport au lundi de la semaine affichée
        int jourIndex = dateCours.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue(); // Lundi=0, Mardi=1, ...

        if (jourIndex < 0 || jourIndex >= JOURS.length) {
            System.err.println("Cours hors plage jours ouvrés (L-V): " + cours.getMatiere() + " le " + dateCours.getDayOfWeek() + " (" + dateCours + ")");
            return;
        }

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut()); // <--- Supprimez le .format(...)
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin());

        if (debutIndex < 0 || finIndex < 0 || debutIndex >= HEURES_30MIN.length || finIndex > HEURES_30MIN.length) {
            System.err.println("Heures de cours invalides ou hors plage pour " + cours.getMatiere() + " : " + cours.getHeureDebut() + "-" + cours.getHeureFin());
            return;
        }

        int span = finIndex - debutIndex;
        if (span <= 0) span = 1; // Un cours dure au moins une demi-heure

        // Récupérer la couleur pour la matière, sinon utiliser une couleur par défaut (violet clair)
        String backgroundColor = matiereColors.getOrDefault(cours.getMatiere(), "#9C27B0"); // Vert par défaut pour les non-définis

        VBox cellule = new VBox(5);
        cellule.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150);
        // Calculer la hauteur en fonction du span et de la hauteur d'une cellule d'heure
        // La hauteur de la cellule d'heure est définie par rc.setPrefHeight(40) + vgap (5) = 45.
        // Il faut prendre en compte le vgap dans la grille. Si vgap est 5, chaque cellule fait 40+5=45px.
        cellule.setPrefHeight((span * 40) + ((span - 1) * 5) - 10); // 40px/heure + 5px de vgap entre les heures, -10px de padding global
        cellule.setMaxWidth(Double.MAX_VALUE);
        cellule.setPadding(new Insets(5)); // Ajouter un padding interne

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label description = new Label(cours.getDescription()); // Afficher la description du cours
        description.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
        description.setWrapText(true); // Permet le retour à la ligne

        Label classe = new Label("Classe: " + cours.getClasse());
        classe.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");

        Label salle = new Label("Salle: " + cours.getSalle()); // Utiliser getSalle() pour l'ID de la salle
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        cellule.getChildren().addAll(matiere, description, classe, salle); // Ordre d'affichage dans la cellule

        // Retirer les cellules vides qui seraient remplacées par le cours
        for (int i = 0; i < span; i++) {
            int targetRow = debutIndex + 1 + i;
            int targetCol = jourIndex + 1;
            Optional<javafx.scene.Node> existingNode = grilleEmploi.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
                            GridPane.getRowIndex(node) == targetRow &&
                            GridPane.getColumnIndex(node) == targetCol)
                    .findFirst();
            existingNode.ifPresent(grilleEmploi.getChildren()::remove);
        }

        grilleEmploi.add(cellule, jourIndex + 1, debutIndex + 1);
        GridPane.setRowSpan(cellule, span); // Étendre le cours sur plusieurs lignes
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
        // La date du cours doit être entre le lundi (inclus) et le vendredi (inclus) de la semaine affichée
        LocalDate finSemaineActuelle = premierJourSemaineActuelle.plusDays(4); // Lundi + 4 jours = Vendredi
        return !date.isBefore(premierJourSemaineActuelle) && !date.isAfter(finSemaineActuelle);
    }


    private VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa;");
        cellule.setPrefHeight(40); // Doit correspondre à la RowConstraints pour une cellule de base
        return cellule;
    }

    private void afficherSemaine() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int numeroSemaine = dateActuelle.get(weekFields.weekOfWeekBasedYear());

        // Afficher la plage de dates de la semaine
        LocalDate debutSemaine = premierJourSemaineActuelle;
        LocalDate finSemaine = premierJourSemaineActuelle.plusDays(4); // Vendredi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        labelSemaine.setText("Semaine du " + debutSemaine.format(formatter) + " au " + finSemaine.format(formatter) + " (Semaine " + numeroSemaine + ")");
    }

    private void afficherStats() {
        long nbCoursCetteSemaine = listeCours.stream()
                .filter(cours -> estDansLaSemaineActuelle(cours.getDate()))
                .filter(cours -> { // Appliquer les filtres pour les stats aussi
                    String selectedClasse = filtreClasse.getValue();
                    String selectedMatiere = filtreMatiere.getValue();
                    boolean matchClasse = (selectedClasse == null || selectedClasse.equals("Toutes les classes") || selectedClasse.equals(cours.getClasse()));
                    boolean matchMatiere = (selectedMatiere == null || selectedMatiere.equals("Toutes les matières") || selectedMatiere.equals(cours.getMatiere()));
                    return matchClasse && matchMatiere;
                })
                .count();

        labelStats.setText("Nombre de cours cette semaine : " + nbCoursCetteSemaine);
    }

    @FXML
    private void semainePrecedente() {
        dateActuelle = dateActuelle.minusWeeks(1);
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY); // Mettre à jour le lundi de la semaine
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY); // Mettre à jour le lundi de la semaine
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void ajouterCours() {
        System.out.println("DEBUG (AccueilProfController): Appel de NavigationUtil.ouvrirFenetreModale pour créer un cours.");
        NavigationUtil.ouvrirFenetreModale(
                "/projet/fxml/creer_cours_professeur.fxml",
                "Créer un Nouveau Cours",
                (Stage) grilleEmploi.getScene().getWindow(),
                utilisateurConnecte
        );
        System.out.println("DEBUG (AccueilProfController): Fenêtre de création de cours fermée. Rechargement des cours.");
        chargerCours(); // Recharge les données au cas où un nouveau cours aurait été ajouté
        actualiserCours(); // Met à jour l'affichage de l'emploi du temps
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }
}