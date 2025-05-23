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
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String CHEMIN_FICHIER_SALLES = "src/main/resources/projet/csv/salle.csv";
    private ObservableList<Salle> listeSalles;
    private Map<Integer, String[]> donneesCSVCompletesSalles = new HashMap<>();

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
    }

    private void chargerSalles() {
        try {
            List<String[]> lignesSalles = CRUDcsvController.lire(CHEMIN_FICHIER_SALLES);
            listeSalles = FXCollections.observableArrayList();
            donneesCSVCompletesSalles.clear();

            if (lignesSalles.isEmpty()) {
                tableViewSalles.setItems(FXCollections.emptyObservableList());
                return;
            }

            int startIndex = (lignesSalles.size() > 0 && lignesSalles.get(0)[0].equalsIgnoreCase("idSalle")) ? 1 : 0;

            for (int i = startIndex; i < lignesSalles.size(); i++) {
                String[] ligne = lignesSalles.get(i);
                if (ligne.length >= 6) {
                    try {
                        int id = Integer.parseInt(ligne[0].trim());
                        String numero = ligne[1].trim();
                        int capacite = Integer.parseInt(ligne[2].trim());
                        String localisation = ligne[3].trim();

                        String materielNomCsv = ligne[4].trim();
                        String materielDescriptionCsv = ligne[5].trim();

                        List<String> nomsMateriel = materielNomCsv.isEmpty() ? new ArrayList<>() :
                                Arrays.asList(materielNomCsv.split(",")).stream().map(String::trim).collect(Collectors.toList());

                        List<String> descriptionsMateriel = materielDescriptionCsv.isEmpty() ? new ArrayList<>() :
                                Arrays.asList(materielDescriptionCsv.split(",")).stream().map(String::trim).collect(Collectors.toList());

                        listeSalles.add(new Salle(id, numero, capacite, localisation, nomsMateriel, descriptionsMateriel));
                        donneesCSVCompletesSalles.put(id, ligne);

                    } catch (NumberFormatException e) {
                        System.err.println("Ligne salle ignorée (ID/Capacité non numérique) : " + String.join(";", ligne) + " - " + e.getMessage());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println("Ligne salle ignorée (colonnes manquantes) : " + String.join(";", ligne) + " - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Ligne salle ignorée (erreur inattendue) : " + String.join(";", ligne) + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Ligne salle ignorée (pas assez de colonnes pour le format attendu) : " + String.join(";", ligne));
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
        NavigationUtil.ouvrirNouvelleFenetre("/projet/fxml/creer-modifier-salle.fxml", "Créer une salle", stageActuel, null);
    }

    private void handleModifierSalle(Salle salle) {
        System.out.println("Modifier salle : " + salle.getNumeroSalle());

        String[] ligneCSVComplete = donneesCSVCompletesSalles.get(salle.getIdSalle());

        if (ligneCSVComplete == null) {
            System.err.println("Erreur: Données CSV complètes introuvables pour la salle ID: " + salle.getIdSalle());
            NavigationUtil.afficherErreur("Erreur interne: Données de salle introuvables pour la modification.");
            return;
        }

        Object[] dataToTransmit = {salle, ligneCSVComplete};

        Stage stageActuel = (Stage) tableViewSalles.getScene().getWindow();
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/creer-modifier-salle.fxml",
                "Modifier salle",
                stageActuel,
                dataToTransmit
        );
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