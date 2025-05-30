package projet.controleurs.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import projet.controleurs.CRUDcsvController;
import projet.models.Salle;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.utils.Transmissible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreerModifierSalleController implements Transmissible {

    @FXML private Label titrePage;
    @FXML private TextField numeroSalleField;
    @FXML private TextField capaciteField;
    @FXML private TextField localisationField;
    @FXML private TextArea materielNomTextArea;
    @FXML private TextArea materielsDescriptionTextArea;
    @FXML private Button creerModifierSalleButton;
    @FXML private Button annulerButton;

    private Salle salleAModifier;
    private String[] ligneCSVOriginaleAModifier;
    private Utilisateur utilisateurConnecte;

    private static final String CHEMIN_FICHIER_SALLES = "src/main/resources/projet/csv/salle.csv";
    private static final String CSV_EN_TETE = "idSalle;numeroSalle;capacite;localisation;materielNom;materielsDescription";

    @FXML
    public void initialize() {
        materielNomTextArea.setWrapText(true);
        materielsDescriptionTextArea.setWrapText(true);
    }

    @Override
    public void transmettreDonnees(Object data) {
        if (data == null) { // <-- NOUVEAU : Traite 'null' comme un signal pour la CRÉATION d'une salle
            this.salleAModifier = null; // Assure que nous sommes en mode création
            titrePage.setText("Créer une nouvelle salle");
            creerModifierSalleButton.setText("Créer la salle");
            // Optionnel : Récupère l'utilisateur connecté globalement si ses infos sont nécessaires pour la création
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            if (this.utilisateurConnecte == null) {
                System.err.println("Aucun utilisateur administrateur connecté pour la création de salle.");
                // Décide si tu veux afficher une erreur ou rediriger ici si l'utilisateur est absolument requis
            }
        } else if (data instanceof Utilisateur) {
            // Cette branche pourrait être utilisée si un objet Utilisateur est transmis
            // explicitement pour donner le contexte de l'administrateur créateur, par exemple.
            this.utilisateurConnecte = (Utilisateur) data;
            this.salleAModifier = null; // En mode création
            titrePage.setText("Créer une nouvelle salle");
            creerModifierSalleButton.setText("Créer la salle");
        } else if (data instanceof Object[] && ((Object[]) data).length == 2) {
            // C'est un tableau d'objets pour la modification : {Salle, String[]}
            Object[] transmittedData = (Object[]) data;
            if (transmittedData[0] instanceof Salle && transmittedData[1] instanceof String[]) {
                this.salleAModifier = (Salle) transmittedData[0];
                this.ligneCSVOriginaleAModifier = (String[]) transmittedData[1];
                preRemplirChampsPourModification();
                // Récupère l'utilisateur connecté pour le contexte si besoin
                this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            } else {
                System.err.println("Type de données incorrectes dans le tableau transmis pour modification de salle.");
                NavigationUtil.afficherErreur("Données de modification de salle invalides.");
                retournerAccueilAdmin();
            }
        } else {
            // Cas de données vraiment inattendues (ni null, ni Utilisateur, ni Object[])
            System.err.println("Type de données non géré transmis au contrôleur CreerModifierSalleController: " + data.getClass().getName());
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
            if (this.utilisateurConnecte == null) {
                NavigationUtil.afficherErreur("Aucun utilisateur administrateur connecté. Redirection.");
                retournerAccueilAdmin();
            }
        }
    }

    private void preRemplirChampsPourModification() {
        if (salleAModifier != null) {
            titrePage.setText("Modifier la salle : " + salleAModifier.getNumeroSalle());
            numeroSalleField.setText(salleAModifier.getNumeroSalle());
            capaciteField.setText(String.valueOf(salleAModifier.getCapacite()));
            localisationField.setText(salleAModifier.getLocalisation());
            // Convertir les listes de String en String séparées par des virgules pour les TextArea
            materielNomTextArea.setText(String.join(",", salleAModifier.getMaterielNom()));
            materielsDescriptionTextArea.setText(String.join(",", salleAModifier.getMaterielsDescription()));
            creerModifierSalleButton.setText("Modifier la salle");
        }
    }

    @FXML
    private void handleCreerModifierSalle(ActionEvent event) {
        String numeroSalle = numeroSalleField.getText();
        String capaciteStr = capaciteField.getText();
        String localisation = localisationField.getText();
        String materielNomCsv = materielNomTextArea.getText();
        String materielsDescriptionCsv = materielsDescriptionTextArea.getText();

        if (!champsValides(numeroSalle, capaciteStr, localisation)) {
            return;
        }

        int capacite = Integer.parseInt(capaciteStr);

        List<String> materielNom = materielNomCsv.isEmpty() ? new ArrayList<>() : Arrays.asList(materielNomCsv.split(",")).stream().map(String::trim).collect(Collectors.toList());
        List<String> materielsDescription = materielsDescriptionCsv.isEmpty() ? new ArrayList<>() : Arrays.asList(materielsDescriptionCsv.split(",")).stream().map(String::trim).collect(Collectors.toList());

        try {
            List<String[]> toutesLesLignes = CRUDcsvController.lire(CHEMIN_FICHIER_SALLES);
            List<String[]> lignesSansEnTete = (toutesLesLignes.isEmpty() || toutesLesLignes.get(0)[0].equals("idSalle"))
                    ? new ArrayList<>(toutesLesLignes.subList(1, toutesLesLignes.size()))
                    : new ArrayList<>(toutesLesLignes);

            if (salleAModifier == null) { // Mode Création
                int prochainId = calculerProchainId(lignesSansEnTete);
                Salle nouvelleSalle = new Salle(prochainId, numeroSalle, capacite, localisation, materielNom, materielsDescription);
                String[] nouvelleLigneCSV = nouvelleSalle.toCSVArray();

                if (toutesLesLignes.isEmpty() || (toutesLesLignes.size() == 1 && toutesLesLignes.get(0)[0].equals("idSalle"))) {
                    CRUDcsvController.ajouter(CHEMIN_FICHIER_SALLES, CSV_EN_TETE.split(";"));
                }
                CRUDcsvController.ajouter(CHEMIN_FICHIER_SALLES, nouvelleLigneCSV);
                NavigationUtil.afficherInformation("Succès", "Salle créée avec succès !");

            } else { // Mode Modification
                int idAModifier = salleAModifier.getIdSalle();
                Salle salleMaj = new Salle(idAModifier, numeroSalle, capacite, localisation, materielNom, materielsDescription);
                String[] ligneCSVMaj = salleMaj.toCSVArray();

                CRUDcsvController.mettreAJour(CHEMIN_FICHIER_SALLES, 0, String.valueOf(idAModifier), ligneCSVMaj);
                NavigationUtil.afficherInformation("Succès", "Salle mise à jour avec succès !");
            }
            retournerAccueilAdmin();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'opération sur la salle : " + e.getMessage());
            e.printStackTrace();
            NavigationUtil.afficherErreur("Erreur lors de la création/modification de la salle.");
        }
    }

    private boolean champsValides(String numeroSalle, String capaciteStr, String localisation) {
        if (numeroSalle == null || numeroSalle.isEmpty()) {
            NavigationUtil.afficherErreur("Le numéro de salle ne peut pas être vide.");
            return false;
        }
        if (capaciteStr == null || capaciteStr.isEmpty()) {
            NavigationUtil.afficherErreur("La capacité ne peut pas être vide.");
            return false;
        }
        try {
            int capacite = Integer.parseInt(capaciteStr);
            if (capacite <= 0) {
                NavigationUtil.afficherErreur("La capacité doit être un nombre entier positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            NavigationUtil.afficherErreur("La capacité doit être un nombre valide.");
            return false;
        }
        if (localisation == null || localisation.isEmpty()) {
            NavigationUtil.afficherErreur("La localisation ne peut pas être vide.");
            return false;
        }
        return true;
    }

    private int calculerProchainId(List<String[]> sallesSansEnTete) {
        int dernierId = 0;
        for (String[] ligne : sallesSansEnTete) {
            try {
                int currentId = Integer.parseInt(ligne[0].trim());
                if (currentId > dernierId) {
                    dernierId = currentId;
                }
            } catch (NumberFormatException e) {
                System.err.println("ID salle non numérique trouvé lors du calcul du prochain ID : " + ligne[0]);
            }
        }
        return dernierId + 1;
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        retournerAccueilAdmin();
    }

    private void retournerAccueilAdmin() {
        Stage stageActuel = (Stage) annulerButton.getScene().getWindow();
        stageActuel.close();

        // Retourne à la page de gestion des salles
        NavigationUtil.ouvrirNouvelleFenetre(
                "/projet/fxml/accueil-admin-gerer-salles.fxml",
                "Gestion des Salles",
                null,
                utilisateurConnecte // Transmet l'admin connecté pour rafraîchir la vue principale
        );
    }
}