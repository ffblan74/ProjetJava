package projet.controleurs.professeur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import projet.models.Utilisateur;
import projet.controleurs.CRUDcsvController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreerCoursProfController {

    @FXML private ComboBox<String> matiereComboBox;
    @FXML private Label matiereLabel;
    @FXML private ComboBox<String> classeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private ComboBox<String> salleIdComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorMessageLabel;

    private Utilisateur enseignantConnecte;
    private Stage currentStage;

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private static final String CHEMIN_SALLES = "src/main/resources/projet/csv/salles.csv";
    private static final String CHEMIN_CLASSES = "src/main/resources/projet/csv/classes.csv";
    private static final String CHEMIN_UTILISATEURS = "src/main/resources/projet/csv/utilisateur.csv"; // Path to utilisateur CSV

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Hardcoded Fallback lists (used if CSVs are empty or not found)
    private final List<String> FALLBACK_CLASSES = Arrays.asList(
            "CP", "CE1", "CE2", "CM1", "CM2",
            "6ème", "5ème", "4ème", "3ème",
            "Seconde", "Première", "Terminale"
    );
    private final List<String> FALLBACK_SALLES = Arrays.asList("101", "102", "201", "Amphi A");
    private final List<String> FALLBACK_MATIERES = Arrays.asList(
            "Mathématiques", "Français", "Histoire-Géographie", "Sciences",
            "Anglais", "Espagnol", "Allemand", "Physique-Chimie", "SVT",
            "Éducation Physique et Sportive", "Arts Plastiques", "Musique"
    );


    public void setData(Stage stage, Utilisateur enseignant) {
        this.currentStage = stage;
        this.enseignantConnecte = enseignant;
        initializeFields();
    }

    @FXML
    public void initialize() {
        heureDebutField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateTimeField(heureDebutField);
            }
        });
        heureFinField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateTimeField(heureFinField);
            }
        });
    }

    private void initializeFields() {
        // 1. Populate Matières (Subjects) based on the connected professor
        chargerMatieresEnseignees();

        // 2. Populate Classes
        chargerClasses();

        // 3. Populate Salles (Rooms)
        chargerSalles();
    }

    private void chargerMatieresEnseignees() {
        List<String> subjectsForThisProfessor = new ArrayList<>();
        if (enseignantConnecte != null) {
            try {
                List<String[]> lignesUtilisateurs = CRUDcsvController.lire(CHEMIN_UTILISATEURS);
                // Skip header if present
                if (!lignesUtilisateurs.isEmpty() && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].trim().equalsIgnoreCase("idUtilisateur")) {
                    lignesUtilisateurs.remove(0);
                }

                for (String[] ligne : lignesUtilisateurs) {
                    // Check if line is valid and has enough columns for matiereEnseignee (index 7)
                    if (ligne.length > 7 && ligne[0] != null && !ligne[0].trim().isEmpty()) {
                        try {
                            int userId = Integer.parseInt(ligne[0].trim());
                            if (userId == enseignantConnecte.getIdUtilisateur()) {
                                String matiereString = ligne[7].trim(); // Get the matiereEnseignee column

                                // --- Manual parsing for ["item1","item2"] format ---
                                if (!matiereString.isEmpty()) {
                                    // Remove leading/trailing brackets and split by ","
                                    if (matiereString.startsWith("[") && matiereString.endsWith("]")) {
                                        String content = matiereString.substring(1, matiereString.length() - 1);
                                        String[] matieresArray = content.split("\",\""); // Split by ","
                                        for (String matiere : matieresArray) {
                                            // Remove any remaining quotes
                                            String cleanedMatiere = matiere.replace("\"", "").trim();
                                            if (!cleanedMatiere.isEmpty()) {
                                                subjectsForThisProfessor.add(cleanedMatiere);
                                            }
                                        }
                                    } else {
                                        // Handle cases where it might be a single string without brackets
                                        String cleanedMatiere = matiereString.replace("\"", "").trim();
                                        if (!cleanedMatiere.isEmpty()) {
                                            subjectsForThisProfessor.add(cleanedMatiere);
                                        }
                                    }
                                }
                                break; // Found the teacher, no need to continue
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("ID utilisateur invalide dans le CSV: " + ligne[0]);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur lecture fichier utilisateurs (" + CHEMIN_UTILISATEURS + "): " + e.getMessage() + ". Utilisation des matières par défaut.");
            }
        }

        // Filter out any blank or null subjects (redundant after manual parsing but good safety)
        subjectsForThisProfessor = subjectsForThisProfessor.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());

        if (subjectsForThisProfessor.isEmpty()) {
            // Fallback if no specific subjects found or CSV error
            matiereComboBox.getItems().addAll(FALLBACK_MATIERES);
            matiereComboBox.setVisible(true); // Ensure ComboBox is visible for fallback
            matiereLabel.setVisible(false);
            errorMessageLabel.setText("Aucune matière attribuée à ce professeur, ou données invalides. Affichage des matières par défaut.");
        } else if (subjectsForThisProfessor.size() == 1) {
            matiereLabel.setText(subjectsForThisProfessor.get(0));
            matiereLabel.setVisible(true);
            matiereComboBox.setVisible(false);
            matiereComboBox.setDisable(true);
            errorMessageLabel.setText(""); // Clear error if successful
        } else {
            matiereComboBox.getItems().addAll(subjectsForThisProfessor);
            matiereComboBox.setVisible(true);
            matiereLabel.setVisible(false);
            errorMessageLabel.setText(""); // Clear error if successful
        }
    }


    private void chargerClasses() {
        List<String> classes = new ArrayList<>();
        try {
            List<String[]> lignes = CRUDcsvController.lire(CHEMIN_CLASSES);
            // Assuming classes.csv has a header "nomClasse" or is just a list of class names
            if (!lignes.isEmpty() && lignes.get(0).length > 0 && lignes.get(0)[0].trim().equalsIgnoreCase("nomClasse")) {
                lignes.remove(0); // Remove header
            }
            for (String[] ligne : lignes) {
                if (ligne.length > 0 && ligne[0] != null) {
                    String className = ligne[0].trim();
                    if (!className.isEmpty()) {
                        classes.add(className);
                    }
                }
            }
            if (!classes.isEmpty()) {
                classeComboBox.getItems().addAll(classes);
            } else {
                classeComboBox.getItems().addAll(FALLBACK_CLASSES);
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier classes (" + CHEMIN_CLASSES + "): " + e.getMessage() + ". Utilisation des classes par défaut.");
            classeComboBox.getItems().addAll(FALLBACK_CLASSES);
        }
    }

    private void chargerSalles() {
        List<String> salles = new ArrayList<>();
        try {
            List<String[]> lignes = CRUDcsvController.lire(CHEMIN_SALLES);
            // Assuming salles.csv has a header "idSalle"
            if (!lignes.isEmpty() && lignes.get(0).length > 0 && lignes.get(0)[0].trim().equalsIgnoreCase("idSalle")) {
                lignes.remove(0); // Remove header
            }
            for (String[] ligne : lignes) {
                // Check if line has enough columns for idSalle (index 0)
                if (ligne.length > 0 && ligne[0] != null) {
                    String salleId = ligne[0].trim(); // Get the idSalle (first column)
                    if (!salleId.isEmpty()) {
                        salles.add(salleId);
                    }
                }
            }
            if (!salles.isEmpty()) {
                salleIdComboBox.getItems().addAll(salles);
            } else {
                salleIdComboBox.getItems().addAll(FALLBACK_SALLES);
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier salles (" + CHEMIN_SALLES + "): " + e.getMessage() + ". Utilisation des salles par défaut.");
            salleIdComboBox.getItems().addAll(FALLBACK_SALLES);
        }
    }

    private boolean validateTimeField(TextField field) {
        String timeText = field.getText();
        if (timeText.isEmpty()) {
            return true;
        }
        try {
            LocalTime.parse(timeText, timeFormatter);
            field.setStyle("");
            return true;
        } catch (DateTimeParseException e) {
            field.setStyle("-fx-border-color: red;");
            return false;
        }
    }

    @FXML
    private void enregistrerCours() {
        errorMessageLabel.setText("");

        String matiere = matiereLabel.isVisible() ? matiereLabel.getText() : matiereComboBox.getValue();
        String classe = classeComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String heureDebutStr = heureDebutField.getText();
        String heureFinStr = heureFinField.getText();
        String salleIdStr = salleIdComboBox.getValue();
        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            description = "";
        }

        // --- Validation ---
        if (matiere == null || matiere.trim().isEmpty()) {
            errorMessageLabel.setText("Veuillez sélectionner une matière."); return;
        }
        if (classe == null || classe.trim().isEmpty()) {
            errorMessageLabel.setText("Veuillez sélectionner une classe."); return;
        }
        if (date == null) {
            errorMessageLabel.setText("Veuillez sélectionner une date."); return;
        }
        if (heureDebutStr.isEmpty() || heureFinStr.isEmpty()) {
            errorMessageLabel.setText("Les heures de début et de fin sont obligatoires."); return;
        }
        if (!validateTimeField(heureDebutField) || !validateTimeField(heureFinField)) {
            errorMessageLabel.setText("Les formats d'heure doivent être HH:mm."); return;
        }
        if (salleIdStr == null || salleIdStr.trim().isEmpty()) {
            errorMessageLabel.setText("Veuillez sélectionner une salle."); return;
        }

        LocalTime heureDebut;
        LocalTime heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutStr, timeFormatter);
            heureFin = LocalTime.parse(heureFinStr, timeFormatter);
            if (heureFin.isBefore(heureDebut) || heureFin.equals(heureDebut)) {
                errorMessageLabel.setText("L'heure de fin doit être après l'heure de début."); return;
            }
        } catch (DateTimeParseException e) {
            errorMessageLabel.setText("Format d'heure invalide (attendu HH:mm)."); return;
        }

        int salleId;
        try {
            salleId = Integer.parseInt(salleIdStr);
        } catch (NumberFormatException e) {
            errorMessageLabel.setText("ID de salle invalide. Veuillez sélectionner une salle valide (numérique).");
            return;
        }

        int idCours = generateUniqueCoursId();
        if (idCours == -1) {
            errorMessageLabel.setText("Erreur: Impossible de générer un ID de cours unique. Veuillez réessayer.");
            return;
        }

        int enseignantId = enseignantConnecte.getIdUtilisateur();

        String[] nouveauCoursLigne = new String[]{
                String.valueOf(idCours),
                matiere,
                date.toString(),
                heureDebutStr,
                heureFinStr,
                classe,
                String.valueOf(salleId),
                String.valueOf(enseignantId),
                description
        };

        try {
            List<String[]> toutesLesLignes = CRUDcsvController.lire(CHEMIN_COURS);
            boolean fileHadHeader = false;

            if (!toutesLesLignes.isEmpty() && toutesLesLignes.get(0).length > 0 && "idCours".equalsIgnoreCase(toutesLesLignes.get(0)[0].trim())) {
                fileHadHeader = true;
            }

            List<String[]> lignesPourEcriture = new ArrayList<>();
            if (!fileHadHeader) {
                lignesPourEcriture.add(new String[]{"idCours", "matiere", "date", "heureDebut", "heureFin", "classe", "salleId", "enseignantId", "description"});
            }

            for(int i = (fileHadHeader ? 1 : 0); i < toutesLesLignes.size(); i++) {
                lignesPourEcriture.add(toutesLesLignes.get(i));
            }

            lignesPourEcriture.add(nouveauCoursLigne);

            CRUDcsvController.ecrire(CHEMIN_COURS, lignesPourEcriture);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours enregistré avec succès!");
            annuler();
        } catch (IOException e) {
            errorMessageLabel.setText("Erreur lors de l'enregistrement du cours: " + e.getMessage());
            System.err.println("Erreur enregistrement cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int generateUniqueCoursId() {
        try {
            List<String[]> lignes = CRUDcsvController.lire(CHEMIN_COURS);
            int maxId = 0;

            if (!lignes.isEmpty() && lignes.get(0).length > 0 && "idCours".equalsIgnoreCase(lignes.get(0)[0].trim())) {
                lignes.remove(0);
            }

            for (String[] ligne : lignes) {
                if (ligne.length > 0 && ligne[0] != null) {
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("ID de cours invalide trouvé dans le CSV: '" + ligne[0] + "' - " + e.getMessage());
                    }
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier des cours pour générer un ID. Retourne 1 si le fichier n'existe pas encore: " + e.getMessage());
            return 1;
        }
    }

    @FXML
    private void annuler() {
        if (currentStage != null) {
            currentStage.close();
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