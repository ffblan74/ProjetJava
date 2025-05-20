package projet.controleurs.eleve;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn; // Assurez-vous d'importer TableColumn
import javafx.scene.control.cell.PropertyValueFactory; // Assurez-vous d'importer PropertyValueFactory
import javafx.stage.Stage;

import projet.controleurs.CRUDcsvController;
import projet.models.Cours;
import projet.models.Utilisateur;
import projet.models.Role; // IMPORTER LA CLASSE ENUM ROLE
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;
import projet.utils.TransmissibleStage;

import java.io.IOException;
import java.time.Duration; // Import si Cours utilise Duration
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Implémente Transmissible et TransmissibleStage pour la transmission de données et du Stage
public class AccueilEleveController implements Transmissible, TransmissibleStage {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<Cours> emploiDuTempsTableView;

    // Déclaration des colonnes de la TableView (si non définies dans FXML avec fx:id et contrôleur)
    // C'est une bonne pratique de les injecter par FXML via fx:id si elles y sont définies.
    // Si elles ne sont pas dans FXML, vous pouvez les créer ici.
    @FXML private TableColumn<Cours, String> matiereColumn;
    @FXML private TableColumn<Cours, String> enseignantColumn;
    @FXML private TableColumn<Cours, String> salleColumn;
    @FXML private TableColumn<Cours, String> jourColumn;
    @FXML private TableColumn<Cours, String> heureDebutColumn;
    @FXML private TableColumn<Cours, String> heureFinColumn;
    // Assurez-vous que ces fx:id correspondent à ceux de votre FXML.

    private Utilisateur utilisateurConnecte;
    private ObservableList<Cours> coursEleve = FXCollections.observableArrayList();

    private Stage stageCourante; // Attribut pour stocker la Stage de cette fenêtre

    // Implémentation de la méthode de l'interface TransmissibleStage
    @Override
    public void setStage(Stage stage) {
        this.stageCourante = stage;
        System.out.println("Stage de AccueilEleveController définie via setStage.");
    }

    // La méthode initialize est appelée par le FXMLLoader après l'injection des FXML
    @FXML
    public void initialize() {
        // Initialiser les CellValueFactory pour chaque colonne.
        // Cela lie les colonnes aux propriétés de l'objet Cours.
        if (matiereColumn != null) matiereColumn.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        if (enseignantColumn != null) enseignantColumn.setCellValueFactory(new PropertyValueFactory<>("enseignantNomComplet"));
        if (salleColumn != null) salleColumn.setCellValueFactory(new PropertyValueFactory<>("salle"));
        if (jourColumn != null) jourColumn.setCellValueFactory(new PropertyValueFactory<>("jour"));
        if (heureDebutColumn != null) heureDebutColumn.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        if (heureFinColumn != null) heureFinColumn.setCellValueFactory(new PropertyValueFactory<>("heureFin"));

        emploiDuTempsTableView.setItems(coursEleve);
        System.out.println("AccueilEleveController initialize method called.");
    }

    // Cette méthode est appelée par NavigationUtil pour transmettre des données (l'utilisateur connecté).
    @Override
    public void transmettreDonnees(Object data) {
        System.out.println("transmettreDonnees appelée dans AccueilEleveController. Type de données: " + (data != null ? data.getClass().getName() : "null"));
        if (data instanceof Utilisateur) {
            this.utilisateurConnecte = (Utilisateur) data;
            System.out.println("Utilisateur transmis à AccueilEleveController: " + utilisateurConnecte.getNom() + " (Rôle: " + utilisateurConnecte.getRole() + ")");
            initialiserContenuPage(); // Appeler la méthode d'initialisation après avoir reçu l'utilisateur
        } else {
            System.err.println("ERREUR: Données transmises non de type Utilisateur à AccueilEleveController.");
            NavigationUtil.afficherErreur("Erreur Interne", "Données Utilisateur Manquantes", "Erreur: Les données utilisateur n'ont pas pu être transmises correctement.");
            // Rediriger vers la page de connexion en cas d'erreur de transmission de données
            if (this.stageCourante != null) {
                NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", this.stageCourante, null);
            } else {
                NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", null, null);
            }
        }
    }

