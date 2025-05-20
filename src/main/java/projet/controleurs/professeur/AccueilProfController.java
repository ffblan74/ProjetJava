package projet.controleurs.professeur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import projet.models.*;
import projet.controleurs.CRUDcsvController;
import projet.utils.NavigationUtil;


import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AccueilProfController {

    @FXML private GridPane calendarGrid;

    // Chemins des fichiers CSV
    private static final String COURS_CSV_PATH = "src/main/resources/projet/csv/cours.csv";
    private static final String UTILISATEURS_CSV_PATH = "src/main/resources/projet/csv/utilisateurs.csv";
    private static final String SALLES_CSV_PATH = "src/main/resources/projet/csv/salle.csv";

    // Données
    private List<Cours> listeCoursProfesseur = new ArrayList<>();
    private Cours coursSelectionne;

    // Mappings pour le calendrier
    private final Map<String, Integer> dayToColumnMap = Map.of(
            "LUNDI", 1, "MARDI", 2, "MERCREDI", 3,
            "JEUDI", 4, "VENDREDI", 5, "SAMEDI", 6, "DIMANCHE", 7
    );

    private final Map<Integer, Integer> hourToRowMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialiser le mapping des heures
        for (int hour = 8, row = 1; hour <= 18; hour++, row++) {
            hourToRowMap.put(hour, row);
        }

        // Charger les données
        chargerDonnees();
    }

    private void chargerDonnees() {
        Utilisateur utilisateur = Utilisateur.getUtilisateurConnecte();
        if (!(utilisateur instanceof Enseignant)) {
            NavigationUtil.afficherErreur("Erreur", "Accès refusé", "Seuls les enseignants peuvent accéder à cette page");
            return;
        }

        try {
            listeCoursProfesseur = chargerCoursPourEnseignant(((Enseignant) utilisateur).getIdEnseignant());
            afficherCalendrier();
        } catch (IOException e) {
            NavigationUtil.afficherErreur("Erreur", "Chargement des cours", e.getMessage());
        }
    }

    private void afficherCalendrier() {
        // Clear existing content
        calendarGrid.getChildren().clear();

        // Add day headers
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < jours.length; i++) {
            Label header = new Label(jours[i]);
            header.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            calendarGrid.add(header, i + 1, 0);
        }

        // Add time slots
        for (int hour = 8, row = 1; hour <= 18; hour++, row++) {
            Label timeLabel = new Label(String.format("%02dh-%02dh", hour, hour+1));
            timeLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            calendarGrid.add(timeLabel, 0, row);
        }

        // Add courses
        for (Cours cours : listeCoursProfesseur) {
            ajouterCoursAuCalendrier(cours);
        }
    }

    private void ajouterCoursAuCalendrier(Cours cours) {
        try {
            String jour = cours.getJour().toUpperCase();
            int heureDebut = Integer.parseInt(cours.getHeureDebut().split(":")[0]);

            Integer col = dayToColumnMap.get(jour);
            Integer row = hourToRowMap.get(heureDebut);

            if (col != null && row != null) {
                Label coursLabel = new Label(coursToString(cours));
                coursLabel.setStyle(getCoursStyle(cours));
                coursLabel.setOnMouseClicked(e -> selectionnerCours(cours, coursLabel));

                int rowSpan = (int) Math.max(1, cours.getDuree().toHours());
                calendarGrid.add(coursLabel, col, row, 1, rowSpan);
            }
        } catch (Exception e) {
            System.err.println("Erreur affichage cours: " + e.getMessage());
        }
    }

    private String coursToString(Cours cours) {
        return String.format("%s\nSalle: %s\n%s",
                cours.getMatiere(), cours.getSalleId(), cours.getClasse());
    }

    private String getCoursStyle(Cours cours) {
        return "-fx-background-color: #C8E6C9; -fx-border-color: #4CAF50; " +
                "-fx-padding: 5px; -fx-alignment: top-left; " +
                "-fx-max-width: Infinity; -fx-max-height: Infinity;";
    }

    private void selectionnerCours(Cours cours, Label label) {
        // Reset previous selection
        calendarGrid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                node.setStyle(node.getStyle().replace("-fx-background-color: #FFCCCB;", ""));
            }
        });

        // Set new selection
        coursSelectionne = cours;
        label.setStyle(label.getStyle() + "-fx-background-color: #FFCCCB;");
    }

    // Méthodes CRUD
    @FXML
    private void handleAddCours() {
        ouvrirFormulaireCours(null);
    }

    @FXML
    private void handleModifyCours() {
        if (coursSelectionne == null) {
            NavigationUtil.afficherErreur("Erreur","Aucun cours sélectionné", "Veuillez sélectionner un cours à modifier");
            return;
        }
        ouvrirFormulaireCours(coursSelectionne);
    }

    @FXML
    private void handleDeleteCours() {
        if (coursSelectionne == null) {
            NavigationUtil.afficherErreur("Erreur", "Aucun cours sélectionné", "Veuillez sélectionner un cours à supprimer");
            return;
        }

        if (NavigationUtil.demanderConfirmation("Supprimer cours",
                "Voulez-vous vraiment supprimer le cours " + coursSelectionne.getMatiere() + "?")) {
            try {
                listeCoursProfesseur.remove(coursSelectionne);
                sauvegarderCours();
                rafraichirCalendrier();
                NavigationUtil.afficherSucces("Succès","Cours supprimé", "Le cours a été supprimé avec succès");
                coursSelectionne = null;
            } catch (IOException e) {
                NavigationUtil.afficherErreur("Erreur", "Échec de la suppression", e.getMessage());
            }
        }
    }

    // Méthodes de données
    private List<Cours> chargerCoursPourEnseignant(int enseignantId) throws IOException {
        return chargerCoursDepuisCSV().stream()
                .filter(c -> c.getEnseignantId() == enseignantId)
                .collect(Collectors.toList());
    }

    private List<Cours> chargerCoursDepuisCSV() throws IOException {
        List<String[]> lignes = CRUDcsvController.lire(COURS_CSV_PATH);
        if (lignes.isEmpty()) return new ArrayList<>();

        int start = lignes.get(0)[0].equalsIgnoreCase("idCours") ? 1 : 0;
        return lignes.stream()
                .skip(start)
                .map(Cours::fromCsv)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Etudiant> chargerEtudiantsDepuisCSV() {
        try {
            List<String[]> lignes = CRUDcsvController.lire(UTILISATEURS_CSV_PATH);
            if (lignes.isEmpty()) return new ArrayList<>();

            int start = lignes.get(0)[0].equalsIgnoreCase("idUtilisateur") ? 1 : 0;
            return lignes.stream()
                    .skip(start)
                    .map(this::parseEtudiant)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur de chargement", "Impossible de charger les étudiants", e.getMessage());
            return new ArrayList<>();
        }
    }


    private Etudiant parseEtudiant(String[] data) {
        try {
            if (data.length >= 10 && "ETUDIANT".equalsIgnoreCase(data[5])) {
                return new Etudiant(
                        Integer.parseInt(data[0]), data[1], data[2], data[3], data[4],
                        data[8], Integer.parseInt(data[9])
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing étudiant: " + Arrays.toString(data));
        }
        return null;
    }

    private List<Salle> chargerSallesDepuisCSV() {
        try {
            List<String[]> lignes = CRUDcsvController.lire(SALLES_CSV_PATH);
            if (lignes.isEmpty()) return new ArrayList<>();

            int start = lignes.get(0)[0].equalsIgnoreCase("idSalle") ? 1 : 0;
            return lignes.stream()
                    .skip(start)
                    .map(this::parseSalle)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur de chargement", "Impossible de charger les salles", e.getMessage());
            return new ArrayList<>();
        }
    }


    private Salle parseSalle(String[] data) {
        try {
            if (data.length >= 5) {
                return new Salle(
                        Integer.parseInt(data[0]), data[1],
                        Integer.parseInt(data[2]), data[3],
                        Arrays.stream(data[4].replaceAll("[\\[\\] ]", "").split(","))
                                .filter(s -> !s.isEmpty())
                                .map(Integer::parseInt)
                                .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing salle: " + Arrays.toString(data));
        }
        return null;
    }

    private int genererNouvelIdCours() {
        try {
            return chargerCoursDepuisCSV().stream()
                    .mapToInt(Cours::getIdCours)
                    .max()
                    .orElse(0) + 1;
        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur de génération d'ID", "Impossible de générer un nouvel ID de cours", e.getMessage());
            return 1; // Valeur par défaut si erreur
        }
    }


    private void sauvegarderCours() throws IOException {
        List<String[]> lignes = new ArrayList<>();
        lignes.add(new String[]{"idCours", "nomMatiere", "codeCours", "description", "duree",
                "enseignantId", "salleId", "horaireId", "jour", "heureDebut",
                "heureFin", "classe"});

        listeCoursProfesseur.stream()
                .map(this::coursToCsvLine)
                .forEach(lignes::add);

        CRUDcsvController.ecrire(COURS_CSV_PATH, lignes);
    }
    private void ouvrirFormulaireCours(Cours cours) {
        Stage stage = new Stage();
        stage.setTitle(cours == null ? "Ajouter un cours" : "Modifier un cours");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(calendarGrid.getScene().getWindow());

        NavigationUtil.initialiserEtAfficherCreerCour(stage, cours,
                this::chargerEtudiantsDepuisCSV,
                this::chargerSallesDepuisCSV,
                this::genererNouvelIdCours);

        if (NavigationUtil.estSauvegarde()) {
            try {
                sauvegarderCours();
                rafraichirCalendrier();
                NavigationUtil.afficherSucces("Succès", "", "Les modifications ont été enregistrées");
            } catch (IOException e) {
                NavigationUtil.afficherErreur("Erreur", "Erreur lors de la sauvegarde du cours", e.getMessage());
            }
        }
    }

    private String[] coursToCsvLine(Cours cours) {
        return new String[]{
                String.valueOf(cours.getIdCours()),
                cours.getMatiere(),
                cours.getCodeCours(),
                cours.getDescription(),
                formatDuree(cours.getDuree()),
                String.valueOf(cours.getEnseignantId()),
                String.valueOf(cours.getSalleId()),
                String.valueOf(cours.getHoraireId()),
                cours.getJour(),
                cours.getHeureDebut(),
                cours.getHeureFin(),
                cours.getClasse()
        };
    }

    private String formatDuree(Duration duree) {
        if (duree == null) return "0h";
        long h = duree.toHours();
        long m = duree.toMinutes() % 60;
        return m == 0 ? h + "h" : h + "h" + m + "min";
    }

    private void rafraichirCalendrier() {
        try {
            Utilisateur user = Utilisateur.getUtilisateurConnecte();
            if (user instanceof Enseignant) {
                listeCoursProfesseur = chargerCoursPourEnseignant(((Enseignant) user).getIdEnseignant());
                afficherCalendrier();
            }
        } catch (IOException e) {
            NavigationUtil.afficherErreur("Erreur", "Rafraîchissement", e.getMessage());
        }
    }
}