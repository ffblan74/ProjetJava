package projet.controleurs.professeur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import projet.models.Cours;
import projet.models.Enseignant;
import projet.models.Etudiant;
import projet.models.Salle;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.UUID;

public class CreerCourController {

    @FXML private TextField matiereField;
    @FXML private ComboBox<String> jourComboBox;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;

    @FXML private ComboBox<String> groupeComboBox;
    @FXML private ComboBox<String> salleComboBox;

    // Optional fields, depending on FXML
    @FXML private TextField codeCoursField; // This should be auto-generated, not a UI field
    @FXML private TextArea descriptionArea;
    @FXML private TextField dureeField; // This should be calculated or derived from start/end time
    @FXML private TextField enseignantIdField; // This should be auto-assigned, not a UI field

    private Stage dialogStage;
    private Cours cours;

    private Supplier<List<Etudiant>> etudiantDataLoader;
    private Supplier<List<Salle>> salleDataLoader;
    private Supplier<Integer> prochainIdCoursSupplier;

    private List<Salle> loadedSalles;
    private Enseignant enseignantConnecte; // To store the connected teacher

    // Define the valid time range
    private final LocalTime MIN_HOUR = LocalTime.of(8, 0);
    private final LocalTime MAX_HOUR = LocalTime.of(19, 0);


