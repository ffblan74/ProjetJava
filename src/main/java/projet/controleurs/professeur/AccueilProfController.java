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
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.controleurs.CRUDcsvControllerProf;
import projet.controleurs.CRUDNotification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

public class AccueilProfController {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private ComboBox<String> filtreClasse;
    @FXML private ComboBox<String> filtreMatiere;
    @FXML private Button supprimerCoursButton;

    private LocalDate dateActuelle;
    private LocalDate premierJourSemaineActuelle;

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
    private static final String CHEMIN_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String CHEMIN_NOTIFICATIONS = "src/main/resources/projet/csv/notifications.csv";

    private Utilisateur utilisateurConnecte;
    private List<Cours> listeCours;

    private Map<String, String> matiereColors = new HashMap<>();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private Cours coursSelectionne;

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY);

        if (Utilisateur.getUtilisateurConnecte() != null) {
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        }
        setEnseignant(utilisateurConnecte);

        matiereColors.put("Mathématiques", "#9400D3");
        matiereColors.put("Physique", "#95C6B2");
        matiereColors.put("Informatique", "#9400D3");
        matiereColors.put("Chimie", "#95C6B2");
        matiereColors.put("Histoire", "#9400D3");
        matiereColors.put("Français", "#95C6B2");

        initialiserGrille();
        afficherSemaine();
        actualiserCours();

        if (supprimerCoursButton != null) {
            supprimerCoursButton.setDisable(true);
        }
    }

    public void setEnseignant(Utilisateur enseignant) {
        this.utilisateurConnecte = enseignant;
        labelBienvenue.setText("Bienvenue, " + enseignant.getPrenom() + " " + enseignant.getNom());
        chargerCours();
    }

    private void chargerCours() {
        try {
            listeCours = new ArrayList<>();
            List<String[]> lignes = CRUDcsvControllerProf.lire(CHEMIN_COURS);
            // Enlève la première ligne si c'est un en-tête
            if (!lignes.isEmpty() && lignes.get(0).length > 0 && lignes.get(0)[0].equalsIgnoreCase("idCours")) {
                lignes.remove(0);
            }

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    // N'ajoute que les cours du prof actuel
                    if (utilisateurConnecte != null && cours.getEnseignantId() == utilisateurConnecte.getIdUtilisateur()) {
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur de traitement d'un cours: " + e.getMessage() + " Ligne: " + String.join(";", ligne));
                }
            }

            initialiserFiltres();
        } catch (IOException e) {
            System.err.println("Erreur de lecture de fichier: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de lecture", "Impossible de charger les cours : " + e.getMessage());
        }
    }

    private void initialiserFiltres() {
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

        filtreClasse.getItems().add(0, "Toutes les classes");
        filtreMatiere.getItems().add(0, "Toutes les matières");

        filtreClasse.getItems().addAll(classes);
        filtreMatiere.getItems().addAll(matieres);

        filtreClasse.getSelectionModel().selectFirst();
        filtreMatiere.getSelectionModel().selectFirst();

        filtreClasse.setOnAction(e -> actualiserCours());
        filtreMatiere.setOnAction(e -> actualiserCours());
    }

    private void initialiserGrille() {
        grilleEmploi.getChildren().clear();
        grilleEmploi.getColumnConstraints().clear();
        grilleEmploi.getRowConstraints().clear();

        coursSelectionne = null;
        if (supprimerCoursButton != null) {
            supprimerCoursButton.setDisable(true);
        }

        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setMinWidth(80);
        cc0.setPrefWidth(80);
        cc0.setHgrow(javafx.scene.layout.Priority.NEVER);
        grilleEmploi.getColumnConstraints().add(cc0);

        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(150);
            cc.setPrefWidth(150);
            cc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            grilleEmploi.getColumnConstraints().add(cc);
        }

        Label horairesLabel = new Label("Horaires");
        horairesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #9400D3; -fx-text-fill: white;");
        horairesLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        horairesLabel.setAlignment(javafx.geometry.Pos.CENTER);
        grilleEmploi.add(horairesLabel, 0, 0);


        LocalDate dateDuJour = premierJourSemaineActuelle;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int j = 0; j < JOURS.length; j++) {
            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(javafx.geometry.Pos.CENTER);
            dayHeader.setStyle("-fx-background-color: #9400D3; -fx-padding: 5; -fx-background-radius: 5;");

            Label jourLabel = new Label(JOURS[j]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            jourLabel.setTextAlignment(TextAlignment.CENTER);

            Label dateLabel = new Label(dateDuJour.format(dateFormatter));
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
            dateLabel.setTextAlignment(TextAlignment.CENTER);

            dayHeader.getChildren().addAll(jourLabel, dateLabel);
            grilleEmploi.add(dayHeader, j + 1, 0);
            dateDuJour = dateDuJour.plusDays(1);
        }

        for (int i = 0; i < HEURES_30MIN.length; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(40);
            rc.setPrefHeight(40);
            grilleEmploi.getRowConstraints().add(rc);

            Label heureLabel = new Label(HEURES_30MIN[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #9400D3; -fx-text-fill: white;");
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
        initialiserGrille();

        if (listeCours != null) {
            String selectedClasse = filtreClasse.getValue();
            String selectedMatiere = filtreMatiere.getValue();

            for (Cours cours : listeCours) {
                try {
                    LocalDate dateCours = cours.getDate();

                    boolean matchClasse = (selectedClasse == null || selectedClasse.equals("Toutes les classes") || selectedClasse.equals(cours.getClasse()));
                    boolean matchMatiere = (selectedMatiere == null || selectedMatiere.equals("Toutes les matières") || selectedMatiere.equals(cours.getMatiere()));

                    if (estDansLaSemaineActuelle(dateCours) && matchClasse && matchMatiere) {
                        ajouterCoursALaGrille(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur d'ajout de cours à la grille: " + e.getMessage() + " Cours: " + cours.getMatiere() + " - " + cours.getDate());
                }
            }
        }
        afficherStats();
    }


    private void ajouterCoursALaGrille(Cours cours) {
        LocalDate dateCours = cours.getDate();
        int jourIndex = dateCours.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();

        if (jourIndex < 0 || jourIndex >= JOURS.length) {
            System.err.println("Cours hors des jours ouvrés: " + cours.getMatiere() + " le " + dateCours.getDayOfWeek() + " (" + dateCours + ")");
            return;
        }

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut().format(timeFormatter));
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin().format(timeFormatter));

        if (debutIndex < 0 || finIndex < 0 || debutIndex >= HEURES_30MIN.length || finIndex > HEURES_30MIN.length) {
            System.err.println("Heures de cours non valides pour " + cours.getMatiere() + " : " + cours.getHeureDebut() + "-" + cours.getHeureFin());
            return;
        }

        int span = finIndex - debutIndex;
        if (span <= 0) span = 1;

        String backgroundColor = matiereColors.getOrDefault(cours.getMatiere(), "#9400D3");

        VBox cellule = new VBox(5);
        cellule.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150);
        cellule.setPrefHeight((span * 40) + ((span - 1) * 5) - 10);
        cellule.setMaxWidth(Double.MAX_VALUE);
        cellule.setPadding(new Insets(5));

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label description = new Label(cours.getDescription());
        description.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
        description.setWrapText(true);

        Label classe = new Label("Classe: " + cours.getClasse());
        classe.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");

        Label salle = new Label("Salle: " + cours.getSalle());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        cellule.getChildren().addAll(matiere, description, classe, salle);

        cellule.setOnMouseClicked(event -> {
            if (coursSelectionne != null) {
                String prevBackgroundColor = matiereColors.getOrDefault(coursSelectionne.getMatiere(), "#9400D3");
                grilleEmploi.getChildren().stream()
                        .filter(node -> node instanceof VBox && node.getUserData() instanceof Cours && ((Cours) node.getUserData()).getIdCours() == coursSelectionne.getIdCours())
                        .findFirst()
                        .ifPresent(node -> node.setStyle("-fx-background-color: " + prevBackgroundColor + "; -fx-padding: 5; -fx-background-radius: 5;"));
            }
            coursSelectionne = cours;
            cellule.setStyle("-fx-background-color: #95C6B2; -fx-padding: 5; -fx-background-radius: 5; -fx-border-color: #9400D3; -fx-border-width: 2;");
            if (supprimerCoursButton != null) {
                supprimerCoursButton.setDisable(false);
            }
            System.out.println("Cours sélectionné pour suppression : " + cours.getMatiere() + " (ID: " + cours.getIdCours() + ")");
        });
        cellule.setUserData(cours);

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
        LocalDate finSemaineActuelle = premierJourSemaineActuelle.plusDays(4);
        return !date.isBefore(premierJourSemaineActuelle) && !date.isAfter(finSemaineActuelle);
    }


    private VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa;");
        cellule.setPrefHeight(40);
        return cellule;
    }

    private void afficherSemaine() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int numeroSemaine = dateActuelle.get(weekFields.weekOfWeekBasedYear());

        LocalDate debutSemaine = premierJourSemaineActuelle;
        LocalDate finSemaine = premierJourSemaineActuelle.plusDays(4);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        labelSemaine.setText("Semaine du " + debutSemaine.format(formatter) + " au " + finSemaine.format(formatter) + " (Semaine " + numeroSemaine + ")");
    }

    private void afficherStats() {
        long nbCoursCetteSemaine = listeCours.stream()
                .filter(cours -> estDansLaSemaineActuelle(cours.getDate()))
                .filter(cours -> {
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
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY);
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY);
        afficherSemaine();
        actualiserCours();
    }

    @FXML
    private void ajouterCours() {
        System.out.println("Appel de NavigationUtil.ouvrirFenetreModale pour créer un cours.");
        NavigationUtil.ouvrirFenetreModale(
                "/projet/fxml/creer_cours_professeur.fxml",
                "Créer un Nouveau Cours",
                (Stage) grilleEmploi.getScene().getWindow(),
                utilisateurConnecte
        );
        System.out.println("Fenetre de création de cours fermée. Rechargement des cours.");
        chargerCours();
        actualiserCours();
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }

    @FXML
    private void supprimerCours() {
        if (coursSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun cours sélectionné", "Veuillez sélectionner un cours à supprimer dans l'emploi du temps.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le cours : " + coursSelectionne.getMatiere() + " le " +
                coursSelectionne.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " de " + coursSelectionne.getHeureDebut().format(timeFormatter) +
                " à " + coursSelectionne.getHeureFin().format(timeFormatter) +
                " en salle " + coursSelectionne.getSalle() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUDcsvControllerProf.supprimerLigne(CHEMIN_COURS, 0, String.valueOf(coursSelectionne.getIdCours()));
                System.out.println("Cours supprimé: ID " + coursSelectionne.getIdCours());

                genererNotificationsAnnulationCours(coursSelectionne);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé avec succès !");
                chargerCours();
                actualiserCours();
                coursSelectionne = null;
                if (supprimerCoursButton != null) {
                    supprimerCoursButton.setDisable(true);
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression", "Impossible de supprimer le cours : " + e.getMessage());
                System.err.println("Erreur suppression cours: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void genererNotificationsAnnulationCours(Cours coursAnnule) {
        String messageNotification = String.format("Le cours de %s prévu le %s de %s à %s en salle %s pour votre classe %s a été annulé.",
                coursAnnule.getMatiere(),
                coursAnnule.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                coursAnnule.getHeureDebut().format(timeFormatter),
                coursAnnule.getHeureFin().format(timeFormatter),
                coursAnnule.getSalle(),
                coursAnnule.getClasse());

        try {
            List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_UTILISATEURS);
            int startIndexUtilisateurs = (lignesUtilisateurs.size() > 0 && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].equalsIgnoreCase("idUtilisateur")) ? 1 : 0;

            for (int i = startIndexUtilisateurs; i < lignesUtilisateurs.size(); i++) {
                String[] userData = lignesUtilisateurs.get(i);
                if (userData.length > 6 && userData[5].trim().equalsIgnoreCase("ETUDIANT")) {
                    String eleveClasse = userData[6].trim().replace("\"", "");

                    if (eleveClasse.equalsIgnoreCase(coursAnnule.getClasse())) {
                        int idEleve = Integer.parseInt(userData[0].trim());

                        int idNotification = generateUniqueNotificationId();
                        projet.models.Notification newNotification = new projet.models.Notification(
                                idNotification,
                                java.time.LocalDateTime.now(),
                                messageNotification,
                                "AnnulationCours",
                                "NON_LUE",
                                coursAnnule.getEnseignantId(),
                                idEleve,
                                "ETUDIANT",
                                coursAnnule.getIdCours()
                        );
                        CRUDNotification.ajouterNotification(CHEMIN_NOTIFICATIONS, newNotification);
                        System.out.println("Notification d'annulation générée pour l'élève ID: " + idEleve + " pour le cours ID: " + coursAnnule.getIdCours());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des utilisateurs ou de l'écriture des notifications d'annulation: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique lors de la lecture des IDs d'utilisateur pour notification d'annulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int generateUniqueNotificationId() {
        int maxId = 0;
        try {
            List<projet.models.Notification> notifications = CRUDNotification.lireNotifications(CHEMIN_NOTIFICATIONS);
            for (projet.models.Notification notif : notifications) {
                if (notif.getIdNotification() > maxId) {
                    maxId = notif.getIdNotification();
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier des notifications pour générer un ID. Retourne 1 si le fichier n'existe pas encore: " + e.getMessage());
            return 1;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}