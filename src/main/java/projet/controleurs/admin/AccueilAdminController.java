package projet.controleurs.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback; // Important pour CellFactory
import projet.controleurs.CRUDcsvController;
import projet.models.Role;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.List;
import java.util.Optional; // Pour les boîtes de dialogue

public class AccueilAdminController implements Transmissible {

    @FXML private Button creerCoursButton;
    @FXML private Button modifierEmploiDuTempsButton;
    @FXML private Button gererCreneauxButton;
    @FXML private Button affecterEnseignantsButton;
    @FXML private Button gererSallesButton;
    @FXML private Button controlerConflitsButton;
    @FXML private Button statistiquesSallesButton;
    @FXML private Button statistiquesEnseignantsButton;
    @FXML private Button gererUtilisateursButton;
    @FXML private TableView<Utilisateur> tableViewUtilisateurs;

    // Déclaration des nouvelles colonnes
    @FXML private TableColumn<Utilisateur, Integer> colIdUtilisateur; // ID
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail; // Ajout de la colonne email
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Void> colActions; // Colonne pour les boutons d'action

    @FXML private Label nomUtilisateurLabel;
    private Utilisateur utilisateurConnecte;

    // Constante pour le chemin du fichier CSV
    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur")); // Pour l'ID
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); // Pour l'Email
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().name()));

        // Configuration de la colonne d'actions (boutons Modifier/Supprimer)
        ajouterBoutonsActions();

        // Charger les utilisateurs au démarrage
        chargerUtilisateurs();
    }

    // Méthode pour charger les utilisateurs depuis le CSV et remplir la TableView
    private void chargerUtilisateurs() {
        try {
            // Récupérer toutes les lignes du fichier CSV
            List<String[]> lignesUtilisateurs = CRUDcsvController.lire(CHEMIN_FICHIER_UTILISATEURS);

            // Liste observable pour le TableView
            ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();

            // Sauter l'en-tête (première ligne)
            for (int i = 1; i < lignesUtilisateurs.size(); i++) {
                String[] ligne = lignesUtilisateurs.get(i);

                // Vérifier que la ligne a au moins 6 colonnes (ID, Nom, Prénom, Email, MDP, Rôle)
                if (ligne.length >= 6) {
                    // Création d'un nouvel utilisateur à partir des données CSV
                    int id = Integer.parseInt(ligne[0].trim());
                    String nom = ligne[1].trim();
                    String prenom = ligne[2].trim();
                    String email = ligne[3].trim();
                    String motDePasse = ligne[4].trim();
                    Role role = Role.valueOf(ligne[5].trim()); // Convertir String en Enum Role

                    utilisateurs.add(new Utilisateur(id, nom, prenom, email, motDePasse, role));
                }
            }

            // Définir les éléments dans le TableView
            tableViewUtilisateurs.setItems(utilisateurs);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des utilisateurs depuis le CSV : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible de charger la liste des utilisateurs. Vérifiez le fichier CSV.");
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format de nombre (ID) dans le CSV : " + e.getMessage());
            NavigationUtil.afficherErreur("Erreur de formatage d'ID dans le fichier utilisateurs.csv.");
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de conversion de rôle dans le CSV : " + e.getMessage());
            NavigationUtil.afficherErreur("Erreur dans la définition des rôles utilisateurs dans le fichier CSV.");
        }
    }

    // Méthode pour ajouter les boutons Modifier et Supprimer à la colonne "Actions"
    private void ajouterBoutonsActions() {
        colActions.setCellFactory(new Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>>() {
            @Override
            public TableCell<Utilisateur, Void> call(final TableColumn<Utilisateur, Void> param) {
                final TableCell<Utilisateur, Void> cell = new TableCell<Utilisateur, Void>() {

                    private final Button btnModifier = new Button("Modifier");
                    private final Button btnSupprimer = new Button("Supprimer");

                    {
                        btnModifier.setOnAction((ActionEvent event) -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            handleModifierUtilisateur(utilisateur);
                        });

                        btnSupprimer.setOnAction((ActionEvent event) -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            handleSupprimerUtilisateur(utilisateur);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox pane = new HBox(5, btnModifier, btnSupprimer); // Espacement de 5px
                            setGraphic(pane);
                        }
                    }
                };
                return cell;
            }
        });
    }

    // --- Gestionnaires d'événements pour les boutons d'action ---

    // Gère la modification d'un utilisateur
    private void handleModifierUtilisateur(Utilisateur utilisateur) {
        System.out.println("Modifier utilisateur : " + utilisateur.getNom() + " (ID: " + utilisateur.getIdUtilisateur() + ")");
        // Implémente la logique de modification ici.
        // Cela pourrait impliquer d'ouvrir une nouvelle fenêtre avec un formulaire pré-rempli.
        try {
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/creer-utilisateur.fxml", // Tu peux réutiliser ce FXML pour la modification
                    "Modifier utilisateur",
                    (Stage) tableViewUtilisateurs.getScene().getWindow(),
                    utilisateur // Transmet l'utilisateur à modifier
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible d'ouvrir la page de modification.");
        }
    }

    // Gère la suppression d'un utilisateur
    private void handleSupprimerUtilisateur(Utilisateur utilisateur) {
        System.out.println("Supprimer utilisateur : " + utilisateur.getNom() + " (ID: " + utilisateur.getIdUtilisateur() + ")");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom() + " ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Utilise l'ID comme valeur unique pour la suppression
                CRUDcsvController.supprimerLigne(CHEMIN_FICHIER_UTILISATEURS, 0, String.valueOf(utilisateur.getIdUtilisateur()));
                System.out.println("Utilisateur supprimé : " + utilisateur.getIdUtilisateur());
                chargerUtilisateurs(); // Recharge le tableau pour mettre à jour l'affichage
                NavigationUtil.afficherInformation("Succès", "L'utilisateur a été supprimé.");
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
                NavigationUtil.afficherErreur("Impossible de supprimer l'utilisateur.");
            }
        }
    }

    // Méthode de transmission de données pour l'utilisateur connecté
    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {

            if (Utilisateur.getUtilisateurConnecte() == null) {
                Utilisateur.connecter((Utilisateur) data);
            }
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            afficherNomUtilisateur();
        } else if (data instanceof String && ((String) data).equals("refresh")) {
            chargerUtilisateurs();
            afficherNomUtilisateur(); // Assure la mise à jour du label
        }
    }

    private void afficherNomUtilisateur() {
        if (utilisateurConnecte != null) {
            nomUtilisateurLabel.setText("Bonjour, " + utilisateurConnecte.getNom() + " " + utilisateurConnecte.getPrenom());
        } else {
            nomUtilisateurLabel.setText("Utilisateur inconnu");
        }
    }

    // --- Gestionnaires d'événements pour les autres boutons de l'interface (restent les mêmes) ---

    @FXML
    private void handleCreerCours(ActionEvent event) {
        System.out.println("Bouton Créer un cours cliqué !");
        // Exemple d'ouverture d'une nouvelle fenêtre pour créer un cours :
        // NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/creer-cours.fxml", "Créer un cours", (Stage) creerCoursButton.getScene().getWindow(), utilisateurConnecte);
    }

    @FXML
    private void handleModifierEmploiDuTemps(ActionEvent event) {
        System.out.println("Bouton Modifier l'emploi du temps cliqué !");
    }

    @FXML
    private void handleGererCreneaux(ActionEvent event) {
        System.out.println("Bouton Gérer les créneaux cliqué !");
    }

    @FXML
    private void handleAffecterEnseignants(ActionEvent event) {
        System.out.println("Bouton Affecter des enseignants cliqué !");
    }

    @FXML
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Bouton Gérer les salles cliqué !");
    }


    @FXML
    private void handleStatistiquesSalles(ActionEvent event) {
        System.out.println("Bouton Statistiques des salles cliqué !");
    }

    @FXML
    private void handleStatistiquesEnseignants(ActionEvent event) {
        System.out.println("Bouton Statistiques enseignants cliqué !");
    }

    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        System.out.println("Bouton Créer Utilisateur cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/creer-utilisateur.fxml",
                "Créer un utilisateur",
                (Stage) gererUtilisateursButton.getScene().getWindow(),
                utilisateurConnecte
        );
    }
}