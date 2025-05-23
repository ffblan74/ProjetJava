package projet.controleurs.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import projet.models.Cours;
import projet.models.Enseignant;
import projet.models.Notification;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;
import projet.utils.TransmissibleRetour;

import projet.controleurs.CRUDcsvControllerProf;
import projet.controleurs.CRUDNotification;

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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class CreerCoursAdminController implements Transmissible, TransmissibleRetour, Initializable {

    @FXML private ComboBox<String> matiereComboBox;
    @FXML private ComboBox<String> classeComboBox;
    @FXML private ComboBox<String> salleComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Enseignant> enseignantComboBox;

    private Cours coursAModifier;
    private Object donneesARetourner;

    private static final String CHEMIN_FICHIER_COURS = "src/main/resources/projet/csv/cours.csv";
    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String CHEMIN_NOTIFICATIONS = "src/main/resources/projet/csv/notifications.csv";
    private static final String CHEMIN_FICHIER_SALLE = "src/main/resources/projet/csv/salle.csv";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatterForCode = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String[] ENTETE_COURS_CSV = {"idCours", "matiere", "codeMatiere", "description", "enseignantId", "salleId", "date", "heureDebut", "heureFin", "classe"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerMatieres();
        chargerClasses();
        chargerSalles();
        chargerEnseignants();

        enseignantComboBox.setConverter(new StringConverter<Enseignant>() {
            @Override
            public String toString(Enseignant enseignant) {
                return enseignant != null ? enseignant.getPrenom() + " " + enseignant.getNom() : "";
            }
            @Override
            public Enseignant fromString(String string) {
                return null;
            }
        });

        addTimeFieldFormatListeners();
    }

    private void chargerMatieres() {
        List<String> matieres = new ArrayList<>();
        try {
            matieres = getAllUniqueValuesFromCSV(CHEMIN_FICHIER_COURS, 1);
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours pour matières (" + CHEMIN_FICHIER_COURS + "): " + e.getMessage());
            matieres = Arrays.asList("Mathématiques", "Physique", "SI", "Java", "Electronique", "Signal", "Anglais");
        }
        matiereComboBox.setItems(FXCollections.observableArrayList(matieres));
    }

    private void chargerClasses() {
        List<String> classes = new ArrayList<>();
        try {
            classes = getAllUniqueValuesFromCSV(CHEMIN_FICHIER_COURS, 9);
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours pour classes (" + CHEMIN_FICHIER_COURS + "): " + e.getMessage());
            classes = Arrays.asList("A1", "A2", "A3", "P1", "P2");
        }
        classeComboBox.setItems(FXCollections.observableArrayList(classes));
    }

    private void chargerSalles() {
        List<String> salles = new ArrayList<>();
        try {
            salles = getAllUniqueValuesFromCSV(CHEMIN_FICHIER_SALLE, 1);
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier salle (" + CHEMIN_FICHIER_SALLE + "): " + e.getMessage());
            salles = Arrays.asList("A101", "A102", "B201", "B202", "C301", "C302", "A103", "B203");
        }
        salleComboBox.setItems(FXCollections.observableArrayList(salles));
    }

    private List<String> getAllUniqueValuesFromCSV(String filePath, int columnIndex) throws IOException {
        Set<String> uniqueValues = new LinkedHashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    String[] headerTokens = line.split(";");
                    if (headerTokens.length > 0 && (headerTokens[0].equalsIgnoreCase("idCours") || headerTokens[0].equalsIgnoreCase("idUtilisateur") || headerTokens[0].equalsIgnoreCase("idSalle"))) {
                        isHeader = false;
                        continue;
                    }
                }
                String[] data = line.split(";");
                if (data.length > columnIndex) {
                    uniqueValues.add(data[columnIndex].trim().replace("\"", ""));
                }
            }
        }
        return new ArrayList<>(uniqueValues);
    }

    private void addTimeFieldFormatListeners() {
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

    private boolean validateTimeField(TextField field) {
        String timeText = field.getText();
        if (timeText.isEmpty()) {
            field.setStyle("");
            return true;
        }
        try {
            LocalTime.parse(timeText, timeFormatter);
            field.setStyle("");
            return true;
        } catch (DateTimeParseException e) {
            field.setStyle("-fx-border-color: red; -fx-background-color: #ffe6e6;");
            return false;
        }
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Cours) {
            this.coursAModifier = (Cours) data;
            matiereComboBox.setValue(coursAModifier.getMatiere());
            classeComboBox.setValue(coursAModifier.getClasse());
            salleComboBox.setValue(coursAModifier.getSalle());
            datePicker.setValue(coursAModifier.getDate());
            heureDebutField.setText(coursAModifier.getHeureDebut().format(timeFormatter));
            heureFinField.setText(coursAModifier.getHeureFin().format(timeFormatter));
            descriptionArea.setText(coursAModifier.getDescription());

            Optional<Enseignant> selectedEnseignant = enseignantComboBox.getItems().stream()
                    .filter(e -> e.getIdUtilisateur() == coursAModifier.getEnseignantId())
                    .findFirst();
            selectedEnseignant.ifPresent(e -> enseignantComboBox.getSelectionModel().select(e));

        } else if (data == null) {
            this.coursAModifier = null;
            matiereComboBox.getSelectionModel().clearSelection();
            classeComboBox.getSelectionModel().clearSelection();
            salleComboBox.getSelectionModel().clearSelection();
            datePicker.setValue(LocalDate.now());
            heureDebutField.setText("08:00");
            heureFinField.setText("09:00");
            descriptionArea.setText("");
            enseignantComboBox.getSelectionModel().clearSelection();
        } else {
            System.err.println("ERREUR (CreerCoursAdminController): Données transmises via Transmissible ne sont pas de type Cours ni Utilisateur. Type reçu: " + (data != null ? data.getClass().getName() : "null"));
            NavigationUtil.afficherErreur("Erreur, type de données inattendu a été transmis à la fenêtre de cours.");
        }
    }

    @Override
    public Object getDonneesRetour() {
        return donneesARetourner;
    }

    private void chargerEnseignants() {
        List<Enseignant> enseignants = new ArrayList<>();
        try {
            List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_UTILISATEURS);
            int startIndex = (lignesUtilisateurs.size() > 0 && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].equalsIgnoreCase("idUtilisateur")) ? 1 : 0;

            for (int i = startIndex; i < lignesUtilisateurs.size(); i++) {
                String[] data = lignesUtilisateurs.get(i);
                if (data.length >= 6 && data[5].trim().equalsIgnoreCase("ENSEIGNANT")) {
                    try {
                        enseignants.add(Enseignant.fromCsv(data));
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement d'un enseignant depuis CSV: " + Arrays.toString(data) + " - " + e.getMessage());
                    }
                }
            }
            enseignantComboBox.setItems(FXCollections.observableArrayList(enseignants));
        } catch (IOException e) {
            System.err.println("Erreur de chargement des enseignants: " + e.getMessage());
            NavigationUtil.afficherErreur("Erreur de chargement des enseignants: " + e.getMessage());
        }
    }

    @FXML
    private void handleSauvegarderCours(ActionEvent event) {
        if (!validerChamps()) {
            return;
        }

        String matiere = matiereComboBox.getValue();
        String classe = classeComboBox.getValue();
        String salle = salleComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String heureDebutStr = heureDebutField.getText().trim();
        String heureFinStr = heureFinField.getText().trim();
        String description = descriptionArea.getText().trim();

        Enseignant selectedEnseignant = enseignantComboBox.getSelectionModel().getSelectedItem();
        if (selectedEnseignant == null) {
            NavigationUtil.afficherErreur("Vous devez sélectionner un enseignant.");
            return;
        }
        int enseignantId = selectedEnseignant.getIdUtilisateur();
        String enseignantNomComplet = selectedEnseignant.getPrenom() + " " + selectedEnseignant.getNom();

        LocalTime heureDebutParsed;
        LocalTime heureFinParsed;
        try {
            heureDebutParsed = LocalTime.parse(heureDebutStr, timeFormatter);
            heureFinParsed = LocalTime.parse(heureFinStr, timeFormatter);
            if (heureFinParsed.isBefore(heureDebutParsed) || heureFinParsed.equals(heureDebutParsed)) {
                NavigationUtil.afficherErreur("L'heure de fin doit être après l'heure de début.");
                return;
            }
        } catch (DateTimeParseException e) {
            NavigationUtil.afficherErreur("Format d'heure invalide (attendu HH:mm).");
            return;
        }

        Cours coursPotentiel = new Cours(
                (coursAModifier != null ? coursAModifier.getIdCours() : -1),
                matiere, "TEMP", description, enseignantId, salle, date, heureDebutParsed, heureFinParsed, classe
        );

        String conflitMessage = checkConflicts(coursPotentiel);
        if (conflitMessage != null) {
            NavigationUtil.afficherErreur("Conflit d'horaire détecté");
            return;
        }

        int idCours;
        String typeAction;
        Cours coursSauvegarde;

        if (coursAModifier != null) {
            idCours = coursAModifier.getIdCours();
            typeAction = "Modification";

            coursAModifier.setMatiere(matiere);
            coursAModifier.setClasse(classe);
            coursAModifier.setSalle(salle);
            coursAModifier.setDate(date);
            coursAModifier.setHeureDebut(heureDebutParsed);
            coursAModifier.setHeureFin(heureFinParsed);
            coursAModifier.setDescription(description);
            coursAModifier.setEnseignantId(enseignantId);
            coursAModifier.setEnseignantNomComplet(enseignantNomComplet);
            coursSauvegarde = coursAModifier;

            try {
                List<String[]> lignes = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
                boolean found = false;
                int startIndex = (lignes.size() > 0 && lignes.get(0).length > 0 && lignes.get(0)[0].equalsIgnoreCase("idCours")) ? 1 : 0;
                for (int i = startIndex; i < lignes.size(); i++) {
                    if (lignes.get(i)[0].equals(String.valueOf(coursAModifier.getIdCours()))) {
                        lignes.set(i, coursAModifier.toCSVArray());
                        found = true;
                        break;
                    }
                }
                if (found) {
                    CRUDcsvControllerProf.ecrire(CHEMIN_FICHIER_COURS, lignes);
                    NavigationUtil.afficherSucces("Modification Réussie", null, "Le cours a été modifié avec succès.");
                    donneesARetourner = "refresh";
                } else {
                    NavigationUtil.afficherErreur("Erreur : Cours à modifier non trouvé dans le fichier.");
                    return;
                }

            } catch (IOException e) {
                NavigationUtil.afficherErreur("Erreur lors de la modification du cours : " + e.getMessage());
                e.printStackTrace();
                return;
            }

        } else {
            idCours = generateNewCoursId();
            typeAction = "Création";
            String generatedCodeCours = generateCoursCode(matiere, date, idCours);

            Cours nouveauCours = new Cours(idCours, matiere, generatedCodeCours, description,
                    enseignantId, salle, date, heureDebutParsed, heureFinParsed, classe);
            coursSauvegarde = nouveauCours;

            try {
                List<String[]> toutesLesLignesActuelles = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
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
                } else {
                    lignesAecrire.addAll(toutesLesLignesActuelles);
                }

                lignesAecrire.add(nouveauCours.toCSVArray());

                CRUDcsvControllerProf.ecrire(CHEMIN_FICHIER_COURS, lignesAecrire);

                NavigationUtil.afficherSucces("Ajout Réussi", null, "Le nouveau cours a été ajouté avec succès.");
                donneesARetourner = "refresh";

            } catch (IOException e) {
                NavigationUtil.afficherErreur("Erreur lors de l'ajout du cours : " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        genererNotificationsPourCours(coursSauvegarde, typeAction);
        fermerFenetre(event);
    }

    private boolean validerChamps() {
        if (matiereComboBox.getValue() == null || matiereComboBox.getValue().isEmpty() ||
                classeComboBox.getValue() == null || classeComboBox.getValue().isEmpty() ||
                salleComboBox.getValue() == null || salleComboBox.getValue().isEmpty() ||
                datePicker.getValue() == null ||
                heureDebutField.getText().trim().isEmpty() ||
                heureFinField.getText().trim().isEmpty() ||
                enseignantComboBox.getSelectionModel().getSelectedItem() == null)
        {
            NavigationUtil.afficherErreur("Tous les champs obligatoires (matière, classe, salle, date, heures, enseignant) doivent être remplis.");
            return false;
        }
        if (!validateTimeField(heureDebutField) || !validateTimeField(heureFinField)) {
            NavigationUtil.afficherErreur("Les formats d'heure doivent être HH:mm.");
            return false;
        }
        try {
            LocalTime heureDebut = LocalTime.parse(heureDebutField.getText().trim(), timeFormatter);
            LocalTime heureFin = LocalTime.parse(heureFinField.getText().trim(), timeFormatter);
            if (heureFin.isBefore(heureDebut) || heureFin.equals(heureDebut)) {
                NavigationUtil.afficherErreur("L'heure de fin doit être après l'heure de début.");
                return false;
            }
        } catch (DateTimeParseException e) {
            NavigationUtil.afficherErreur("Format d'heure invalide (attendu HH:mm).");
            return false;
        }

        return true;
    }

    private String checkConflicts(Cours nouveauOuModifieCours) {
        List<Cours> tousLesCoursExistants = new ArrayList<>();
        try {
            List<String[]> lignes = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
            int startIndex = (lignes.size() > 0 && lignes.get(0).length > 0 && lignes.get(0)[0].equalsIgnoreCase("idCours")) ? 1 : 0;
            for (int i = startIndex; i < lignes.size(); i++) {
                try {
                    Cours existingCours = Cours.fromCsv(lignes.get(i));
                    if (coursAModifier != null && existingCours.getIdCours() == coursAModifier.getIdCours()) {
                        continue;
                    }
                    tousLesCoursExistants.add(existingCours);
                } catch (Exception e) {
                    System.err.println("Erreur de lecture d'un cours existant pour la détection de conflits: " + e.getMessage() + " Ligne: " + String.join(";", lignes.get(i)));
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier des cours pour la détection de conflits: " + e.getMessage());
            return "Erreur interne lors de la vérification des conflits.";
        }

        LocalDate nouvelleDate = nouveauOuModifieCours.getDate();
        LocalTime nouvelleHeureDebut = nouveauOuModifieCours.getHeureDebut();
        LocalTime nouvelleHeureFin = nouveauOuModifieCours.getHeureFin();
        int nouvelEnseignantId = nouveauOuModifieCours.getEnseignantId();
        String nouvelleSalle = nouveauOuModifieCours.getSalle();
        String nouvelleClasse = nouveauOuModifieCours.getClasse();

        for (Cours coursExistant : tousLesCoursExistants) {
            if (coursExistant.getDate().equals(nouvelleDate)) {
                if (doTimeRangesOverlap(nouvelleHeureDebut, nouvelleHeureFin, coursExistant.getHeureDebut(), coursExistant.getHeureFin())) {

                    if (coursExistant.getEnseignantId() == nouvelEnseignantId) {
                        return String.format("L'enseignant est déjà assigné au cours '%s' de %s à %s le %s.",
                                coursExistant.getMatiere(),
                                coursExistant.getHeureDebut().format(timeFormatter),
                                coursExistant.getHeureFin().format(timeFormatter),
                                coursExistant.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }

                    if (coursExistant.getSalle().equalsIgnoreCase(nouvelleSalle)) {
                        return String.format("La salle '%s' est déjà occupée par le cours de '%s' de %s à %s le %s.",
                                coursExistant.getSalle(),
                                coursExistant.getMatiere(),
                                coursExistant.getHeureDebut().format(timeFormatter),
                                coursExistant.getHeureFin().format(timeFormatter),
                                coursExistant.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }

                    if (coursExistant.getClasse().equalsIgnoreCase(nouvelleClasse)) {
                        return String.format("La classe '%s' est déjà assignée au cours de '%s' de %s à %s le %s.",
                                coursExistant.getClasse(),
                                coursExistant.getMatiere(),
                                coursExistant.getHeureDebut().format(timeFormatter),
                                coursExistant.getHeureFin().format(timeFormatter),
                                coursExistant.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                }
            }
        }
        return null;
    }

    private boolean doTimeRangesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void genererNotificationsPourCours(Cours cours, String typeAction) {
        String messageEleve;
        String messageEnseignant;

        if ("Création".equals(typeAction)) {
            messageEleve = String.format("Un nouveau cours de %s pour votre classe %s a été programmé le %s de %s à %s en salle %s.",
                    cours.getMatiere(), cours.getClasse(), cours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    cours.getHeureDebut().format(timeFormatter), cours.getHeureFin().format(timeFormatter), cours.getSalle());
            messageEnseignant = String.format("Vous avez été assigné à un nouveau cours de %s (%s) pour la classe %s le %s de %s à %s en salle %s.",
                    cours.getMatiere(), cours.getDescription(), cours.getClasse(), cours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    cours.getHeureDebut().format(timeFormatter), cours.getHeureFin().format(timeFormatter), cours.getSalle());
        } else {
            messageEleve = String.format("Le cours de %s pour votre classe %s du %s de %s à %s en salle %s a été modifié. Veuillez vérifier les détails.",
                    cours.getMatiere(), cours.getClasse(), cours.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    cours.getHeureDebut().format(timeFormatter), cours.getHeureFin().format(timeFormatter), cours.getSalle());
            messageEnseignant = String.format("Le cours de %s (%s) pour la classe %s que vous enseignez a été modifié. Veuillez vérifier les détails. Nouvelle salle: %s, Nouvel horaire: %s-%s.",
                    cours.getMatiere(), cours.getDescription(), cours.getClasse(), cours.getSalle(),
                    cours.getHeureDebut().format(timeFormatter), cours.getHeureFin().format(timeFormatter));
        }

        try {
            int idNotificationEnseignant = generateUniqueNotificationId();
            Notification notifEnseignant = new Notification(
                    idNotificationEnseignant,
                    LocalDateTime.now(),
                    messageEnseignant,
                    typeAction.equals("Création") ? "NouveauCours" : "ModificationCours",
                    "NON_LUE",
                    0,
                    cours.getEnseignantId(),
                    "PROFESSEUR",
                    cours.getIdCours()
            );
            CRUDNotification.ajouterNotification(CHEMIN_NOTIFICATIONS, notifEnseignant);
            System.out.println("Notification " + typeAction + " générée pour l'enseignant ID: " + cours.getEnseignantId());

            List<String[]> lignesUtilisateurs = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_UTILISATEURS);
            int startIndexUtilisateurs = (lignesUtilisateurs.size() > 0 && lignesUtilisateurs.get(0).length > 0 && lignesUtilisateurs.get(0)[0].equalsIgnoreCase("idUtilisateur")) ? 1 : 0;

            for (int i = startIndexUtilisateurs; i < lignesUtilisateurs.size(); i++) {
                String[] userData = lignesUtilisateurs.get(i);
                if (userData.length > 6 && userData[5].trim().equalsIgnoreCase("ETUDIANT")) {
                    String eleveClasse = userData[6].trim().replace("\"", "");

                    if (eleveClasse.equalsIgnoreCase(cours.getClasse())) {
                        int idEleve = Integer.parseInt(userData[0].trim());

                        int idNotificationEleve = generateUniqueNotificationId();
                        Notification notifEleve = new Notification(
                                idNotificationEleve,
                                LocalDateTime.now(),
                                messageEleve,
                                typeAction.equals("Création") ? "NouveauCours" : "ModificationCours",
                                "NON_LUE",
                                0,
                                idEleve,
                                "ETUDIANT",
                                cours.getIdCours()
                        );
                        CRUDNotification.ajouterNotification(CHEMIN_NOTIFICATIONS, notifEleve);
                        System.out.println("Notification " + typeAction + " générée pour l'élève ID: " + idEleve + " (Classe: " + eleveClasse + ")");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération/enregistrement des notifications du cours : " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique lors de la lecture des IDs d'utilisateur pour la notification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateCoursCode(String matiere, LocalDate date, int idCours) {
        String matierePrefix = matiere.length() >= 3 ? matiere.substring(0, 3).toUpperCase() : matiere.toUpperCase();
        return matierePrefix + "-" + date.format(dateFormatterForCode) + "-" + idCours;
    }

    private int generateNewCoursId() {
        int maxId = 0;
        try {
            List<String[]> lignes = CRUDcsvControllerProf.lire(CHEMIN_FICHIER_COURS);
            int startIndex = (lignes.size() > 0 && lignes.get(0).length > 0 && lignes.get(0)[0].equalsIgnoreCase("idCours")) ? 1 : 0;
            for (int i = startIndex; i < lignes.size(); i++) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(lignes.get(i)[0]));
                } catch (NumberFormatException e) {
                    System.err.println("ID de cours invalide trouvé dans le CSV: '" + lignes.get(i)[0] + "' - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du nouvel ID de cours: " + e.getMessage());
            return 1;
        }
        return maxId + 1;
    }

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
            return 1;
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        donneesARetourner = null;
        fermerFenetre(event);
    }

    private void fermerFenetre(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}