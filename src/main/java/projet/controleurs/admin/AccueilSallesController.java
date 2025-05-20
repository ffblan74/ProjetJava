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
import projet.models.Materiel;
import projet.models.Salle;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AccueilSallesController implements Transmissible {

    @FXML private TableView<Salle> tableViewSalles;
    @FXML private TableColumn<Salle, Integer> colIdSalle;
    @FXML private TableColumn<Salle, String> colNumeroSalle;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colLocalisation;
    @FXML private TableColumn<Salle, String> colMateriel; // Pour afficher le matériel (nom/description)
    @FXML private TableColumn<Salle, Void> colActions;
    @FXML private Button creerSalleButton;
    @FXML private TextField champRecherche;

    private static final String CHEMIN_FICHIER_SALLES = "src/main/resources/projet/csv/salle.csv";
    private static final String CHEMIN_FICHIER_MATERIEL = "src/main/resources/projet/csv/materiel.csv"; // Si tu as un fichier matériel séparé
    private ObservableList<Salle> listeSalles;
    private List<Materiel> listeMaterielDisponible; // Pour stocker tout le matériel disponible

    @FXML
    public void initialize() {
        colIdSalle.setCellValueFactory(new PropertyValueFactory<>("idSalle"));
        colNumeroSalle.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        // Pour la colonne Matériel, on doit mapper une liste d'IDs à une chaîne affichable
        colMateriel.setCellValueFactory(cellData -> {
            List<Integer> idsMateriel = cellData.getValue().getMaterielIds();
            String materielText = obtenirNomsMateriel(idsMateriel);
            return new SimpleStringProperty(materielText);
        });

        ajouterBoutonsActions();
        chargerMaterielDisponible(); // Charge le matériel une seule fois
        chargerSalles();
    }

    @Override
    public void transmettreDonnees(Object data) {
        // Cette méthode peut être utilisée pour rafraîchir le tableau
        // ou transmettre un utilisateur connecté si nécessaire.
        if (data instanceof String && ((String) data).equals("refresh")) {
            chargerSalles();
        }
        // else if (data instanceof Utilisateur) {
        //     // Gérer l'utilisateur connecté si cette vue en a besoin
        // }
    }

    private void chargerMaterielDisponible() {
        listeMaterielDisponible = new ArrayList<>();
        try {
            List<String[]> lignesMateriel = CRUDcsvController.lire(CHEMIN_FICHIER_MATERIEL);
            int startIndex = (lignesMateriel.size() > 0 && lignesMateriel.get(0)[0].equalsIgnoreCase("idMateriel")) ? 1 : 0;
            for (int i = startIndex; i < lignesMateriel.size(); i++) {
                String[] ligne = lignesMateriel.get(i);
                if (ligne.length >= 3) {
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        String nom = ligne[1].trim();
                        String description = ligne[2].trim();
                        listeMaterielDisponible.add(new Materiel(id, nom, description));
                    } catch (NumberFormatException e) {
                        System.err.println("Ligne matériel ignorée (ID non numérique) : " + String.join(";", ligne) + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du matériel : " + e.getMessage());
            NavigationUtil.afficherErreur("Impossible de charger la liste du matériel. Vérifiez le fichier CSV.");
        }
    }

    private String obtenirNomsMateriel(List<Integer> idsMateriel) {
        if (idsMateriel == null || idsMateriel.isEmpty()) {
            return "Aucun";
        }
        return idsMateriel.stream()
                .map(id -> listeMaterielDisponible.stream()
                        .filter(m -> m.getIdMateriel() == id)
                        .map(Materiel::getNom)
                        .findFirst()
                        .orElse("Inconnu"))
                .collect(Collectors.joining(", "));
    }

    private void chargerSalles() {
        try {
            List<String[]> lignesSalles = CRUDcsvController.lire(CHEMIN_FICHIER_SALLES);
            listeSalles = FXCollections.observableArrayList();

            if (lignesSalles.isEmpty()) {
                tableViewSalles.setItems(FXCollections.emptyObservableList());
                return;
            }

            int startIndex = (lignesSalles.size() > 0 && lignesSalles.get(0)[0].equalsIgnoreCase("idSalle")) ? 1 : 0;

            for (int i = startIndex; i < lignesSalles.size(); i++) {
                String[] ligne = lignesSalles.get(i);
                if (ligne.length >= 4) { // Au moins id, numero, capacite, localisation
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        String numero = ligne[1].trim();
                        int capacite = Integer.parseInt(ligne[2].trim());
                        String localisation = ligne[3].trim();

                        List<Integer> materielIds = new ArrayList<>();
                        if (ligne.length > 4 && !ligne[4].trim().isEmpty()) {
                            // Le matériel est une chaîne de IDs séparés par des virgules (ex: "1,2,3")
                            String materielString = ligne[4].trim();
                            Arrays.stream(materielString.split(","))
                                    .filter(s -> !s.trim().isEmpty())
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .forEach(materielIds::add);
                        }

                        listeSalles.add(new Salle(id, numero, capacite, localisation, materielIds));

                    } catch (NumberFormatException e) {
                        System.err.println("Ligne salle ignorée (numérique invalide) : " + String.join(";", ligne) + " - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Ligne salle ignorée (erreur de parsing) : " + String.join(";", ligne) + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Ligne salle ignorée (pas assez de colonnes) : " + String.join(";", ligne));
                }
            }
            tableViewSalles.setItems(listeSalles);

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
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/admin/creer-modifier-salle.fxml", "Créer une salle", stageActuel, null);
    }

    private void handleModifierSalle(Salle salle) {
        System.out.println("Modifier salle : " + salle.getNumeroSalle());
        Stage stageActuel = (Stage) tableViewSalles.getScene().getWindow();
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/admin/creer-modifier-salle.fxml", "Modifier salle", stageActuel, salle);
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
            tableViewSalles.setItems(listeSalles); // Afficher toutes les salles si le champ est vide
            return;
        }

        ObservableList<Salle> sallesFiltrees = FXCollections.observableArrayList();
        for (Salle salle : listeSalles) {
            if (salle.getNumeroSalle().toLowerCase().contains(texteRecherche) ||
                    salle.getLocalisation().toLowerCase().contains(texteRecherche) ||
                    obtenirNomsMateriel(salle.getMaterielIds()).toLowerCase().contains(texteRecherche)) {
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