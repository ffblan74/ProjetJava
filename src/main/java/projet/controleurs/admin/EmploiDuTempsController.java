package projet.controleurs.admin;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.control.ButtonType;
// import javafx.scene.control.ContextMenu; // Supprimez cet import si vous n'utilisez plus ContextMenu
// import javafx.scene.control.MenuItem;     // Supprimez cet import si vous n'utilisez plus MenuItem
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup; // NOUVEL IMPORT

import projet.models.Cours;
import projet.models.Enseignant;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import projet.controleurs.CRUDcsvControllerProf;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

public class EmploiDuTempsController implements Transmissible {

    @FXML private Label currentWeekLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button supprimerCoursButton;
    @FXML private Button modifierCoursButton;

    // Suppression de ContextMenu, MenuItem n'est plus FXML
    // @FXML private ContextMenu coursContextMenu;

    // Nouveaux FXML pour le contenu du Popup
    @FXML private VBox popupContent;
    @FXML private Button popupModifierButton;
    @FXML private Button popupSupprimerButton;

    private Popup coursPopup; // Le Popup lui-même

    private LocalDate currentDate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private List<Cours> tousLesCours;
    private Map<Integer, Enseignant> enseignantsMap;

    private Utilisateur utilisateurConnecte;
    private Cours selectedCours;
    private Cours rightClickedCours; // Toujours utilisé pour le clic droit

    private static final String CHEMIN_FICHIER_COURS = "src/main/resources/projet/csv/cours.csv";
    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";

    private Set<Cours> coursEnConflitMajeur;
    private Set<Cours> coursEnConflitMineur;


    @FXML
    public void initialize() {
        currentDate = LocalDate.now().with(DayOfWeek.MONDAY);
        loadEnseignants();
        updateCalendar();

        if (supprimerCoursButton != null) {
            supprimerCoursButton.setDisable(true);
        }
        if (modifierCoursButton != null) {
            modifierCoursButton.setDisable(true);
        }

        // Initialiser le Popup après l'injection FXML
        initializeCoursPopup();
    }