    // Initialise le contenu de la page en fonction de l'utilisateur connecté
    private void initialiserContenuPage() {
        System.out.println("initialiserContenuPage appelée dans AccueilEleveController.");

        // COMPARISON DU RÔLE CORRIGÉE : Utilise l'opérateur == avec l'énumération Role
        if (utilisateurConnecte != null && utilisateurConnecte.getRole() == Role.ETUDIANT) {
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue, " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());
            } else {
                System.err.println("AVERTISSEMENT: welcomeLabel est null dans initialiserContenuPage. Vérifiez votre FXML.");
            }
            chargerEmploiDuTempsEleve();
        } else {
            // Cette partie est atteinte si l'utilisateur n'est PAS un ETUDIANT
            NavigationUtil.afficherErreur("Accès non autorisé", "Erreur d'accès", "Vous n'êtes pas connecté en tant qu'étudiant.");

            System.err.println("Condition d'accès non remplie: utilisateurConnecte est null ou n'est pas un ETUDIANT.");
            if (utilisateurConnecte != null) {
                System.err.println("Rôle de l'utilisateur connecté: " + utilisateurConnecte.getRole());
            } else {
                System.err.println("L'objet utilisateurConnecte est null.");
            }

            // Rediriger vers la page de connexion
            if (this.stageCourante != null) {
                NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", this.stageCourante, null);
            } else {
                NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", null, null);
            }
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion de l'utilisateur: " + utilisateurConnecte.getNom());
        Utilisateur.deconnecter(); // Assurez-vous que cette méthode déconnecte bien l'utilisateur globalement.

        // Utilisez this.stageCourante pour la redirection
        if (this.stageCourante != null) {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", this.stageCourante, null);
        } else {
            NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", null, null);
        }
    }

