package projet.controleurs.professeur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox; // Type de la racine de votre FXML

import projet.models.Cours;
import projet.models.Enseignant;
import projet.models.Utilisateur;
import projet.models.Role; // Pour les rôles des utilisateurs
import projet.models.Etudiant; // Pour charger les groupes
import projet.models.Salle;   // Pour charger les salles
import projet.controleurs.CRUDcsvController; // Import de votre contrôleur CSV

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.function.Supplier; // Assurez-vous que cet import est présent

public class AccueilProfController {

    @FXML
    private TableView<Cours> tableViewEmploiDuTemps;

    private ObservableList<Cours> listeCoursProfesseur = FXCollections.observableArrayList();

    // --- Chemins des fichiers CSV (à vérifier et ajuster si besoin) ---
    private static final String COURS_CSV_PATH = "src/main/resources/projet/csv/cours.csv";
    private static final String UTILISATEURS_CSV_PATH = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String SALLES_CSV_PATH = "src/main/resources/projet/csv/salle.csv"; // Nom du fichier confirmé "salle.csv"

    @FXML
    public void initialize() {
        Utilisateur utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        if (utilisateurConnecte instanceof Enseignant) {
            Enseignant enseignantConnecte = (Enseignant) utilisateurConnecte;
            System.out.println("Enseignant connecté : " + enseignantConnecte.getNom() + " " + enseignantConnecte.getPrenom());

            chargerCoursPourEnseignant(enseignantConnecte.getIdEnseignant());

            // Assurez-vous que les colonnes de votre TableView sont correctement configurées
            // Ex: tableViewEmploiDuTemps.getColumns().add(new TableColumn<>("Matière", new PropertyValueFactory<>("matiere")));
            // (Ces configurations de colonnes ne sont pas dans ce fichier FXML)

            tableViewEmploiDuTemps.setItems(listeCoursProfesseur);
        } else {
            showAlert("Erreur d'authentification", "Accès non autorisé", "Vous n'êtes pas connecté en tant qu'enseignant.");
            // Gérer la redirection ou la fermeture de la fenêtre si l'utilisateur n'est pas un enseignant
        }
    }

