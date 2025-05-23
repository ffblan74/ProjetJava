package projet.controleurs.professeur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import projet.models.Utilisateur;
import projet.models.Notification;
import projet.models.Cours; // Importez la classe Cours
import projet.controleurs.CRUDcsvControllerProf;
import projet.controleurs.CRUDNotification;
import projet.utils.Transmissible;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class CreerCoursProfController implements Transmissible, Initializable {

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

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";
    private static final String CHEMIN_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String CHEMIN_NOTIFICATIONS = "src/main/resources/projet/csv/notifications.csv";
    private static final String CHEMIN_SALLE = "src/main/resources/projet/csv/salle.csv";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // L'en-tête du CSV, l'idCours est la première colonne et sera généré
    // Cette constante n'est utilisée que pour initialiser le fichier si vide/incorrect.
    // L'écriture réelle du cours utilise toCSVArray() de l'objet Cours.
    private final String[] ENTETE_COURS_CSV = {"idCours", "matiere", "codeMatiere", "description", "enseignantId", "salle", "date", "heureDebut", "heureFin", "classe"};


    @Override
    public void transmettreDonnees(Object data) {
        System.out.println("DEBUG (CreerCoursProfController): transmettreDonnees() appelé.");
        if (data instanceof Utilisateur) {
            this.enseignantConnecte = (Utilisateur) data;
            System.out.println("DEBUG (CreerCoursProfController): Utilisateur enseignant connecté reçu: " + enseignantConnecte.getNom());
            // Appel initial des champs déplacé ici pour s'assurer que enseignantConnecte est défini
            initializeFields();
        } else {
            System.err.println("ERREUR (CreerCoursProfController): Données transmises via Transmissible ne sont pas de type Utilisateur.");
            // Si pas d'utilisateur, initialisation des champs sans données spécifiques à l'enseignant
            initializeFields();
        }
    }

    @Override // Implémentation de la méthode initialize d'Initializable
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("DEBUG (CreerCoursProfController): initialize() de FXML appelé.");
        // Le code d'initialisation des listeners reste ici
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
        // Ces méthodes vont maintenant charger depuis les CSV
        chargerMatieresEnseignees(); // Cette méthode a besoin de l'enseignantConnecte
        chargerClasses();
        chargerSalles();
        System.out.println("DEBUG (CreerCoursProfController): initializeFields() - Fin.");
    }

    // MODIFIÉE: Cette méthode reste basée sur les matières attribuées au professeur dans utilisateurs.csv
    private void chargerMatieresEnseignees() {
        System.out.println("DEBUG (CreerCoursProfController): chargerMatieresEnseignees() - Début.");
        List<String> subjectsForThisProfessor = new ArrayList<>();

        if (enseignantConnecte != null) {
            try {
                List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_UTILISATEURS);
                // Sauter l'en-tête si la première colonne est "idUtilisateur"
                int startIndex = 0;
                if (!lignesUtilisateurs.isEmpty() && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].trim().equalsIgnoreCase("idUtilisateur")) {
                    startIndex = 1;
                }

                for (int i = startIndex; i < lignesUtilisateurs.size(); i++) {
                    String[] ligne = lignesUtilisateurs.get(i);
                    // S'assurer que la ligne est assez longue pour contenir l'ID utilisateur et les matières (index 7)
                    if (ligne.length > 7 && ligne[0] != null && !ligne[0].trim().isEmpty()) {
                        try {
                            int userId = Integer.parseInt(ligne[0].trim());
                            if (userId == enseignantConnecte.getIdUtilisateur()) {
                                String matiereString = ligne[7].trim(); // Index 7 pour matieresEnseignees

                                if (!matiereString.isEmpty()) {
                                    // Gérer le format [\"matiere1\",\"matiere2\"] ou simplement "matiere"
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
                                        // Cas où il n'y a qu'une seule matière sans les crochets/guillemets multiples
                                        String cleanedMatiere = matiereString.replace("\"", "").trim();
                                        if (!cleanedMatiere.isEmpty()) {
                                            subjectsForThisProfessor.add(cleanedMatiere);
                                        }
                                    }
                                }
                                break; // On a trouvé l'enseignant, on peut sortir de la boucle
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

        // Nettoyer les doublons et les chaînes vides/nulles
        subjectsForThisProfessor = subjectsForThisProfessor.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        matiereComboBox.getItems().clear(); // Vider avant d'ajouter
        if (!subjectsForThisProfessor.isEmpty()) {
            matiereComboBox.getItems().addAll(subjectsForThisProfessor);
            System.out.println("DEBUG (CreerCoursProfController): Matières chargées pour le professeur (" + (enseignantConnecte != null ? enseignantConnecte.getNom() : "N/A") + "): " + subjectsForThisProfessor);
            if (subjectsForThisProfessor.size() == 1) {
                matiereLabel.setText(subjectsForThisProfessor.get(0));
                matiereLabel.setVisible(true);
                matiereComboBox.setVisible(false);
                matiereComboBox.setDisable(true); // Désactiver si une seule matière
            } else {
                matiereComboBox.setVisible(true);
                matiereLabel.setVisible(false);
                matiereComboBox.setDisable(false); // Activer si plusieurs matières
            }
            errorMessageLabel.setText("");
        } else {
            // Si l'enseignant n'a pas de matières attribuées, on peut charger toutes les matières connues du cours.csv
            List<String> allMatiereNames = new ArrayList<>();
            try {
                // Index 1 pour la colonne 'matiere' dans cours.csv
                allMatiereNames = getAllUniqueValuesFromCSV(CHEMIN_COURS, 1);
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture de toutes les matières depuis cours.csv: " + e.getMessage());
                // Fallback si cours.csv ne peut pas être lu ou est vide
                allMatiereNames = Arrays.asList("Mathématiques", "Programmation", "Réseaux", "IA", "Anglais"); // Une liste par défaut si tout échoue
            }

            matiereComboBox.getItems().addAll(allMatiereNames);
            System.out.println("DEBUG (CreerCoursProfController): Aucune matière spécifique trouvée pour le professeur. Chargement de toutes les matières disponibles: " + allMatiereNames);
            matiereComboBox.setVisible(true);
            matiereLabel.setVisible(false);
            matiereComboBox.setDisable(false);
            errorMessageLabel.setText("Aucune matière spécifique attribuée. Affichage des matières disponibles.");
        }
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans matiereComboBox: " + matiereComboBox.getItems().size());
        System.out.println("DEBUG (CreerCoursProfController): chargerMatieresEnseignees() - Fin.");
    }

    // MODIFIÉE: Maintenant lit les classes depuis cours.csv
    private void chargerClasses() {
        System.out.println("DEBUG (CreerCoursProfController): chargerClasses() - Début.");
        List<String> classes = new ArrayList<>();
        try {
            // Index 9 pour la colonne 'classe' dans cours.csv
            classes = getAllUniqueValuesFromCSV(CHEMIN_COURS, 9);
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours pour classes (" + CHEMIN_COURS + "): " + e.getMessage());
            // Fallback si cours.csv ne peut pas être lu ou est vide
            classes = Arrays.asList("A1", "A2", "A3", "P1", "P2"); // Liste par défaut
        }

        classeComboBox.getItems().clear(); // Vider avant d'ajouter
        classeComboBox.getItems().addAll(classes);
        System.out.println("DEBUG (CreerCoursProfController): Classes chargées depuis CSV: " + classes + " dans ComboBox.");
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans classeComboBox: " + classeComboBox.getItems().size());
        errorMessageLabel.setText("");
        System.out.println("DEBUG (CreerCoursProfController): chargerClasses() - Fin.");
    }

    // MODIFIÉE: Maintenant lit les numéros de salle depuis salle.csv
    private void chargerSalles() {
        System.out.println("DEBUG (CreerCoursProfController): chargerSalles() - Début.");
        List<String> salleNumeros = new ArrayList<>();
        try {
            // Index 1 pour la colonne 'numeroSalle' dans salle.csv
            salleNumeros = getAllUniqueValuesFromCSV(CHEMIN_SALLE, 1);
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier salle (" + CHEMIN_SALLE + "): " + e.getMessage());
            // Fallback si salle.csv ne peut pas être lu ou est vide
            salleNumeros = Arrays.asList("A101", "A102", "B201", "B202", "C301", "C302", "A103", "B203"); // Votre ancienne liste fixe
        }

        salleIdComboBox.getItems().clear(); // Vider avant d'ajouter
        salleIdComboBox.getItems().addAll(salleNumeros);
        System.out.println("DEBUG (CreerCoursProfController): Salles chargées depuis CSV: " + salleNumeros + " dans ComboBox.");
        System.out.println("DEBUG (CreerCoursProfController): Nombre d'éléments dans salleIdComboBox: " + salleIdComboBox.getItems().size());
        errorMessageLabel.setText("");
        System.out.println("DEBUG (CreerCoursProfController): chargerSalles() - Fin.");
    }

    /**
     * Méthode générique pour lire des valeurs uniques d'une colonne spécifique d'un fichier CSV.
     * @param filePath Le chemin du fichier CSV.
     * @param columnIndex L'index de la colonne à lire (0-basé).
     * @return Une liste de chaînes de caractères uniques de la colonne spécifiée.
     * @throws IOException Si une erreur de lecture du fichier se produit.
     */
    private List<String> getAllUniqueValuesFromCSV(String filePath, int columnIndex) throws IOException {
        Set<String> uniqueValues = new LinkedHashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header line
                }
                String[] data = line.split(";", -1); // Utiliser -1 pour inclure les champs vides à la fin
                if (data.length > columnIndex) {
                    uniqueValues.add(data[columnIndex].trim());
                }
            }
        }
        return new ArrayList<>(uniqueValues);
    }

    private boolean validateTimeField(TextField field) {
        String timeText = field.getText();
        if (timeText.isEmpty()) {
            return true; // Accepter vide pour la validation des champs obligatoires ailleurs
        }
        try {
            LocalTime.parse(timeText, timeFormatter);
            field.setStyle(""); // Réinitialiser le style si valide
            return true;
        } catch (DateTimeParseException e) {
            field.setStyle("-fx-border-color: red;"); // Mettre en rouge si invalide
            return false;
        }
    }

    @FXML
    private void enregistrerCours() {
        errorMessageLabel.setText("");
        System.out.println("DEBUG (CreerCoursProfController): Début enregistrerCours().");

        String matiere = matiereLabel.isVisible() ? matiereLabel.getText() : matiereComboBox.getValue();
        String classe = classeComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String heureDebutStr = heureDebutField.getText();
        String heureFinStr = heureFinField.getText();
        String numeroSalleChoisi = salleIdComboBox.getValue();

        String descriptionCours = descriptionArea.getText();
        if (descriptionCours == null || descriptionCours.trim().isEmpty()) {
            descriptionCours = "";
        }

        String codeMatiere = "";
        if (matiere != null && !matiere.isEmpty()) {
            codeMatiere = matiere.substring(0, Math.min(matiere.length(), 4)).toUpperCase();
        }

        // --- Validation des entrées de base ---
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

        LocalTime heureDebut;
        LocalTime heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutStr, timeFormatter);
            heureFin = LocalTime.parse(heureFinStr, timeFormatter);
            if (heureFin.isBefore(heureDebut) || heureFin.equals(heureDebut)) {
                errorMessageLabel.setText("L'heure de fin doit être après l'heure de début."); return;
            }
            LocalTime minTime = LocalTime.of(8, 0);
            LocalTime maxTime = LocalTime.of(19, 0);
            if (heureDebut.isBefore(minTime) || heureFin.isAfter(maxTime)) {
                errorMessageLabel.setText("Les cours doivent être entre 08:00 et 19:00."); return;
            }
        } catch (DateTimeParseException e) {
            errorMessageLabel.setText("Format d'heure invalide (attendu HH:mm)."); return;
        }

        int enseignantId = (enseignantConnecte != null) ? enseignantConnecte.getIdUtilisateur() : -1;
        if (enseignantId == -1) {
            errorMessageLabel.setText("Erreur: ID de l'enseignant non trouvé. Veuillez vous reconnecter.");
            System.err.println("ERREUR: enseignantConnecte ou son ID est null/invalide lors de l'enregistrement du cours.");
            return;
        }

        // --- Détection de conflits AVANT de générer l'ID ---
        // Créez un objet Cours temporaire pour la vérification des conflits.
        // L'ID est 0 car il n'est pas encore attribué et n'intervient pas dans la détection de conflit.
        Cours nouveauCoursPropose = new Cours(
                0, matiere, codeMatiere, descriptionCours,
                enseignantId, numeroSalleChoisi, date, heureDebut, heureFin, classe
        );

        try {
            String conflictMessage = checkConflicts(nouveauCoursPropose);
            if (conflictMessage != null) {
                errorMessageLabel.setText(conflictMessage);
                return; // Arrêter si un conflit est détecté
            }
        } catch (IOException e) {
            errorMessageLabel.setText("Erreur lors de la vérification des conflits d'horaires: " + e.getMessage());
            System.err.println("ERREUR (CreerCoursProfController): Vérification des conflits: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        int idCours = generateUniqueCoursId(); // Générer l'ID seulement après la vérification des conflits
        if (idCours == -1) {
            errorMessageLabel.setText("Erreur: Impossible de générer un ID de cours unique. Veuillez réessayer.");
            return;
        }

        // Mettre à jour l'ID du cours proposé car il est maintenant attribué
        nouveauCoursPropose.setIdCours(idCours);

        // Utiliser la méthode toCSVArray() de l'objet Cours
        String[] nouveauCoursLigne = nouveauCoursPropose.toCSVArray();

        try {
            List<String[]> toutesLesLignesActuelles = CRUDcsvControllerProf.lire(CHEMIN_COURS);
            List<String[]> lignesAecrire = new ArrayList<>();

            boolean fichierExisteEtContientEntete = false;
            // Vérifier si l'en-tête existe en comparant les noms de colonnes
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
                // Garder l'en-tête existant
                lignesAecrire.add(toutesLesLignesActuelles.get(0));
                // Puis ajouter toutes les lignes de données existantes (à partir de la deuxième ligne)
                for (int i = 1; i < toutesLesLignesActuelles.size(); i++) {
                    lignesAecrire.add(toutesLesLignesActuelles.get(i));
                }
            }

            lignesAecrire.add(nouveauCoursLigne); // Ajoutez le nouveau cours

            CRUDcsvControllerProf.ecrire(CHEMIN_COURS, lignesAecrire);

            // --- Génération et enregistrement des notifications pour les élèves ---
            genererNotificationsNouveauCours(idCours, matiere, classe, date, heureDebutStr, heureFinStr, enseignantId);
            // --- FIN NOUVEAU ---

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours enregistré avec succès!");
            System.out.println("DEBUG (CreerCoursProfController): Cours enregistré. Fermeture de la fenêtre.");
            closeCurrentWindow();
        } catch (IOException e) {
            errorMessageLabel.setText("Erreur lors de l'enregistrement du cours: " + e.getMessage());
            System.err.println("ERREUR (CreerCoursProfController): Enregistrement cours: ");
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les conflits d'horaire pour un nouveau cours proposé.
     * Un conflit est détecté si :
     * 1. L'enseignant est déjà assigné à un autre cours sur la même plage horaire.
     * 2. La salle est déjà occupée par un autre cours sur la même plage horaire.
     * 3. La classe est déjà assignée à un autre cours sur la même plage horaire.
     *
     * @param nouveauCours Le cours à vérifier.
     * @return Un message d'erreur si un conflit est trouvé, null sinon.
     * @throws IOException Si une erreur de lecture du fichier des cours se produit.
     */
    private String checkConflicts(Cours nouveauCours) throws IOException {
        List<String[]> lignesCoursExistants = CRUDcsvControllerProf.lire(CHEMIN_COURS);
        List<Cours> coursExistants = new ArrayList<>();

        // Convertir les lignes CSV en objets Cours
        int startIndex = 0;
        if (!lignesCoursExistants.isEmpty() && lignesCoursExistants.get(0).length > 0 && "idCours".equalsIgnoreCase(lignesCoursExistants.get(0)[0].trim())) {
            startIndex = 1; // Sauter l'en-tête
        }

        for (int i = startIndex; i < lignesCoursExistants.size(); i++) {
            try {
                coursExistants.add(Cours.fromCsv(lignesCoursExistants.get(i)));
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors du parsing d'un cours existant depuis CSV: " + e.getMessage());
                // Continuer avec les autres cours même si un cours est mal formaté
            }
        }

        for (Cours existingCours : coursExistants) {
            // Un cours ne peut pas être en conflit avec lui-même s'il est mis à jour (ici, on crée un nouveau cours, donc l'ID sera différent)
            // if (nouveauCours.getIdCours() == existingCours.getIdCours()) {
            //     continue; // Ignorer le cours lui-même si on est en mode édition
            // }

            // Vérifier la date en premier pour optimiser
            if (nouveauCours.getDate().equals(existingCours.getDate())) {
                // Vérifier si les plages horaires se chevauchent
                if (doTimeRangesOverlap(
                        nouveauCours.getHeureDebut(), nouveauCours.getHeureFin(),
                        existingCours.getHeureDebut(), existingCours.getHeureFin())) {

                    // Conflit Enseignant
                    if (nouveauCours.getEnseignantId() == existingCours.getEnseignantId()) {
                        return "Conflit: L'enseignant est déjà assigné à un cours (" + existingCours.getMatiere() + ") le " +
                                nouveauCours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " de " +
                                existingCours.getHeureDebut().format(timeFormatter) + " à " +
                                existingCours.getHeureFin().format(timeFormatter) + ".";
                    }

                    // Conflit Salle
                    if (nouveauCours.getSalle().equalsIgnoreCase(existingCours.getSalle())) {
                        return "Conflit: La salle " + existingCours.getSalle() + " est déjà occupée par un cours (" + existingCours.getMatiere() + ") le " +
                                nouveauCours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " de " +
                                existingCours.getHeureDebut().format(timeFormatter) + " à " +
                                existingCours.getHeureFin().format(timeFormatter) + ".";
                    }

                    // Conflit Classe
                    if (nouveauCours.getClasse().equalsIgnoreCase(existingCours.getClasse())) {
                        return "Conflit: La classe " + existingCours.getClasse() + " a déjà un cours (" + existingCours.getMatiere() + ") programmé le " +
                                nouveauCours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " de " +
                                existingCours.getHeureDebut().format(timeFormatter) + " à " +
                                existingCours.getHeureFin().format(timeFormatter) + ".";
                    }
                }
            }
        }
        return null; // Pas de conflit
    }

    /**
     * Vérifie si deux plages horaires se chevauchent.
     * @param start1 Heure de début de la plage 1.
     * @param end1 Heure de fin de la plage 1.
     * @param start2 Heure de début de la plage 2.
     * @param end2 Heure de fin de la plage 2.
     * @return true si les plages se chevauchent, false sinon.
     */
    private boolean doTimeRangesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Deux plages horaires se chevauchent si le début de l'une est avant la fin de l'autre
        // ET la fin de l'une est après le début de l'autre.
        // Exclut les contacts où l'heure de fin d'un cours est égale à l'heure de début d'un autre (pas de conflit)
        return start1.isBefore(end2) && end1.isAfter(start2);
    }


    /**
     * Génère et enregistre des notifications pour les élèves concernés par un nouveau cours.
     * @param idCours L'ID du cours qui vient d'être créé.
     * @param matiere La matière du nouveau cours.
     * @param classeConcernee La classe à laquelle le cours est destiné.
     * @param dateCours La date du cours.
     * @param heureDebutStr L'heure de début du cours (string).
     * @param heureFinStr L'heure de fin du cours (string).
     * @param emetteurId L'ID de l'enseignant qui crée le cours.
     */
    private void genererNotificationsNouveauCours(int idCours, String matiere, String classeConcernee,
                                                  LocalDate dateCours, String heureDebutStr, String heureFinStr,
                                                  int emetteurId) {
        // Message de la notification
        String messageNotification = String.format("Un nouveau cours de %s a été ajouté pour votre classe %s le %s de %s à %s.",
                matiere, classeConcernee, dateCours.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), heureDebutStr, heureFinStr);

        try {
            // 1. Lire tous les utilisateurs pour trouver les élèves de la classe concernée
            List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_UTILISATEURS);
            // Ignorer l'en-tête du fichier utilisateurs si présent
            int startIndexUtilisateurs = (lignesUtilisateurs.size() > 0 && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].equalsIgnoreCase("idUtilisateur")) ? 1 : 0;

            // 2. Parcourir les utilisateurs pour trouver les élèves à notifier
            for (int i = startIndexUtilisateurs; i < lignesUtilisateurs.size(); i++) {
                String[] userData = lignesUtilisateurs.get(i);
                // Vérifier si c'est un ETUDIANT et si sa classe correspond
                // Assurez-vous que les index des colonnes correspondent à votre fichier utilisateurs.csv
                // Col. 0: idUtilisateur, Col. 5: role, Col. 6: groupe (pour les étudiants)
                if (userData.length > 6 && userData[5].trim().equalsIgnoreCase("ETUDIANT")) {
                    String eleveClasse = userData[6].trim().replace("\"", ""); // Utilisez l'index 6 pour la classe de l'élève

                    if (eleveClasse.equalsIgnoreCase(classeConcernee)) {
                        int idEleve = Integer.parseInt(userData[0].trim()); // L'ID de l'élève

                        // Créer une nouvelle notification pour cet élève
                        int idNotification = generateUniqueNotificationId();
                        Notification newNotification = new Notification(
                                idNotification,
                                LocalDateTime.now(), // Date/heure actuelle de la création de la notification
                                messageNotification,
                                "NouveauCours",      // Type de notification
                                "NON_LUE",           // Statut initial
                                emetteurId,          // ID de l'enseignant qui a créé le cours
                                idEleve,             // ID de l'élève destinataire
                                "ETUDIANT",          // Type du destinataire (rôle de l'utilisateur)
                                idCours              // ID du cours concerné
                        );
                        // Utiliser la méthode ajouterNotification de CRUDNotification
                        CRUDNotification.ajouterNotification(CHEMIN_NOTIFICATIONS, newNotification);
                        System.out.println("DEBUG: Notification générée pour l'élève ID: " + idEleve);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des utilisateurs ou de l'écriture des notifications: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique lors de la lecture des IDs d'utilisateur: " + e.getMessage());
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
            // Si le fichier n'existe pas ou est vide, on commence à 1
            return 1;
        }
    }

    /**
     * Génère un ID unique pour une nouvelle notification.
     * Lit le fichier des notifications pour trouver le maximum ID existant.
     * @return Le nouvel ID unique.
     */
    private int generateUniqueNotificationId() {
        int maxId = 0;
        try {
            List<Notification> notifications = CRUDNotification.lireNotifications(CHEMIN_NOTIFICATIONS);
            for (Notification notif : notifications) {
                if (notif.getIdNotification() > maxId) {
                    maxId = notif.getIdNotification();
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier des notifications pour générer un ID. Retourne 1 si le fichier n'existe pas encore: " + e.getMessage());
            // Si le fichier n'existe pas ou est vide, on commence à 1
            return 1;
        }
    }

    @FXML
    private void annuler() {
        System.out.println("DEBUG (CreerCoursProfController): Annulation. Fermeture de la fenêtre.");
        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        Stage stage = (Stage) errorMessageLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}