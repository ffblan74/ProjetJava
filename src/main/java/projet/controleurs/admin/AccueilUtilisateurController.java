package projet.controleurs.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
// import javafx.scene.layout.StackPane; // Plus besoin
// import javafx.scene.Node;              // Plus besoin
// import javafx.fxml.FXMLLoader;         // Plus besoin
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Role;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AccueilUtilisateurController implements Transmissible {

    @FXML private Button btnGererUtilisateurs;
    @FXML private Button btnGererCours;
    @FXML private Button btnGererSalles;
    @FXML private Button btnGererEmploiDuTemps;

    // @FXML private StackPane contentPane; // Plus besoin

    @FXML private TableView<Utilisateur> tableViewUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colIdUtilisateur;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Void> colActions;
    @FXML private Button creerUtilisateurButton;

    @FXML private Label nomUtilisateurLabel;
    private Utilisateur utilisateurConnecte;

    private static final String CHEMIN_FICHIER_UTILISATEURS = "src/main/resources/projet/csv/utilisateurs.csv";
    private Map<Integer, String[]> donneesCSVCompletes = new HashMap<>();

    @FXML
    public void initialize() {
        if (Utilisateur.getUtilisateurConnecte() != null) {
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            afficherNomUtilisateur();
        }

        initialiserGestionUtilisateurs();
        chargerUtilisateurs();
    }

    private void initialiserGestionUtilisateurs() {
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().name()));
        ajouterBoutonsActions();
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof Utilisateur) {
            if (Utilisateur.getUtilisateurConnecte() == null || !Utilisateur.getUtilisateurConnecte().equals(data)) {
                Utilisateur.connecter((Utilisateur) data);
            }
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            afficherNomUtilisateur();
            chargerUtilisateurs();
        } else if (data instanceof String && ((String) data).equals("refresh")) {
            chargerUtilisateurs();
            afficherNomUtilisateur();
        } else {
            if (Utilisateur.getUtilisateurConnecte() != null) {
                this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
                afficherNomUtilisateur();
                chargerUtilisateurs();
            } else {
                System.err.println("Aucun utilisateur connecté transmis à AccueilAdminController, ou données inattendues.");
                NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/login.fxml", "Connexion", (Stage) nomUtilisateurLabel.getScene().getWindow(), null);
            }
        }
    }

    private void afficherNomUtilisateur() {
        if (utilisateurConnecte != null) {
            nomUtilisateurLabel.setText("Bonjour, " + utilisateurConnecte.getNom() + " " + utilisateurConnecte.getPrenom());
        } else {
            nomUtilisateurLabel.setText("Utilisateur inconnu");
        }
    }

    private void chargerUtilisateurs() {
        donneesCSVCompletes.clear();
        try {
            List<String[]> lignesUtilisateurs = CRUDcsvController.lire(CHEMIN_FICHIER_UTILISATEURS);
            ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();

            if (lignesUtilisateurs.isEmpty()) {
                tableViewUtilisateurs.setItems(FXCollections.emptyObservableList());
                return;
            }

            int startIndex = (lignesUtilisateurs.size() > 0 && lignesUtilisateurs.get(0)[0].equalsIgnoreCase("idUtilisateur")) ? 1 : 0;

            for (int i = startIndex; i < lignesUtilisateurs.size(); i++) {
                String[] ligne = lignesUtilisateurs.get(i);

                if (ligne.length >= 6) {
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        String nom = ligne[1].trim();
                        String prenom = ligne[2].trim();
                        String email = ligne[3].trim();
                        String motDePasse = ligne[4].trim();
                        Role role = Role.valueOf(ligne[5].trim().toUpperCase());

                        Utilisateur utilisateur = new Utilisateur(id, nom, prenom, email, motDePasse, role);
                        utilisateurs.add(utilisateur);
                        donneesCSVCompletes.put(id, ligne);

                    } catch (NumberFormatException e) {
                        System.err.println("Ligne CSV ignorée (ID non numérique) : " + String.join(";", ligne) + " - " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Ligne CSV ignorée (Rôle invalide) : " + String.join(";", ligne) + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Ligne CSV ignorée (pas assez de colonnes) : " + String.join(";", ligne));
                }
            }
            tableViewUtilisateurs.setItems(utilisateurs);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des utilisateurs depuis le CSV : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible de charger la liste des utilisateurs. Vérifiez le fichier CSV.");
        }
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(tc -> new TableCell<Utilisateur, Void>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnModifier, btnSupprimer);

            {
                btnModifier.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    handleModifierUtilisateur(utilisateur);
                });

                btnSupprimer.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    handleSupprimerUtilisateur(utilisateur);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleModifierUtilisateur(Utilisateur utilisateur) {
        System.out.println("Modifier utilisateur : " + utilisateur.getNom() + " (ID: " + utilisateur.getIdUtilisateur() + ")");

        String[] ligneCSVComplete = donneesCSVCompletes.get(utilisateur.getIdUtilisateur());

        if (ligneCSVComplete == null) {
            System.err.println("Erreur: Données CSV complètes introuvables pour l'utilisateur ID: " + utilisateur.getIdUtilisateur());
            NavigationUtil.afficherErreur("Erreur de données. Impossible de récupérer les informations complètes de l'utilisateur pour la modification.");
            return;
        }

        Object[] dataToTransmit = {utilisateur, ligneCSVComplete};

        try {
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/creer-utilisateur.fxml",
                    "Modifier utilisateur",
                    (Stage) tableViewUtilisateurs.getScene().getWindow(),
                    dataToTransmit
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible d'ouvrir la page de modification.");
        }
    }

    private void handleSupprimerUtilisateur(Utilisateur utilisateur) {
        System.out.println("Supprimer utilisateur : " + utilisateur.getNom() + " (ID: " + utilisateur.getIdUtilisateur() + ")");

        if (NavigationUtil.afficherConfirmation("Confirmation de suppression", "Supprimer l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom() + " ?\nCette action est irréversible.")) {
            try {
                CRUDcsvController.supprimerLigne(CHEMIN_FICHIER_UTILISATEURS, 0, String.valueOf(utilisateur.getIdUtilisateur()));
                System.out.println("Utilisateur supprimé : " + utilisateur.getIdUtilisateur());
                chargerUtilisateurs();
                NavigationUtil.afficherInformation("Succès", "L'utilisateur a été supprimé.");
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
                NavigationUtil.afficherErreur("Impossible de supprimer l'utilisateur.");
            }
        }
    }

    @FXML
    private void handleGererUtilisateurs(ActionEvent event) {
        System.out.println("Bouton 'Gérer les Utilisateurs' cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/admin/accueil-admin-gerer-utilisateur.fxml", // Le chemin vers ta nouvelle page de gestion des utilisateurs
                "Gérer les Utilisateurs",
                (Stage) btnGererUtilisateurs.getScene().getWindow(),
                utilisateurConnecte // Transmet l'utilisateur connecté à la nouvelle fenêtre
        );
    }

    @FXML
    private void handleGererCours(ActionEvent event) {
        System.out.println("Bouton 'Gérer les Cours' cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/admin/accueil-admin-gerer-cours.fxml", // Le chemin vers ta nouvelle page de gestion des cours
                "Gérer les Cours",
                (Stage) btnGererCours.getScene().getWindow(),
                utilisateurConnecte
        );
    }

    @FXML
    private void handleGererSalles(ActionEvent event) {
        System.out.println("Bouton 'Gérer les Salles' cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/admin/accueil-admin-gerer-salles.fxml", // Le chemin vers ta nouvelle page de gestion des salles
                "Gérer les Salles",
                (Stage) btnGererSalles.getScene().getWindow(),
                utilisateurConnecte
        );
    }

    @FXML
    private void handleGererEmploiDuTemps(ActionEvent event) {
        System.out.println("Bouton 'Gérer Emploi du Temps' cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/admin/accueil-admin-gerer-emploi-du-temps.fxml", // Le chemin vers ta nouvelle page de gestion de l'emploi du temps
                "Gérer Emploi du Temps",
                (Stage) btnGererEmploiDuTemps.getScene().getWindow(),
                utilisateurConnecte
        );
    }

    @FXML
    private void handleCreerUtilisateur(ActionEvent event) {
        System.out.println("Bouton 'Créer un nouvel utilisateur' cliqué !");
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/creer-utilisateur.fxml",
                "Créer un utilisateur",
                (Stage) creerUtilisateurButton.getScene().getWindow(),
                Utilisateur.getUtilisateurConnecte()
        );
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'administrateur.");
        Utilisateur.deconnecter();
        try {
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/login.fxml",
                    "Connexion",
                    (Stage) nomUtilisateurLabel.getScene().getWindow(),
                    null
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
            NavigationUtil.afficherErreur("Problème lors de la déconnexion.");
        }
    }
}