    private void initializeCoursPopup() {
        coursPopup = new Popup();
        coursPopup.getContent().add(popupContent); // Ajoutez le VBox que vous avez défini en FXML

        // Optionnel: Fermer le popup lorsque l'on clique en dehors
        coursPopup.setAutoHide(true);
        // Optionnel: Permettre le focus sur le popup (pour les boutons)
        coursPopup.setHideOnEscape(true); // Fermer avec Échap
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
        }
    }

    @FXML
    private void handlePreviousWeek(ActionEvent event) {
        currentDate = currentDate.minusWeeks(1);
        updateCalendar();
    }

    @FXML
    private void handleNextWeek(ActionEvent event) {
        currentDate = currentDate.plusWeeks(1);
        updateCalendar();
    }

    @FXML
    private void handleToday(ActionEvent event) {
        currentDate = LocalDate.now().with(DayOfWeek.MONDAY);
        updateCalendar();
    }

    private void updateCalendar() {
        if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            currentDate = currentDate.plusDays(2);
        } else if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            currentDate = currentDate.plusDays(1);
        }

        currentWeekLabel.setText("Semaine du " + currentDate.format(DATE_FORMATTER) + " au " + currentDate.plusDays(4).format(DATE_FORMATTER));

        calendarGrid.getChildren().clear();
        addDayHeaders();

        loadAllCours();
        detectConflicts();

        populateCalendarGrid();
        clearSelection();
    }

    private void addDayHeaders() {
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
        for (int i = 0; i < joursSemaine.length; i++) {
            Label dayLabel = new Label(joursSemaine[i] + "\n" + currentDate.plusDays(i).format(DATE_FORMATTER));
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px; -fx-background-color: #E0E0E0;");
            calendarGrid.add(dayLabel, i, 0);
        }
    }

    private void loadAllCours() {
        tousLesCours = new ArrayList<>();
        try {
            List<String[]> lignesCours = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
            int startIndex = 0;
            if (!lignesCours.isEmpty() && lignesCours.get(0).length > 0 && "idCours".equalsIgnoreCase(lignesCours.get(0)[0].trim())) {
                startIndex = 1;
            }

            for (int i = startIndex; i < lignesCours.size(); i++) {
                String[] data = lignesCours.get(i);
                try {
                    Cours cours = Cours.fromCsv(data);
                    Enseignant enseignant = enseignantsMap.get(cours.getEnseignantId());
                    if (enseignant != null) {
                        cours.setEnseignantNomComplet(enseignant.getPrenom() + " " + enseignant.getNom());
                    } else {
                        cours.setEnseignantNomComplet("Enseignant inconnu (ID: " + cours.getEnseignantId() + ")");
                    }
                    tousLesCours.add(cours);
                } catch (IllegalArgumentException e) {
                    System.err.println("Erreur de parsing d'une ligne de cours (ignoring): " + Arrays.toString(data) + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement des cours depuis le CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEnseignants() {
        enseignantsMap = new HashMap<>();
        try {
            List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_UTILISATEURS);
            int startIndex = 0;
            if (!lignesUtilisateurs.isEmpty() && lignesUtilisateurs.get(0).length > 0 && "idUtilisateur".equalsIgnoreCase(lignesUtilisateurs.get(0)[0].trim())) {
                startIndex = 1;
            }

            for (int i = startIndex; i < lignesUtilisateurs.size(); i++) {
                String[] data = lignesUtilisateurs.get(i);
                if (data.length >= 6 && data[5].trim().equalsIgnoreCase("ENSEIGNANT")) {
                    try {
                        Enseignant enseignant = Enseignant.fromCsv(data);
                        enseignantsMap.put(enseignant.getIdUtilisateur(), enseignant);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement d'un enseignant depuis CSV: " + Arrays.toString(data) + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement des utilisateurs/enseignants depuis le CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void detectConflicts() {
        coursEnConflitMajeur = new HashSet<>();
        coursEnConflitMineur = new HashSet<>();

        Map<LocalDate, List<Cours>> coursParJour = new HashMap<>();
        for (Cours cours : tousLesCours) {
            if (cours.getDate().isAfter(currentDate.minusDays(1)) && cours.getDate().isBefore(currentDate.plusDays(5))) {
                coursParJour.computeIfAbsent(cours.getDate(), k -> new ArrayList<>()).add(cours);
            }
        }

        for (List<Cours> coursDuJour : coursParJour.values()) {
            coursDuJour.sort(Comparator.comparing(Cours::getHeureDebut));

            for (int i = 0; i < coursDuJour.size(); i++) {
                Cours coursA = coursDuJour.get(i);
                int conflitsCompteur = 0;

                for (int j = i + 1; j < coursDuJour.size(); j++) {
                    Cours coursB = coursDuJour.get(j);

                    if (coursA.getHeureDebut().isBefore(coursB.getHeureFin()) &&
                            coursB.getHeureDebut().isBefore(coursA.getHeureFin())) {

                        boolean isConflit = false;
                        if (coursA.getSalle().equals(coursB.getSalle())) {
                            System.out.println("Conflit de salle détecté entre Cours " + coursA.getIdCours() + " et Cours " + coursB.getIdCours() + " dans la salle " + coursA.getSalle() + " le " + coursA.getDate());
                            isConflit = true;
                        }
                        if (coursA.getEnseignantId() == coursB.getEnseignantId()) {
                            System.out.println("Conflit d'enseignant détecté entre Cours " + coursA.getIdCours() + " et Cours " + coursB.getIdCours() + " pour l'enseignant " + coursA.getEnseignantNomComplet() + " le " + coursA.getDate());
                            isConflit = true;
                        }

                        if (isConflit) {
                            conflitsCompteur++;
                            coursEnConflitMineur.add(coursA);
                            coursEnConflitMineur.add(coursB);
                        }
                    }
                }

                if (conflitsCompteur > 1) {
                    coursEnConflitMajeur.add(coursA);
                    coursEnConflitMineur.remove(coursA);
                }
            }
        }
    }

    private void populateCalendarGrid() {
        for (int i = 0; i < 5; i++) {
            LocalDate day = currentDate.plusDays(i);
            List<Cours> coursDuJour = new ArrayList<>();

            for (Cours cours : tousLesCours) {
                if (cours.getDate().isEqual(day)) {
                    coursDuJour.add(cours);
                }
            }

            coursDuJour.sort(Comparator.comparing(Cours::getHeureDebut));

            VBox dayContent = new VBox(2);
            dayContent.setStyle("-fx-padding: 2px;");
            dayContent.setAlignment(Pos.TOP_LEFT);

            for (Cours cours : coursDuJour) {
                Label coursLabel = createCoursLabel(cours);
                dayContent.getChildren().add(coursLabel);
            }

            calendarGrid.add(dayContent, i, 1);
            GridPane.setVgrow(dayContent, Priority.ALWAYS);
        }
    }

    private Label createCoursLabel(Cours cours) {
        String heureDebutFormatted = cours.getHeureDebut().format(timeFormatter);
        String heureFinFormatted = cours.getHeureFin().format(timeFormatter);

        Label coursLabel = new Label(
                cours.getCodeCours() + "\n" +
                        cours.getMatiere() + "\n" +
                        "Salle: " + cours.getSalle() + "\n" +
                        "Prof: " + cours.getEnseignantNomComplet() + "\n" +
                        "Heure: " + heureDebutFormatted + " - " + heureFinFormatted + "\n" +
                        "Classe: " + cours.getClasse()
        );
        coursLabel.setMaxWidth(Double.MAX_VALUE);
        coursLabel.setWrapText(true);

        if (cours.equals(selectedCours)) {
            coursLabel.setStyle("-fx-background-color: #006400; -fx-border-color: #004d00; -fx-text-fill: white; -fx-border-width: 2; -fx-padding: 5px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 10px;");
            Tooltip.install(coursLabel, new Tooltip("Ce cours est sélectionné."));
        } else if (coursEnConflitMajeur.contains(cours)) {
            coursLabel.setStyle("-fx-background-color: #ff6666; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-padding: 5px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 10px;");
            Tooltip.install(coursLabel, new Tooltip("Conflit majeur : Salle ou enseignant utilisé par plusieurs autres cours en même temps."));
        } else if (coursEnConflitMineur.contains(cours)) {
            coursLabel.setStyle("-fx-background-color: #ffcc99; -fx-border-color: #ff9933; -fx-border-width: 2; -fx-padding: 5px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 10px;");
            Tooltip.install(coursLabel, new Tooltip("Conflit mineur : Salle ou enseignant utilisé par un autre cours en même temps."));
        } else {
            coursLabel.setStyle("-fx-background-color: #e6ffe6; -fx-border-color: #a3d9a3; -fx-border-width: 1; -fx-padding: 5px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 10px;");
            Tooltip.install(coursLabel, new Tooltip(cours.getDescription()));
        }

        coursLabel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                selectCours(cours);
                // Si le popup est ouvert suite à un clic droit précédent, le fermer
                if (coursPopup != null && coursPopup.isShowing()) {
                    coursPopup.hide();
                }
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                handleModifierCoursAction(cours);
                if (coursPopup != null && coursPopup.isShowing()) {
                    coursPopup.hide();
                }
            } else if (event.getButton() == MouseButton.SECONDARY) { // Clic droit
                System.out.println("DEBUG: Clic droit détecté sur le cours (Popup) : " + cours.getMatiere());
                rightClickedCours = cours; // Stocke le cours sur lequel on a fait un clic droit

                if (coursPopup != null) {
                    // Positionner le popup au niveau du clic de la souris
                    coursPopup.show(coursLabel, event.getScreenX(), event.getScreenY());
                } else {
                    System.err.println("ERREUR: coursPopup est null. Problème d'initialisation.");
                }
                event.consume();
            }
        });

        // Supprimez setOnContextMenuRequested entièrement ou commentez-le.
        // coursLabel.setOnContextMenuRequested(...)

        return coursLabel;
    }

    private void selectCours(Cours cours) {
        this.selectedCours = cours;
        updateCalendar();
    }

    private void clearSelection() {
        this.selectedCours = null;
        this.rightClickedCours = null;
        if (modifierCoursButton != null) {
            modifierCoursButton.setDisable(true);
        }
        if (supprimerCoursButton != null) {
            supprimerCoursButton.setDisable(true);
        }
        // Assurez-vous que le popup est caché quand la sélection est claire
        if (coursPopup != null && coursPopup.isShowing()) {
            coursPopup.hide();
        }
    }


    @FXML
    private void handleAjouterCours(ActionEvent event) {
        System.out.println("Ouvrir la fenêtre d'ajout de cours pour l'Admin.");
        Stage stageActuel = (Stage) ((Node) event.getSource()).getScene().getWindow();

        NavigationUtil.ouvrirFenetreModaleAvecRetour(
                "/projet/fxml/creer_cours_admin.fxml",
                "Créer un nouveau cours",
                stageActuel,
                null,
                data -> {
                    if (data != null && data instanceof String && ((String) data).equals("refresh")) {
                        System.out.println("Fenêtre d'ajout de cours fermée. Rechargement des cours.");
                        updateCalendar();
                    }
                }
        );
    }

    @FXML
    private void handleModifierCours(ActionEvent event) {
        if (selectedCours != null) {
            handleModifierCoursAction(selectedCours);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Modification de Cours");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez d'abord sélectionner un cours en cliquant dessus, puis cliquez sur 'Modifier' ou faites un clic droit sur un cours.");
            alert.showAndWait();
        }
    }

    // Nouveau: Gérer l'action du bouton "Modifier" du Popup
    @FXML
    private void handlePopupModifier(ActionEvent event) {
        if (rightClickedCours != null) {
            handleModifierCoursAction(rightClickedCours);
            if (coursPopup != null) {
                coursPopup.hide(); // Cacher le popup après l'action
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
            alert.setHeaderText(null);
            alert.setContentText("Aucun cours sélectionné pour la modification.");
            alert.showAndWait();
        }
    }

    private void handleModifierCoursAction(Cours coursAModifier) {
        System.out.println("Modification du cours: " + coursAModifier.getCodeCours());
        Stage stageActuel = (Stage) calendarGrid.getScene().getWindow();

        NavigationUtil.ouvrirFenetreModaleAvecRetour(
                "/projet/fxml/creer_cours_admin.fxml",
                "Modifier le cours",
                stageActuel,
                coursAModifier,
                data -> {
                    if (data != null && data instanceof String && ((String) data).equals("refresh")) {
                        System.out.println("Fenêtre de modification de cours fermée. Rechargement des cours.");
                        updateCalendar();
                    }
                }
        );
    }

    @FXML
    private void handleSupprimerCours(ActionEvent event) {
        if (selectedCours == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Supprimer un Cours");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner le cours à supprimer en cliquant dessus ou faites un clic droit sur un cours.");
            alert.showAndWait();
            return;
        }
        confirmAndDeleteCours(selectedCours);
    }

    // Nouveau: Gérer l'action du bouton "Supprimer" du Popup
    @FXML
    private void handlePopupSupprimer(ActionEvent event) {
        if (rightClickedCours != null) {
            confirmAndDeleteCours(rightClickedCours);
            if (coursPopup != null) {
                coursPopup.hide(); // Cacher le popup après l'action
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Aucun cours sélectionné pour la suppression.");
            alert.showAndWait();
        }
    }

    private void confirmAndDeleteCours(Cours coursToDelete) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le cours : " + coursToDelete.getCodeCours() + " (" + coursToDelete.getMatiere() + ") ?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce cours ? Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                List<String[]> allLines = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
                List<String[]> updatedLines = new ArrayList<>();
                boolean headerSkipped = false;

                for (String[] line : allLines) {
                    if (!headerSkipped && line.length > 0 && "idCours".equalsIgnoreCase(line[0].trim())) {
                        updatedLines.add(line);
                        headerSkipped = true;
                        continue;
                    }
                    if (line.length > 0 && Integer.parseInt(line[0].trim()) == coursToDelete.getIdCours()) {
                        System.out.println("Cours supprimé : " + coursToDelete.getCodeCours());
                    } else {
                        updatedLines.add(line);
                    }
                }

                CRUDcsvControllerProf.ecrire(CHEMIN_FICHIER_COURS, updatedLines);
                System.out.println("Cours avec ID " + coursToDelete.getIdCours() + " supprimé du CSV.");

                updateCalendar();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Suppression réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Le cours a été supprimé avec succès.");
                successAlert.showAndWait();

            } catch (IOException | NumberFormatException e) {
                System.err.println("Erreur lors de la suppression du cours: " + e.getMessage());
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de suppression");
                errorAlert.setHeaderText("Impossible de supprimer le cours.");
                errorAlert.setContentText("Une erreur est survenue lors de la suppression du cours. Veuillez vérifier les logs.");
                errorAlert.showAndWait();
            }
        }
    }
}