    private void chargerCoursPourEnseignant(int enseignantId) {
        try {
            List<Cours> tousLesCours = chargerCoursDepuisCSV(); // Utilise la méthode locale de chargement
            listeCoursProfesseur.setAll(tousLesCours.stream()
                    .filter(cours -> cours.getEnseignantId() == enseignantId)
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            showAlert("Erreur de chargement", "Impossible de charger les cours", "Une erreur est survenue lors du chargement des données des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge tous les cours depuis le fichier CSV.
     * @return Liste de tous les objets Cours.
     * @throws IOException Si une erreur de lecture/écriture survient.
     */
    private List<Cours> chargerCoursDepuisCSV() throws IOException {
        List<Cours> coursList = new ArrayList<>();
        List<String[]> records = CRUDcsvController.lire(COURS_CSV_PATH);

        if (records.isEmpty()) return coursList; // Pas de données

        // Saute l'en-tête (première ligne)
        // Vérifier si la liste a au moins deux éléments (en-tête + au moins une ligne de données)
        List<String[]> dataRecords = (records.size() > 1) ? records.subList(1, records.size()) : new ArrayList<>();

        for (String[] record : dataRecords) {
            // Vérifier que le tableau a suffisamment d'éléments avant d'accéder aux index
            if (record.length < 12) { // 12 colonnes attendues pour le format complet du cours
                System.err.println("Ligne CSV cours invalide (trop peu de colonnes): " + Arrays.toString(record));
                continue; // Passer à la ligne suivante si la ligne est mal formée
            }
            try {
                int idCours = Integer.parseInt(record[0]); // idCours
                String nomMatiere = record[1]; // nomMatiere
                String codeCours = record[2]; // codeCours
                String description = record[3]; // description
                Duration duree = parseDuration(record[4]); // duree (ex: "2h")
                int enseignantId = Integer.parseInt(record[5]); // enseignantId
                int salleId = Integer.parseInt(record[6]); // salleId
                int horaireId = Integer.parseInt(record[7]); // horaireId

                // Nouvelles colonnes (index 8 à 11)
                String jour = record[8];
                String heureDebut = record[9];
                String heureFin = record[10];
                String classe = record[11];

                coursList.add(new Cours(idCours, nomMatiere, codeCours, description, duree, enseignantId, salleId, horaireId,
                        jour, heureDebut, heureFin, classe));
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format numérique dans la ligne CSV cours: " + Arrays.toString(record) + " - " + e.getMessage());
            }
        }
        return coursList;
    }

    /**
     * Charge tous les étudiants (pour obtenir les groupes) depuis le fichier CSV.
     * @return Liste de tous les objets Etudiant.
     * @throws IOException Si une erreur de lecture/écriture survient.
     */
    private List<Etudiant> chargerEtudiantsDepuisCSV() throws IOException {
        List<Etudiant> etudiants = new ArrayList<>();
        List<String[]> records = CRUDcsvController.lire(UTILISATEURS_CSV_PATH);

        if (records.isEmpty()) return etudiants;
        List<String[]> dataRecords = (records.size() > 1) ? records.subList(1, records.size()) : new ArrayList<>();

        for (String[] record : dataRecords) {
            // Assurez-vous que le tableau a suffisamment d'éléments (min 11 colonnes pour un utilisateur complet)
            if (record.length < 11) {
                System.err.println("Ligne CSV utilisateurs invalide (trop peu de colonnes): " + Arrays.toString(record));
                continue;
            }
            try {
                // Vérifier le rôle avant de créer un objet Etudiant
                Role role = Role.valueOf(record[5].toUpperCase()); // Index 5 pour le rôle
                if (role == Role.ETUDIANT) {
                    int idUtilisateur = Integer.parseInt(record[0]);
                    String nom = record[1];
                    String prenom = record[2];
                    String email = record[3];
                    String motDePasse = record[4];
                    String groupe = record[8]; // Index 8 pour le groupe
                    int emploiDuTempsId = "None".equalsIgnoreCase(record[9]) || record[9].isEmpty() ? 0 : Integer.parseInt(record[9]);
                    etudiants.add(new Etudiant(idUtilisateur, nom, prenom, email, motDePasse, groupe, emploiDuTempsId));
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur de parsing de rôle ou de format numérique dans la ligne CSV utilisateurs: " + Arrays.toString(record) + " - " + e.getMessage());
            }
        }
        return etudiants;
    }

    /**
     * Charge toutes les salles depuis le fichier CSV.
     * @return Liste de tous les objets Salle.
     * @throws IOException Si une erreur de lecture/écriture survient.
     */
    private List<Salle> chargerSallesDepuisCSV() throws IOException {
        List<Salle> salles = new ArrayList<>();
        List<String[]> records = CRUDcsvController.lire(SALLES_CSV_PATH);

        if (records.isEmpty()) return salles;
        List<String[]> dataRecords = (records.size() > 1) ? records.subList(1, records.size()) : new ArrayList<>();

        for (String[] record : dataRecords) {
            // Vérifier que le tableau a suffisamment d'éléments (min 5 colonnes pour une salle)
            if (record.length < 5) {
                System.err.println("Ligne CSV salles invalide (trop peu de colonnes): " + Arrays.toString(record));
                continue;
            }
            try {
                int idSalle = Integer.parseInt(record[0]);
                String numeroSalle = record[1];
                int capacite = Integer.parseInt(record[2]);
                String localisation = record[3];
                List<Integer> materielIds = parseListInteger(record[4]); // Parsing de la liste d'IDs de matériel
                salles.add(new Salle(idSalle, numeroSalle, capacite, localisation, materielIds));
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format numérique dans la ligne CSV salles: " + Arrays.toString(record) + " - " + e.getMessage());
            }
        }
        return salles;
    }

    /**
     * Génère le prochain ID de cours unique en se basant sur les IDs existants.
     * @return Le prochain ID disponible.
     * @throws IOException Si une erreur de lecture CSV survient.
     */
    private int genererProchainIdCours() throws IOException {
        List<Cours> tousLesCours = chargerCoursDepuisCSV();
        return tousLesCours.stream()
                .mapToInt(Cours::getIdCours)
                .max()
                .orElse(0) + 1; // Commence à 1 si aucun cours n'existe (max ID = 0)
    }

    /**
     * Parse une durée exprimée en "Xh" en un objet Duration.
     * @param durationString La chaîne de durée (ex: "2h").
     * @return L'objet Duration correspondant.
     */
    private Duration parseDuration(String durationString) {
        if (durationString != null && durationString.endsWith("h")) {
            try {
                int hours = Integer.parseInt(durationString.replace("h", ""));
                return Duration.ofHours(hours);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format de durée (non numérique): " + durationString);
            }
        }
        // Retourne une durée de 0 heures si le format n'est pas reconnu ou est invalide
        return Duration.ofHours(0);
    }

    /**
     * Parse une chaîne de caractères représentant une liste (ex: "[item1,item2]" ou "item1,item2")
     * en une List<String>.
     * @param raw La chaîne brute.
     * @return La liste de chaînes.
     */
    private List<String> parseListString(String raw) {
        if (raw == null || raw.trim().isEmpty() || "None".equalsIgnoreCase(raw)) {
            return new ArrayList<>();
        }
        String cleaned = raw.replaceAll("[\\[\\]\"]", ""); // Supprime les crochets et guillemets
        if (cleaned.isEmpty()) return new ArrayList<>();
        // IMPORTANT: Si vos listes internes (ex: matières enseignées) sont séparées par des points-virgules,
        // remplacez la virgule par un point-virgule dans le split.
        return Arrays.stream(cleaned.split(",")) // <<-- Vérifiez ce délimiteur pour les listes internes
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Parse une chaîne de caractères représentant une liste d'entiers (ex: "[1,2,3]" ou "1,2,3")
     * en une List<Integer>.
     * @param raw La chaîne brute.
     * @return La liste d'entiers.
     */
    private List<Integer> parseListInteger(String raw) {
        if (raw == null || raw.trim().isEmpty() || "None".equalsIgnoreCase(raw)) {
            return new ArrayList<>();
        }
        String cleaned = raw.replaceAll("[\\[\\]\"]", ""); // Supprime les crochets et guillemets
        if (cleaned.isEmpty()) return new ArrayList<>();
        // IMPORTANT: Si vos IDs de matériel sont séparés par des points-virgules dans la cellule du CSV,
        // remplacez la virgule par un point-virgule dans le split.
        return Arrays.stream(cleaned.split(",")) // <<-- Vérifiez ce délimiteur pour les listes internes
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @FXML
    private void handleAddCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projet/fxml/creercour-professeur.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un Nouveau Cours");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableViewEmploiDuTemps.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            CreerCourController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCours(null);

            // MODIFICATION ICI POUR GÉRER L'IOException
            controller.setEtudiantDataLoader(() -> {
                try {
                    return chargerEtudiantsDepuisCSV();
                } catch (IOException e) {
                    throw new RuntimeException("Erreur de chargement des étudiants: " + e.getMessage(), e);
                }
            });
            controller.setSalleDataLoader(() -> {
                try {
                    return chargerSallesDepuisCSV();
                } catch (IOException e) {
                    throw new RuntimeException("Erreur de chargement des salles: " + e.getMessage(), e);
                }
            });
            controller.setProchainIdCoursSupplier(() -> {
                try {
                    return genererProchainIdCours();
                } catch (IOException e) {
                    throw new RuntimeException("Erreur de génération d'ID de cours: " + e.getMessage(), e);
                }
            });

            dialogStage.showAndWait();

            Cours nouveauCours = controller.getCours();
            if (nouveauCours != null) {
                listeCoursProfesseur.add(nouveauCours);
                sauvegarderTousLesCours();
                showAlert("Succès", "Cours ajouté", "Le nouveau cours a été ajouté avec succès.");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifyCours() {
        Cours selectedCours = tableViewEmploiDuTemps.getSelectionModel().getSelectedItem();
        if (selectedCours != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projet/fxml/creercour-professeur.fxml"));
                VBox page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Modifier un Cours");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(tableViewEmploiDuTemps.getScene().getWindow());
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                CreerCourController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setCours(selectedCours);

                // MODIFICATION ICI POUR GÉRER L'IOException
                controller.setEtudiantDataLoader(() -> {
                    try {
                        return chargerEtudiantsDepuisCSV();
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur de chargement des étudiants: " + e.getMessage(), e);
                    }
                });
                controller.setSalleDataLoader(() -> {
                    try {
                        return chargerSallesDepuisCSV();
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur de chargement des salles: " + e.getMessage(), e);
                    }
                });

                dialogStage.showAndWait();

                if (controller.getCours() != null) {
                    tableViewEmploiDuTemps.refresh();
                    sauvegarderTousLesCours();
                    showAlert("Succès", "Cours modifié", "Le cours a été modifié avec succès.");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Aucun cours sélectionné", "Erreur", "Veuillez sélectionner un cours à modifier.");
        }
    }

    @FXML
    private void handleDeleteCours() {
        Cours selectedCours = tableViewEmploiDuTemps.getSelectionModel().getSelectedItem();
        if (selectedCours != null) {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation de suppression");
            confirmationAlert.setHeaderText("Supprimer le cours : " + selectedCours.getMatiere());
            confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce cours ?");
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    listeCoursProfesseur.remove(selectedCours); // Supprime de l'ObservableList
                    try {
                        sauvegarderTousLesCours(); // Sauvegarde l'intégralité de la liste (sans le cours supprimé)
                        showAlert("Succès", "Cours supprimé", "Le cours a été supprimé avec succès.");
                    } catch (IOException e) {
                        showAlert("Erreur de suppression", "Impossible de supprimer le cours du fichier", "Une erreur est survenue: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } else {
            showAlert("Aucun cours sélectionné", "Erreur", "Veuillez sélectionner un cours à supprimer.");
        }
    }

    /**
     * Sauvegarde l'intégralité de la liste `listeCoursProfesseur` dans le fichier CSV.
     * Cette méthode lira l'en-tête existante et l'ajoutera en début de fichier.
     * Si le fichier est vide, elle ajoute un en-tête par défaut.
     * @throws IOException Si une erreur d'écriture survient.
     */
    private void sauvegarderTousLesCours() throws IOException {
        List<String[]> lignesAecrire = new ArrayList<>();

        // 1. Lire l'en-tête du fichier existant ou en créer un si le fichier est vide
        List<String[]> existingRecords = CRUDcsvController.lire(COURS_CSV_PATH);
        if (!existingRecords.isEmpty()) {
            lignesAecrire.add(existingRecords.get(0)); // Ajoute l'en-tête existante
        } else {
            // Si le fichier est vide ou n'existe pas, ajoutez un en-tête par défaut
            lignesAecrire.add(new String[]{"idCours", "nomMatiere", "codeCours", "description", "duree", "enseignantId", "salleId", "horaireId", "jour", "heureDebut", "heureFin", "classe"});
        }

        // 2. Convertir les objets Cours de `listeCoursProfesseur` en tableaux de String[]
        for (Cours cours : listeCoursProfesseur) {
            String[] ligne = {
                    String.valueOf(cours.getIdCours()),
                    cours.getMatiere(),
                    cours.getCodeCours(),
                    cours.getDescription(),
                    cours.getDuree().toHours() + "h", // Convertir Duration en String (ex: "2h")
                    String.valueOf(cours.getEnseignantId()),
                    String.valueOf(cours.getSalleId()),
                    String.valueOf(cours.getHoraireId()),
                    cours.getJour(),
                    cours.getHeureDebut(),
                    cours.getHeureFin(),
                    cours.getClasse()
            };
            lignesAecrire.add(ligne);
        }

        // 3. Écrire toutes les lignes (en-tête + données) dans le fichier CSV
        CRUDcsvController.ecrire(COURS_CSV_PATH, lignesAecrire);
    }


    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}