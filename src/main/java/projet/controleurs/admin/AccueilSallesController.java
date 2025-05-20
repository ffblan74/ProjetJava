package projet.controleurs.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Salle;
// import projet.models.Utilisateur; // Pas directement nécessaire ici, mais peut l'être si la vue spécifique a besoin des données utilisateur
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AccueilSallesController implements Transmissible {

    @FXML private TableView<Salle> tableViewSalles;
    @FXML private TableColumn<Salle, Integer> colIdSalle;
    @FXML private TableColumn<Salle, String> colNumeroSalle;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colLocalisation;
    @FXML private TableColumn<Salle, String> colMateriel;
    @FXML private TableColumn<Salle, Void> colActions;
    @FXML private Button creerSalleButton;
    @FXML private TextField champRecherche;

    private Map<Integer, String[]> donneesCSVCompletesSalles = new HashMap<>();
    private static final String CHEMIN_FICHIER_SALLES = "src/main/resources/projet/csv/salle.csv";
    private ObservableList<Salle> listeSalles;

    @FXML
    public void initialize() {
        colIdSalle.setCellValueFactory(new PropertyValueFactory<>("idSalle"));
        colNumeroSalle.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        colMateriel.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMaterielsNomsAffichables())
        );

        ajouterBoutonsActions();
        chargerSalles();
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data instanceof String && ((String) data).equals("refresh")) {
            chargerSalles();
        }
        // Si AccueilSallesController avait besoin de l'objet Utilisateur
        // else if (data instanceof Utilisateur) {
        //     // Faire quelque chose avec l'utilisateur si nécessaire pour cette vue spécifique
        // }
    }


    private void chargerSalles() {
        donneesCSVCompletesSalles.clear(); // Videz la map avant de la remplir
        try {
            List<String[]> lignesSalles = CRUDcsvController.lire(CHEMIN_FICHIER_SALLES); // Utilisez le bon chemin CSV pour les salles

            if (lignesSalles.isEmpty()) {
                // Gérez le cas où le fichier est vide
                return;
            }

            // Pour ignorer l'en-tête (si "idSalle" est la première colonne)
            int startIndex = (lignesSalles.size() > 0 && lignesSalles.get(0)[0].equalsIgnoreCase("idSalle")) ? 1 : 0;


            for (int i = startIndex; i < lignesSalles.size(); i++) {
                String[] ligne = lignesSalles.get(i);

                // Assurez-vous que la ligne a suffisamment de colonnes pour une Salle
                if (ligne.length >= 6) { // id, numero, capacite, localisation, materielNom, materielsDescription
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        String numeroSalle = ligne[1].trim();
                        int capacite = Integer.parseInt(ligne[2].trim());
                        String localisation = ligne[3].trim();
                        // Pour les listes, utilisez le constructeur de Salle qui prend des chaînes
                        String materielNomCsv = ligne[4].trim();
                        String materielsDescriptionCsv = ligne[5].trim();

                        // Créez l'objet Salle en utilisant le constructeur adapté aux données CSV
                        Salle salle = new Salle(id, numeroSalle, capacite, localisation, materielNomCsv, materielsDescriptionCsv);

                        // Ajoutez la salle à l'ObservableList de votre TableView
                        // tableViewSalles.getItems().add(salle); // Ceci doit être fait une seule fois à la fin

                        // SAUVEGARDEZ LA LIGNE CSV COMPLÈTE
                        donneesCSVCompletesSalles.put(id, ligne); // <-- C'est CRUCIAL
                    } catch (NumberFormatException e) {
                        System.err.println("Ligne CSV salle ignorée (numérique invalide) : " + String.join(";", ligne) + " - " + e.getMessage());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println("Ligne CSV salle ignorée (colonnes manquantes) : " + String.join(";", ligne) + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Ligne CSV salle ignorée (pas assez de colonnes) : " + String.join(";", ligne));
                }
            }
            // Mettre à jour la TableView (exemple)
            // tableViewSalles.setItems(FXCollections.observableArrayList(salles)); // Assurez-vous de mettre à jour votre TableView ici
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des salles depuis le CSV : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible de charger la liste des salles. Vérifiez le fichier CSV.");
        }
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(tc -> new TableCell<Salle, Void>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnModifier, btnSupprimer);

            {
                btnModifier.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    handleModifierSalle(salle);
                });

                btnSupprimer.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    handleSupprimerSalle(salle);
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

    @FXML
    private void handleCreerSalle(ActionEvent event) {
        System.out.println("Créer une nouvelle salle.");
        Stage stageActuel = (Stage) creerSalleButton.getScene().getWindow();
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/creer-modifier-salle.fxml", "Créer une salle", stageActuel, null);
    }

    // Dans AccueilSallesController (la méthode appelée par le bouton "Modifier" dans la colonne d'action)
    private void handleModifierSalle(Salle salle) { // 'salle' est la Salle sélectionnée dans le tableau
        System.out.println("Modifier salle : " + salle.getNumeroSalle() + " (ID: " + salle.getIdSalle() + ")");

        // Récupère la ligne CSV complète associée à cette salle depuis la Map
        String[] ligneCSVComplete = donneesCSVCompletesSalles.get(salle.getIdSalle());

        if (ligneCSVComplete == null) {
            System.err.println("Erreur: Données CSV complètes introuvables pour la salle ID: " + salle.getIdSalle());
            NavigationUtil.afficherErreur("Erreur interne: Données de salle introuvables pour la modification.");
            return;
        }


        Object[] dataToTransmit = {salle, ligneCSVComplete};

        try {
            NavigationUtil.ouvrirNouvelleFenetre(
                    "/projet/fxml/creer-modifier-salle.fxml",
                    "Modifier une Salle",
                    (Stage) tableViewSalles.getScene().getWindow(), // Ou un autre élément pour obtenir le stage
                    dataToTransmit
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre de modification de salle : " + e.getMessage());
            e.printStackTrace();
            NavigationUtil.afficherErreur("Impossible d'ouvrir la page de modification de salle.");
        }
    }

    private void handleSupprimerSalle(Salle salle) {
        System.out.println("Supprimer salle : " + salle.getNumeroSalle());
        if (NavigationUtil.afficherConfirmation("Confirmation de suppression", "Supprimer la salle " + salle.getNumeroSalle() + " ?\nCette action est irréversible.")) {
            try {
                CRUDcsvController.supprimerLigne(CHEMIN_FICHIER_SALLES, 0, String.valueOf(salle.getIdSalle()));
                chargerSalles();
                NavigationUtil.afficherInformation("Succès", "La salle a été supprimée.");
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression de la salle : " + e.getMessage());
                NavigationUtil.afficherErreur("Impossible de supprimer la salle.");
            }
        }
    }

    @FXML
    private void handleRechercherSalle(ActionEvent event) {
        String texteRecherche = champRecherche.getText().toLowerCase().trim();
        if (texteRecherche.isEmpty()) {
            tableViewSalles.setItems(listeSalles);
            return;
        }

        ObservableList<Salle> sallesFiltrees = FXCollections.observableArrayList();
        for (Salle salle : listeSalles) {
            if (salle.getNumeroSalle().toLowerCase().contains(texteRecherche) ||
                    salle.getLocalisation().toLowerCase().contains(texteRecherche) ||
                    salle.getMaterielNom().stream().anyMatch(n -> n.toLowerCase().contains(texteRecherche)) ||
                    salle.getMaterielsDescription().stream().anyMatch(d -> d.toLowerCase().contains(texteRecherche))) {
                sallesFiltrees.add(salle);
            }
        }
        tableViewSalles.setItems(sallesFiltrees);
    }

    @FXML
    private void handleActualiserTableau(ActionEvent event) {
        champRecherche.clear();
        chargerSalles();
    }
}