package projet.controleurs.eleve;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.time.DayOfWeek;
import projet.controleurs.CRUDcsvController;
import projet.controleurs.CRUDNotification;
import projet.models.Etudiant;
import projet.models.Utilisateur;
import projet.models.Notification;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.utils.Transmissible;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class AccueilEleveController implements Initializable, Transmissible {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private ComboBox<String> filtreMatiere;

    @FXML private Label unreadNotificationsCountLabel;
    @FXML private ListView<String> notificationsListView;
    @FXML private ComboBox<String> filtreStatutNotifications; // NOUVEAU: ComboBox pour les notifications

    private LocalDate dateActuelle;
    private LocalDate premierJourSemaineActuelle;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

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
    private static final String CHEMIN_NOTIFICATIONS = "src/main/resources/projet/csv/notifications.csv";
    private Etudiant utilisateurConnecte;
    private List<Cours> listeCours;
    private List<Notification> toutesLesNotifications; // Stocke toutes les notifications de l'élève

    // Couleurs pour les matières (comme chez le prof)
    private Map<String, String> matiereColors = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateActuelle = LocalDate.now();
        premierJourSemaineActuelle = dateActuelle.with(DayOfWeek.MONDAY);

        // Initialisation des couleurs des matières
        matiereColors.put("Mathématiques", "#9400D3");
        matiereColors.put("Physique", "#95C6B2");
        matiereColors.put("Informatique", "#9400D3");
        matiereColors.put("Chimie", "#95C6B2");
        matiereColors.put("Histoire", "#9400D3");
        matiereColors.put("Français", "#95C6B2");
        // Ajoutez d'autres matières et couleurs si nécessaire

        initialiserGrille();
        afficherSemaine();
        initialiserFiltreNotifications(); // NOUVEAU: Initialise le filtre de notifications
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Etudiant) data;
            System.out.println("DEBUG (AccueilEleveController): Utilisateur étudiant connecté reçu: " + utilisateurConnecte.getNom());
            setEleve(utilisateurConnecte);
            chargerNotifications(); // Charger les notifications après que l'utilisateur soit défini
        } else {
            System.err.println("ERREUR (AccueilEleveController): Données transmises via Transmissible ne sont pas de type Utilisateur.");
            labelBienvenue.setText("Erreur : Utilisateur non défini.");
        }
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
            if (!lignes.isEmpty() && lignes.get(0).length > 0 && lignes.get(0)[0].equalsIgnoreCase("idCours")) {
                lignes.remove(0); // Supprimer l'en-tête
            }

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    // L'élève ne voit que les cours de sa classe
                    if (utilisateurConnecte != null && cours.getClasse().equals(utilisateurConnecte.getGroupe())) {
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing cours: " + e.getMessage() + " Ligne: " + String.join(";", ligne));
                }
            }

            initialiserFiltres();
            actualiserCours();
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours: " + e.getMessage());
        }
    }

    private void initialiserFiltres() {
        filtreMatiere.getItems().clear();
        filtreMatiere.getItems().add(0, "Toutes les matières");
        filtreMatiere.getSelectionModel().selectFirst();

        List<String> matieres = listeCours.stream()
                .map(Cours::getMatiere)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        filtreMatiere.getItems().addAll(matieres);
        filtreMatiere.setOnAction(e -> actualiserCours());
    }

    private void initialiserGrille() {
        grilleEmploi.getChildren().clear();
        grilleEmploi.getColumnConstraints().clear();
        grilleEmploi.getRowConstraints().clear();

        // Colonne pour les horaires
        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setMinWidth(80);
        cc0.setPrefWidth(80);
        cc0.setHgrow(javafx.scene.layout.Priority.NEVER);
        grilleEmploi.getColumnConstraints().add(cc0);

        // Colonnes pour chaque jour (Lundi à Vendredi)
        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(150);
            cc.setPrefWidth(150);
            cc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            grilleEmploi.getColumnConstraints().add(cc);
        }

        // Cellule d'en-tête pour les horaires
        Label horairesLabel = new Label("Horaires");
        horairesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #9400D3; -fx-text-fill: white;");
        horairesLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        horairesLabel.setAlignment(javafx.geometry.Pos.CENTER);
        grilleEmploi.add(horairesLabel, 0, 0);

        // En-têtes des jours (Lundi à Vendredi) avec date
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

        // Lignes pour les heures et cellules vides
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
        initialiserGrille(); // Nettoie et recrée la grille

        if (listeCours != null) {
            String selectedMatiere = filtreMatiere.getValue();

            for (Cours cours : listeCours) {
                try {
                    LocalDate dateCours = cours.getDate();

                    boolean matchMatiere = (selectedMatiere == null || selectedMatiere.equals("Toutes les matières") || selectedMatiere.equals(cours.getMatiere()));

                    if (estDansLaSemaineActuelle(dateCours) && matchMatiere) {
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
        int jourIndex = dateCours.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue(); // Lundi=0, Mardi=1, etc.

        if (jourIndex < 0 || jourIndex >= JOURS.length) { // Assurez-vous d'être dans Lundi-Vendredi
            System.err.println("Cours hors plage jours ouvrés (L-V): " + cours.getMatiere() + " le " + dateCours.getDayOfWeek() + " (" + dateCours + ")");
            return;
        }

        int debutIndex = trouverIndexHeure30Min(cours.getHeureDebut().format(timeFormatter));
        int finIndex = trouverIndexHeure30Min(cours.getHeureFin().format(timeFormatter));

        if (debutIndex < 0 || finIndex < 0 || debutIndex >= HEURES_30MIN.length || finIndex > HEURES_30MIN.length) {
            System.err.println("Heures de cours invalides ou hors plage pour " + cours.getMatiere() + " : " + cours.getHeureDebut() + "-" + cours.getHeureFin());
            return;
        }

        int span = finIndex - debutIndex; // Nombre de cellules que le cours doit couvrir
        if (span <= 0) span = 1; // Un cours doit au moins prendre une cellule

        String backgroundColor = matiereColors.getOrDefault(cours.getMatiere(), "#9400D3"); // Couleur par défaut si non trouvée

        VBox cellule = new VBox(5); // Spacing entre les éléments dans la cellule
        cellule.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 5; -fx-background-radius: 5;");
        cellule.setPrefWidth(150); // Largeur préférée
        // Calcul de la hauteur préférée en fonction du span (hauteur d'une heure + spacing)
        cellule.setPrefHeight((span * 40) + ((span - 1) * 5) - 10); // 40px par ligne + 5px de vgap - un peu de padding
        cellule.setMaxWidth(Double.MAX_VALUE);
        cellule.setPadding(new Insets(5)); // Padding interne

        Label matiere = new Label(cours.getMatiere());
        matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label description = new Label(cours.getDescription());
        description.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
        description.setWrapText(true); // Permet au texte de passer à la ligne

        Label classe = new Label("Classe: " + cours.getClasse());
        classe.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");

        Label salle = new Label("Salle: " + cours.getSalle());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        cellule.getChildren().addAll(matiere, description, classe, salle);

        // Comportement de clic simple pour l'élève (afficher les détails)
        cellule.setOnMouseClicked(event -> {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Détails du Cours");
            infoAlert.setHeaderText(cours.getMatiere() + " - " + cours.getClasse());
            infoAlert.setContentText(
                    "Heure: " + cours.getHeureDebut().format(timeFormatter) + " - " + cours.getHeureFin().format(timeFormatter) + "\n" +
                            "Salle: " + cours.getSalle() + "\n" +
                            "Prof: " + cours.getEnseignantNomComplet() + "\n" +
                            "Description: " + cours.getDescription()
            );
            infoAlert.showAndWait();
        });


        // Retirer les cellules vides que ce cours va recouvrir
        for (int i = 0; i < span; i++) {
            int targetRow = debutIndex + 1 + i; // +1 car la ligne 0 est l'en-tête des heures
            int targetCol = jourIndex + 1;      // +1 car la colonne 0 est l'en-tête des jours
            // Trouver et supprimer le VBox à cette position
            grilleEmploi.getChildren().removeIf(node ->
                    GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
                            GridPane.getRowIndex(node) == targetRow &&
                            GridPane.getColumnIndex(node) == targetCol);
        }

        // Ajouter la cellule du cours à la grille
        grilleEmploi.add(cellule, jourIndex + 1, debutIndex + 1);
        GridPane.setRowSpan(cellule, span); // Étendre sur plusieurs lignes si le cours dure plus de 30 min
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
        LocalDate finSemaineActuelle = premierJourSemaineActuelle.plusDays(4); // La semaine finit le Vendredi
        return !date.isBefore(premierJourSemaineActuelle) && !date.isAfter(finSemaineActuelle);
    }

    private VBox creerCelluleVide() {
        VBox cellule = new VBox();
        cellule.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa;");
        cellule.setPrefHeight(40); // Hauteur par défaut pour une cellule de 30 min
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
                    String selectedMatiere = filtreMatiere.getValue();
                    boolean matchMatiere = (selectedMatiere == null || selectedMatiere.equals("Toutes les matières") || selectedMatiere.equals(cours.getMatiere()));
                    return matchMatiere;
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
    private void deconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }

    // --- Gestion des notifications ---
    private void initialiserFiltreNotifications() {
        filtreStatutNotifications.getItems().addAll("Toutes", "Non lues", "Lues");
        filtreStatutNotifications.getSelectionModel().selectFirst(); // Sélectionne "Toutes" par défaut
    }

    // Méthode appelée lorsque le filtre des notifications change
    @FXML
    private void filtrerNotifications() {
        afficherNotifications(); // Appel la méthode d'affichage avec le filtre actuel
    }

    private void chargerNotifications() {
        if (utilisateurConnecte == null) {
            System.err.println("ERREUR: Impossible de charger les notifications, utilisateur non défini.");
            return;
        }
        try {
            // Charge TOUTES les notifications destinées à l'utilisateur connecté
            List<Notification> toutesLesNotifsUser = CRUDNotification.lireNotifications(CHEMIN_NOTIFICATIONS).stream()
                    .filter(notif -> notif.getIdDestinataire() == utilisateurConnecte.getIdUtilisateur() && "ETUDIANT".equalsIgnoreCase(notif.getTypeDestinataire()))
                    .collect(Collectors.toList());
            this.toutesLesNotifications = toutesLesNotifsUser; // Stocke-les toutes

            afficherNotifications(); // Affiche les notifications en fonction du filtre actuel

            long unreadCount = toutesLesNotifsUser.stream()
                    .filter(notif -> "NON_LUE".equalsIgnoreCase(notif.getStatut()))
                    .count();
            if (unreadNotificationsCountLabel != null) {
                unreadNotificationsCountLabel.setText(String.valueOf(unreadCount));
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture des notifications : " + e.getMessage());
        }
    }

    private void afficherNotifications() {
        if (notificationsListView == null || toutesLesNotifications == null) {
            return; // S'assurer que les composants sont initialisés et les notifications chargées
        }

        notificationsListView.getItems().clear();
        String filtreSelectionne = filtreStatutNotifications.getValue();

        List<Notification> notificationsFiltrees = toutesLesNotifications.stream()
                .filter(notif -> {
                    if ("Non lues".equals(filtreSelectionne)) {
                        return "NON_LUE".equalsIgnoreCase(notif.getStatut());
                    } else if ("Lues".equals(filtreSelectionne)) {
                        return "LUE".equalsIgnoreCase(notif.getStatut());
                    }
                    return true; // "Toutes" ou aucun filtre sélectionné
                })
                .sorted((n1, n2) -> n2.getDateHeure().compareTo(n1.getDateHeure())) // Tri par date/heure décroissante
                .collect(Collectors.toList());

        notificationsFiltrees.forEach(notif -> {
            String display = notif.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " - " + notif.getMessage();
            if ("NON_LUE".equalsIgnoreCase(notif.getStatut())) {
                display = "[NOUVEAU] " + display;
            }
            notificationsListView.getItems().add(display);
        });
    }


    @FXML
    private void marquerToutCommeLu() {
        if (utilisateurConnecte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté.");
            return;
        }

        try {
            // Récupérer toutes les notifications (pas seulement celles de l'utilisateur actuel)
            // pour éviter d'écraser les notifications d'autres utilisateurs
            List<Notification> toutesLesNotificationsDuFichier = CRUDNotification.lireNotifications(CHEMIN_NOTIFICATIONS);
            boolean changed = false;

            for (Notification notif : toutesLesNotificationsDuFichier) {
                // S'assurer que c'est bien une notification pour l'utilisateur connecté et non lue
                if (notif.getIdDestinataire() == utilisateurConnecte.getIdUtilisateur() &&
                        "ETUDIANT".equalsIgnoreCase(notif.getTypeDestinataire()) && // Correction ici
                        "NON_LUE".equalsIgnoreCase(notif.getStatut())) {
                    notif.setStatut("LUE");
                    changed = true;
                }
            }

            if (changed) {
                // Écrire TOUTES les notifications (y compris celles non modifiées) dans le fichier CSV
                CRUDNotification.ecrireNotifications(CHEMIN_NOTIFICATIONS, toutesLesNotificationsDuFichier); // Correction du nom de la méthode
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Toutes les notifications ont été marquées comme lues.");
                chargerNotifications(); // Recharger pour mettre à jour l'affichage
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Aucune nouvelle notification à marquer comme lue.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de marquer les notifications comme lues: " + e.getMessage());
            System.err.println("Erreur lors du marquage des notifications comme lues: " + e.getMessage());
            e.printStackTrace();
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