    private void chargerEmploiDuTempsEleve() {
        coursEleve.clear(); // Vider la liste avant de recharger
        System.out.println("Tentative de chargement de l'emploi du temps pour l'étudiant ID: " + utilisateurConnecte.getIdUtilisateur());

        try {
            List<String[]> emploisDuTempsCsv = CRUDcsvController.lire("src/main/resources/projet/csv/emplois_du_temps.csv"); // Nom du fichier : emplois_du_temps.csv
            Optional<String[]> etEmploiDuTemps = emploisDuTempsCsv.stream()
                    .skip(1)
                    .filter(line -> line.length > 1 && line[1].trim().equals(String.valueOf(utilisateurConnecte.getIdUtilisateur())))
                    .findFirst();

            if (etEmploiDuTemps.isPresent()) {
                String[] emploiDuTempsData = etEmploiDuTemps.get();
                if (emploiDuTempsData.length > 2 && !emploiDuTempsData[2].trim().isEmpty() && !emploiDuTempsData[2].trim().equalsIgnoreCase("None")) {
                    List<Integer> coursIds = Arrays.stream(emploiDuTempsData[2].trim().split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    System.out.println("Cours IDs trouvés pour l'étudiant: " + coursIds);

                    List<String[]> allCoursCsv = CRUDcsvController.lire("src/main/resources/projet/csv/cours.csv");
                    List<Cours> allCours = allCoursCsv.stream()
                            .skip(1)
                            .map(line -> {
                                try {
                                    // Assurez-vous que le nombre de colonnes est suffisant avant d'accéder aux index
                                    // Adaptez ces index et le constructeur de Cours à votre `cours.csv`
                                    if (line.length < 12) { // Exemple: si vous attendez 12 colonnes pour un cours
                                        System.err.println("Ligne CSV cours invalide (trop peu de colonnes): " + Arrays.toString(line));
                                        return null;
                                    }
                                    int idCours = Integer.parseInt(line[0].trim());
                                    String nomMatiere = line[1].trim();
                                    String codeCours = line[2].trim();
                                    String description = line[3].trim();
                                    Duration duree = parseDuration(line[4].trim()); // Méthode parseDuration à adapter
                                    int enseignantId = Integer.parseInt(line[5].trim());
                                    int salleId = Integer.parseInt(line[6].trim());
                                    int horaireId = Integer.parseInt(line[7].trim());
                                    String jour = line[8].trim(); // Si ces champs sont directement dans cours.csv
                                    String heureDebut = line[9].trim();
                                    String heureFin = line[10].trim();
                                    String classe = line[11].trim();

                                    return new Cours(idCours, nomMatiere, codeCours, description, duree, enseignantId, salleId, horaireId, jour, heureDebut, heureFin, classe);

                                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                    System.err.println("Erreur lors du parsing d'une ligne de cours (format ou index): " + Arrays.toString(line) + " - " + e.getMessage());
                                    return null;
                                }
                            })
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList());
                    System.out.println("Nombre total de cours lus: " + allCours.size());

                    List<Cours> filteredCours = allCours.stream()
                            .filter(c -> coursIds.contains(c.getIdCours()))
                            .collect(Collectors.toList());
                    System.out.println("Nombre de cours assignés à cet étudiant: " + filteredCours.size());

                    List<String[]> allUtilisateursCsv = CRUDcsvController.lire("src/main/resources/projet/csv/utilisateurs.csv");
                    List<String[]> allHorairesCsv = CRUDcsvController.lire("src/main/resources/projet/csv/horaire.csv"); // Nom du fichier : horaire.csv
                    List<String[]> allSallesCsv = CRUDcsvController.lire("src/main/resources/projet/csv/salle.csv"); // Nom du fichier : salle.csv

                    for (Cours cours : filteredCours) {
                        // Chercher l'enseignant
                        allUtilisateursCsv.stream()
                                .skip(1)
                                .filter(u -> u.length > 7 && u[7].trim().equals(String.valueOf(cours.getEnseignantId())) && Role.ENSEIGNANT.name().equalsIgnoreCase(u[5].trim())) // Vérifier rôle enseignant à l'index 5
                                .findFirst()
                                .ifPresent(u -> {
                                    if (u.length > 2) {
                                        cours.setEnseignantNomComplet(u[1].trim() + " " + u[2].trim());
                                    } else {
                                        cours.setEnseignantNomComplet("Enseignant inconnu");
                                    }
                                });

                        // Chercher l'horaire
                        allHorairesCsv.stream()
                                .skip(1)
                                .filter(h -> h.length > 0 && h[0].trim().equals(String.valueOf(cours.getHoraireId())))
                                .findFirst()
                                .ifPresent(h -> {
                                    if (h.length > 3) {
                                        cours.setJour(h[1].trim());
                                        cours.setHeureDebut(h[2].trim());
                                        cours.setHeureFin(h[3].trim());
                                    } else {
                                        cours.setJour("N/A");
                                        cours.setHeureDebut("N/A");
                                        cours.setHeureFin("N/A");
                                    }
                                });

                        // Chercher la salle
                        allSallesCsv.stream()
                                .skip(1)
                                .filter(s -> s.length > 0 && s[0].trim().equals(String.valueOf(cours.getSalleId())))
                                .findFirst()
                                .ifPresent(s -> {
                                    if (s.length > 1) {
                                        cours.setSalle(s[1].trim());
                                    } else {
                                        cours.setSalle("Salle N/A");
                                    }
                                });
                        System.out.println("Cours enrichi: " + cours.getMatiere() + " - Enseignant: " + cours.getEnseignantNomComplet() + " - Salle: " + cours.getSalle() + " - Jour: " + cours.getJour());
                    }

                    coursEleve.addAll(filteredCours);
                } else {
                    System.out.println("L'étudiant ID: " + utilisateurConnecte.getIdUtilisateur() + " n'a pas de cours assignés ou la colonne coursIds est vide/invalide.");
                }
            } else {
                System.out.println("Aucun emploi du temps trouvé pour l'étudiant ID: " + utilisateurConnecte.getIdUtilisateur());
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture des fichiers CSV : " + e.getMessage());
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur de Fichier", "Impossible de charger l'emploi du temps.", "Vérifiez l'intégrité de vos fichiers CSV.");
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique lors du parsing d'un ID ou autre: " + e.getMessage());
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur de Données", "Format numérique invalide.", "Un ID ou une valeur numérique dans un fichier CSV n'est pas au bon format.");
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors du chargement de l'emploi du temps: " + e.getMessage());
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur Inattendue", "Problème lors du chargement.", "Une erreur inattendue est survenue lors du chargement de l'emploi du temps.");
        }
    }

    // Méthode utilitaire pour parser la durée (si nécessaire)
    private Duration parseDuration(String durationString) {
        if (durationString != null && !durationString.isEmpty()) {
            try {
                // Si la durée est stockée comme "2h", "30min", etc.
                if (durationString.toLowerCase().endsWith("h")) {
                    return Duration.ofHours(Long.parseLong(durationString.toLowerCase().replace("h", "").trim()));
                } else if (durationString.toLowerCase().endsWith("min")) {
                    return Duration.ofMinutes(Long.parseLong(durationString.toLowerCase().replace("min", "").trim()));
                } else {
                    // Supposons que c'est en heures par défaut si aucun suffixe n'est trouvé
                    return Duration.ofHours(Long.parseLong(durationString.trim()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format de durée (non numérique ou format inconnu): " + durationString);
            }
        }
        return Duration.ofHours(0); // Valeur par défaut en cas d'erreur
    }
}