package projet.controleurs.professeur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import projet.models.Utilisateur;
import projet.controleurs.CRUDcsvControllerProf;
import projet.utils.Transmissible;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreerCoursProfController implements Transmissible {

    @FXML private ComboBox<String> matiereComboBox;
    @FXML private Label matiereLabel;
    @FXML private ComboBox<String> classeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private ComboBox<String> salleIdComboBox;
    @FXML private TextArea descriptionArea; // Ce sera la colonne "description" (sujet du cours)
    @FXML private Label errorMessageLabel;

    // Nous n'aurons PAS de @FXML private TextField codeMatiereField; si on ne veut pas le saisir

    private Utilisateur enseignantConnecte;

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private static final String CHEMIN_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final List<String> FIXED_SALLE_NUMEROS = Arrays.asList("A101", "32432", "3454", "4", "B201", "C302", "D401", "Amphi X");
    private final List<String> FIXED_CLASSES = Arrays.asList("A1", "A2", "A3", "P1", "P2");
    private final List<String> FIXED_MATIERES = Arrays.asList(
            "Mathématiques", "Physique", "SI", "Java", "Electronique", "Signal", "Anglais"
    );

    // Mettez à jour cet en-tête pour qu'il corresponde à la structure à 10 colonnes
    private final String[] ENTETE_COURS_CSV = {"idCours", "matiere", "codeMatiere", "description", "enseignantId", "salleId", "date", "heureDebut", "heureFin", "classe"};


    @Override
    public void transmettreDonnees(Object data) {
        System.out.println("DEBUG (CreerCoursProfController): transmettreDonnees() appelé.");
        if (data instanceof Utilisateur) {
            this.enseignantConnecte = (Utilisateur) data;
            System.out.println("DEBUG (CreerCoursProfController): Utilisateur enseignant connecté reçu: " + enseignantConnecte.getNom());
            initializeFields();
        } else {
            System.err.println("ERREUR (CreerCoursProfController): Données transmises via Transmissible ne sont pas de type Utilisateur.");
            initializeFields();
        }
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG (CreerCoursProfController): initialize() de FXML appelé.");
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
        System.out.println("DEBUG (CreerCoursProfController): initializeFields() - Début.");
        chargerMatieresEnseignees();
        chargerClasses();
        chargerSalles();
        System.out.println("DEBUG (CreerCoursProfController): initializeFields() - Fin.");
    }

    private void chargerMatieresEnseignees() {
        System.out.println("DEBUG (CreerCoursProfController): chargerMatieresEnseignees() - Début.");
        List<String> subjectsForThisProfessor = new ArrayList<>();

        if (enseignantConnecte != null) {
            try {
                List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_UTILISATEURS);
                if (!lignesUtilisateurs.isEmpty() && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].trim().equalsIgnoreCase("idUtilisateur")) {
                    lignesUtilisateurs.remove(0);
                }

                for (String[] ligne : lignesUtilisateurs) {
                    if (ligne.length > 7 && ligne[0] != null && !ligne[0].trim().isEmpty()) {
                        try {
                            int userId = Integer.parseInt(ligne[0].trim());
                            if (userId == enseignantConnecte.getIdUtilisateur()) {
                                String matiereString = ligne[7].trim();

                                if (!matiereString.isEmpty()) {
                                    if (matiereString.startsWith("[\"") && matiereString.endsWith("\"]")) {
                                        String content = matiereString.substring(2, matiereString.length() - 2);
                                        String[] matieresArray = content.split("\",\"");
                                        for (String matiere : matieresArray) {
                                            String cleanedMatiere = matiere.trim();
                                            if (!cleanedMatiere.isEmpty()) {
                                                subjectsForThisProfessor.add(cleanedMatiere);
                                            }
                                        }
                                    } else {
                                        String cleanedMatiere = matiereString.replace("\"", "").trim();
                                        if (!cleanedMatiere.isEmpty()) {
                                            subjectsForThisProfessor.add(cleanedMatiere);
                                        }
                                    }
                                }
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("ID utilisateur invalide dans le CSV: " + ligne[0]);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur lecture fichier utilisateurs (" + CHEMIN_UTILISATEURS + "): " + e.getMessage());
            }
        }

        subjectsForThisProfessor = subjectsForThisProfessor.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (!subjectsForThisProfessor.isEmpty()) {
            matiereComboBox.getItems().addAll(subjectsForThisProfessor);
            System.out.println("DEBUG (CreerCoursProfController): Matières chargées pour le professeur (" + (enseignantConnecte != null ? enseignantConnecte.getNom() : "N/A") + "): " + subjectsForThisProfessor);
            if (subjectsForThisProfessor.size() == 1) {
                matiereLabel.setText(subjectsForThisProfessor.get(0));
                matiereLabel.setVisible(true);
                matiereComboBox.setVisible(false);
                matiereComboBox.setDisable(true);
            } else {
                matiereComboBox.setVisible(true);
                matiereLabel.setVisible(false);
            }
            errorMessageLabel.setText("");
        } else {
            matiereComboBox.getItems().addAll(FIXED_MATIERES);
            System.out.println("DEBUG (CreerCoursProfController): Aucune matière spécifique trouvée. Chargement depuis FIXED_MATIERES: " + FIXED_MATIERES);
            matiereComboBox.setVisible(true);
            matiereLabel.setVisible(false);
            errorMessageLabel.setText("Aucune matière spécifique attribuée. Affichage des matières par défaut.");
        }
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans matiereComboBox: " + matiereComboBox.getItems().size());
        System.out.println("DEBUG (CreerCoursProfController): chargerMatieresEnseignees() - Fin.");
    }


    private void chargerClasses() {
        System.out.println("DEBUG (CreerCoursProfController): chargerClasses() - Début.");
        classeComboBox.getItems().addAll(FIXED_CLASSES);
        System.out.println("DEBUG (CreerCoursProfController): Classes chargées depuis FIXED_CLASSES: " + FIXED_CLASSES + " dans ComboBox.");
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans classeComboBox: " + classeComboBox.getItems().size());
        errorMessageLabel.setText("");
        System.out.println("DEBUG (CreerCoursProfController): chargerClasses() - Fin.");
    }

    private void chargerSalles() {
        System.out.println("DEBUG (CreerCoursProfController): chargerSalles() - Début.");
        salleIdComboBox.getItems().addAll(FIXED_SALLE_NUMEROS);
        System.out.println("DEBUG (CreerCoursProfController): Salles chargées depuis FIXED_SALLE_NUMEROS: " + FIXED_SALLE_NUMEROS + " dans ComboBox.");
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans salleIdComboBox: " + salleIdComboBox.getItems().size());
        errorMessageLabel.setText("");
        System.out.println("DEBUG (CreerCoursProfController): chargerSalles() - Fin.");
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
        System.out.println("DEBUG (CreerCoursProfController): Début enregistrerCours().");

        String matiere = matiereLabel.isVisible() ? matiereLabel.getText() : matiereComboBox.getValue();
        String classe = classeComboBox.getValue(); // La 10ème colonne
        LocalDate date = datePicker.getValue();
        String heureDebutStr = heureDebutField.getText();
        String heureFinStr = heureFinField.getText();
        String numeroSalleChoisi = salleIdComboBox.getValue();

        // ** Récupérer la description (sujet du cours) depuis le TextArea existant **
        // Ce champ est supposé être rempli par l'utilisateur pour le sujet du cours.
        String descriptionCours = descriptionArea.getText();
        if (descriptionCours == null || descriptionCours.trim().isEmpty()) {
            descriptionCours = ""; // Assurez-vous que c'est une chaîne vide si non rempli
        }

        // ** Générer le codeMatiere automatiquement **
        // Simple exemple: les 3-4 premières lettres de la matière en majuscules
        String codeMatiere = "";
        if (matiere != null && !matiere.isEmpty()) {
            codeMatiere = matiere.substring(0, Math.min(matiere.length(), 4)).toUpperCase();
            // Vous pouvez ajouter une logique plus complexe ici, par exemple un ID unique si nécessaire
            // ou une correspondance à partir d'une liste prédéfinie.
        }


        // --- Validation --- (inchangé pour les champs, mais validation pour matiere essentielle pour codeMatiere)
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
        if (numeroSalleChoisi == null || numeroSalleChoisi.trim().isEmpty()) {
            errorMessageLabel.setText("Veuillez sélectionner une salle."); return;
        }
        // Validation pour la description (sujet du cours) si elle est obligatoire
        // if (descriptionCours.isEmpty()) {
        //     errorMessageLabel.setText("Veuillez saisir une description ou un sujet pour le cours."); return;
        // }


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


        int idCours = generateUniqueCoursId();
        if (idCours == -1) {
            errorMessageLabel.setText("Erreur: Impossible de générer un ID de cours unique. Veuillez réessayer.");
            return;
        }

        int enseignantId = (enseignantConnecte != null) ? enseignantConnecte.getIdUtilisateur() : -1;

        // ** L'ordre et le nombre de champs sont CRUCIAUX et DOIVENT correspondre à ENTETE_COURS_CSV **
        // 1. idCours
        // 2. matiere
        // 3. codeMatiere (généré automatiquement)
        // 4. description (saisie par l'utilisateur dans descriptionArea)
        // 5. enseignantId
        // 6. salleId
        // 7. date
        // 8. heureDebut
        // 9. heureFin
        // 10. classe
        String[] nouveauCoursLigne = new String[]{
                String.valueOf(idCours),
                matiere,
                codeMatiere,        // <--- Généré automatiquement
                descriptionCours,   // <--- Vient de descriptionArea
                String.valueOf(enseignantId),
                numeroSalleChoisi,
                date.toString(),
                heureDebutStr,
                heureFinStr,
                classe
        };

        try {
            List<String[]> toutesLesLignesActuelles = CRUDcsvControllerProf.lire(CHEMIN_COURS);
            List<String[]> lignesAecrire = new ArrayList<>();

            boolean fichierExisteEtContientEntete = false;
            if (!toutesLesLignesActuelles.isEmpty() && toutesLesLignesActuelles.get(0).length == ENTETE_COURS_CSV.length) {
                boolean enteteCorrespond = true;
                for (int i = 0; i < ENTETE_COURS_CSV.length; i++) {
                    if (!toutesLesLignesActuelles.get(0)[i].trim().equalsIgnoreCase(ENTETE_COURS_CSV[i].trim())) {
                        enteteCorrespond = false;
                        break;
                    }
                }
                if (enteteCorrespond) {
                    fichierExisteEtContientEntete = true;
                }
            }

            if (!fichierExisteEtContientEntete) {
                lignesAecrire.add(ENTETE_COURS_CSV);
                System.out.println("DEBUG (CreerCoursProfController): Ajout de l'en-tête standard au fichier CSV.");
            } else {
                lignesAecrire.add(toutesLesLignesActuelles.get(0)); // Garder l'en-tête existant
                for (int i = 1; i < toutesLesLignesActuelles.size(); i++) {
                    lignesAecrire.add(toutesLesLignesActuelles.get(i));
                }
            }

            lignesAecrire.add(nouveauCoursLigne); // Ajoutez le nouveau cours

            CRUDcsvControllerProf.ecrire(CHEMIN_COURS, lignesAecrire);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours enregistré avec succès!");
            System.out.println("DEBUG (CreerCoursProfController): Cours enregistré. Fermeture de la fenêtre.");
            closeCurrentWindow();
        } catch (IOException e) {
            errorMessageLabel.setText("Erreur lors de l'enregistrement du cours: " + e.getMessage());
            System.err.println("ERREUR (CreerCoursProfController): Enregistrement cours: ");
            e.printStackTrace();
        }
    }

    private int generateUniqueCoursId() {
        try {
            List<String[]> lignes = CRUDcsvControllerProf.lire(CHEMIN_COURS);
            int maxId = 0;

            // Ignorer l'en-tête si présent lors de la recherche du maxId
            int startIndex = 0;
            if (!lignes.isEmpty() && lignes.get(0).length > 0 && "idCours".equalsIgnoreCase(lignes.get(0)[0].trim())) {
                startIndex = 1;
            }

            for (int i = startIndex; i < lignes.size(); i++) {
                String[] ligne = lignes.get(i);
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
        System.out.println("DEBUG (CreerCoursProfController): Annulation. Fermeture de la fenêtre.");
        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        if (matiereComboBox != null && matiereComboBox.getScene() != null && matiereComboBox.getScene().getWindow() != null) {
            ((Stage) matiereComboBox.getScene().getWindow()).close();
        } else {
            System.err.println("ERREUR (CreerCoursProfController): Impossible de trouver la Stage à fermer pour annuler/enregistrer.");
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