    @FXML
    public void initialize() {
        jourComboBox.getItems().addAll("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

        // Set the connected teacher from the global static field
        Utilisateur utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        if (utilisateurConnecte instanceof Enseignant) {
            this.enseignantConnecte = (Enseignant) utilisateurConnecte;
        } else {
            NavigationUtil.afficherErreur("Erreur", "Utilisateur non enseignant", "L'utilisateur connecté n'est pas un enseignant.");
            // If the stage is already set, close it immediately
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCours(Cours cours) {
        this.cours = cours;

        // Load data for ComboBoxes first
        if (this.etudiantDataLoader != null) chargerGroupes();
        if (this.salleDataLoader != null) chargerSalles();

        if (cours != null) {
            matiereField.setText(cours.getMatiere());
            jourComboBox.getSelectionModel().select(cours.getJour());
            heureDebutField.setText(cours.getHeureDebut());
            heureFinField.setText(cours.getHeureFin());
            groupeComboBox.getSelectionModel().select(cours.getClasse());

            if (loadedSalles != null && cours.getSalleId() != 0) {
                loadedSalles.stream()
                        .filter(s -> s.getIdSalle() == cours.getSalleId())
                        .map(Salle::getNumeroSalle)
                        .findFirst()
                        .ifPresent(s -> salleComboBox.getSelectionModel().select(s));
            }

            // Populate optional fields (though they shouldn't be user-editable in the FXML)
            if (codeCoursField != null) codeCoursField.setText(cours.getCodeCours());
            if (descriptionArea != null) descriptionArea.setText(cours.getDescription());
            if (dureeField != null) dureeField.setText(formatDuration(cours.getDuree()));
            if (enseignantIdField != null) enseignantIdField.setText(String.valueOf(cours.getEnseignantId()));

        } else {
            clearFields();
        }
    }

    public Cours getCours() {
        return cours;
    }

    public void setEtudiantDataLoader(Supplier<List<Etudiant>> etudiantDataLoader) {
        this.etudiantDataLoader = etudiantDataLoader;
        chargerGroupes();
    }

    public void setSalleDataLoader(Supplier<List<Salle>> salleDataLoader) {
        this.salleDataLoader = salleDataLoader;
        chargerSalles();
    }

    public void setProchainIdCoursSupplier(Supplier<Integer> prochainIdCoursSupplier) {
        this.prochainIdCoursSupplier = prochainIdCoursSupplier;
    }


    private void chargerGroupes() {
        if (etudiantDataLoader != null) {
            try {
                List<Etudiant> etudiants = etudiantDataLoader.get();
                List<String> groupesUniques = etudiants.stream()
                        .map(Etudiant::getGroupe)
                        .distinct()
                        .filter(g -> g != null && !g.trim().isEmpty() && !"None".equalsIgnoreCase(g.trim()))
                        .sorted()
                        .collect(Collectors.toList());
                groupeComboBox.setItems(FXCollections.observableArrayList(groupesUniques));
            } catch (RuntimeException e) {
                NavigationUtil.afficherErreur("Erreur de chargement", "Impossible de charger les groupes.", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void chargerSalles() {
        if (salleDataLoader != null) {
            try {
                loadedSalles = salleDataLoader.get();
                List<String> numerosSalles = loadedSalles.stream()
                        .map(Salle::getNumeroSalle)
                        .sorted()
                        .collect(Collectors.toList());
                salleComboBox.setItems(FXCollections.observableArrayList(numerosSalles));
            } catch (RuntimeException e) {
                NavigationUtil.afficherErreur("Erreur de chargement", "Impossible de charger les salles.", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreerCours() {
        if (isInputValid()) {
            // Ensure enseignantConnecte is set. This should have happened in initialize.
            if (enseignantConnecte == null) {
                NavigationUtil.afficherErreur("Erreur", "Utilisateur non authentifié", "L'utilisateur connecté n'est pas un enseignant. Opération impossible.");
                return;
            }

            String selectedSalleNumero = salleComboBox.getSelectionModel().getSelectedItem();
            int salleId = -1;
            if (selectedSalleNumero != null && loadedSalles != null) {
                Optional<Salle> foundSalle = loadedSalles.stream()
                        .filter(s -> s.getNumeroSalle().equals(selectedSalleNumero))
                        .findFirst();
                if (foundSalle.isPresent()) {
                    salleId = foundSalle.get().getIdSalle();
                }
            }

            if (salleId == -1) {
                NavigationUtil.afficherErreur("Erreur de sélection", "Salle invalide", "La salle sélectionnée n'a pas pu être identifiée.");
                return;
            }

            Duration duree;
            LocalTime debut;
            LocalTime fin;
            try {
                debut = LocalTime.parse(heureDebutField.getText());
                fin = LocalTime.parse(heureFinField.getText());
                duree = Duration.between(debut, fin);
                // Additional check as per isInputValid
                if (duree.isNegative() || duree.isZero()) {
                    // This case should ideally be caught by isInputValid, but safety check here
                    NavigationUtil.afficherErreur("Erreur de durée", "La durée calculée est nulle ou négative.", "L'heure de fin doit être strictement après l'heure de début.");
                    return;
                }
            } catch (DateTimeParseException e) { // Catch specific parsing error
                NavigationUtil.afficherErreur("Erreur de temps", "Format de l'heure invalide.", "Veuillez vérifier les formats HH:mm pour les heures de début et de fin.");
                return;
            }


            if (cours == null) {
                int newIdCours;
                if (prochainIdCoursSupplier != null) {
                    try {
                        newIdCours = prochainIdCoursSupplier.get();
                    } catch (RuntimeException e) {
                        NavigationUtil.afficherErreur("Erreur", "Impossible de générer un ID de cours.", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                } else {
                    NavigationUtil.afficherErreur("Erreur", "Erreur interne", "La méthode de génération d'ID de cours n'est pas configurée.");
                    return;
                }

                String codeCours = generateGenericCodeCours(matiereField.getText()); // Use generic code generation
                // Assigning a dummy HoraireId for now, you might have a different logic
                int horaireId = generateHoraireId(); // You need to implement generateHoraireId()

                cours = new Cours(
                        newIdCours,
                        matiereField.getText(),
                        codeCours,
                        descriptionArea != null ? descriptionArea.getText() : "", // Use descriptionArea if it exists
                        duree,
                        enseignantConnecte.getIdEnseignant(),
                        salleId,
                        horaireId, // Assign generated HoraireId
                        jourComboBox.getSelectionModel().getSelectedItem(),
                        heureDebutField.getText(),
                        heureFinField.getText(),
                        groupeComboBox.getSelectionModel().getSelectedItem()
                        // new ArrayList<>() // Initialize with empty list of student IDs for new course - Removed because Cours constructor does not have this parameter
                );
            } else {
                // If modifying existing course
                cours.setMatiere(matiereField.getText());
                cours.setDuree(duree);
                cours.setJour(jourComboBox.getSelectionModel().getSelectedItem());
                cours.setHeureDebut(heureDebutField.getText());
                cours.setHeureFin(heureFinField.getText());
                cours.setClasse(groupeComboBox.getSelectionModel().getSelectedItem());
                cours.setSalleId(salleId);
                if (descriptionArea != null) cours.setDescription(descriptionArea.getText());
                // codeCours, enseignantId, horaireId should not be changed on modification via UI
            }

            NavigationUtil.afficherSucces("Succès", "Opération réussie", "Le cours a été créé/modifié avec succès !");
            dialogStage.close();
        }
    }

    @FXML
    private void handleRetour() {
        cours = null; // Indicate cancellation
        dialogStage.close();
    }

    private void clearFields() {
        matiereField.clear();
        jourComboBox.getSelectionModel().clearSelection();
        heureDebutField.clear();
        heureFinField.clear();
        groupeComboBox.getSelectionModel().clearSelection();
        salleComboBox.getSelectionModel().clearSelection();

        if (codeCoursField != null) codeCoursField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (dureeField != null) dureeField.clear();
        if (enseignantIdField != null) enseignantIdField.clear();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        // Validate Matiere (only check if not empty)
        if (matiereField.getText() == null || matiereField.getText().trim().isEmpty()) {
            errorMessage += "Veuillez entrer la matière !\n";
        }

        if (jourComboBox.getSelectionModel().getSelectedItem() == null || jourComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
            errorMessage += "Veuillez sélectionner un jour !\n";
        }
        if (groupeComboBox.getSelectionModel().getSelectedItem() == null || groupeComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
            errorMessage += "Veuillez sélectionner un groupe !\n";
        }
        if (salleComboBox.getSelectionModel().getSelectedItem() == null || salleComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Veuillez sélectionner une salle !\n";
        }

        LocalTime heureDebut = null;
        LocalTime heureFin = null;

        if (heureDebutField.getText() == null || heureDebutField.getText().isEmpty()) {
            errorMessage += "Veuillez entrer l'heure de début !\n";
        } else {
            try {
                heureDebut = LocalTime.parse(heureDebutField.getText());
                // Validate heureDebut is within the 08:00 - 19:00 range
                if (heureDebut.isBefore(MIN_HOUR) || heureDebut.isAfter(MAX_HOUR)) {
                    errorMessage += "L'heure de début doit être entre 08:00 et 19:00 !\n";
                }
            } catch (DateTimeParseException e) {
                errorMessage += "Format de l'heure de début invalide (HH:mm attendu)!\n";
            }
        }

        if (heureFinField.getText() == null || heureFinField.getText().isEmpty()) {
            errorMessage += "Veuillez entrer l'heure de fin !\n";
        } else {
            try {
                heureFin = LocalTime.parse(heureFinField.getText());
                // Validate heureFin is within the 08:00 - 19:00 range
                if (heureFin.isBefore(MIN_HOUR) || heureFin.isAfter(MAX_HOUR)) {
                    errorMessage += "L'heure de fin doit être entre 08:00 et 19:00 !\n";
                }
            } catch (DateTimeParseException e) {
                errorMessage += "Format de l'heure de fin invalide (HH:mm attendu)!\n";
            }
        }

        // Validate heureDebut is before heureFin (if both are valid and parsed)
        if (heureDebut != null && heureFin != null) {
            if (heureFin.isBefore(heureDebut) || heureFin.equals(heureDebut)) {
                errorMessage += "L'heure de fin doit être strictement après l'heure de début !\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            NavigationUtil.afficherErreur("Champs invalides", "Veuillez corriger les erreurs suivantes :", errorMessage);
            return false;
        }
    }


    private String formatDuration(Duration duration) {
        if (duration == null) return "";
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        if (hours > 0 && minutes == 0) {
            return hours + "h";
        } else if (hours == 0 && minutes > 0) {
            return minutes + "min";
        } else if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "min";
        }
        return "0h"; // Return "0h" or similar if duration is zero or negative after formatting
    }

    // --- Helper Methods for automatic codeCours and Horaire ID ---

    // Generic code generation (since we don't have Matiere object for prefix)
    private String generateGenericCodeCours(String matiereName) {
        // Take first few chars of matiere name and append a UUID segment
        String prefix = matiereName.length() > 3 ? matiereName.substring(0, 3).toUpperCase() : matiereName.toUpperCase();
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + uniqueId;
    }

    private int generateHoraireId() {
        return (int) (System.currentTimeMillis() % 100000) + 1; // Simple unique ID
    }
}