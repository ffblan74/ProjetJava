package projet.controleurs.professeur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import projet.models.Utilisateur;
import projet.models.Cours;
import projet.utils.GrilleUtil;
import projet.utils.NavigationUtil;
import projet.controleurs.CRUDcsvController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Locale;

public class AccueilProfController {

    @FXML private Label labelBienvenue;
    @FXML private Label labelSemaine;
    @FXML private Label labelStats;
    @FXML private GridPane grilleEmploi;
    @FXML private Label labelDate;

    private LocalDate dateActuelle;
    private Utilisateur utilisateurConnecte;
    private List<Cours> listeCours;

    private static final String CHEMIN_COURS = "src/main/resources/projet/csv/cours.csv";

    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();

        if (Utilisateur.getUtilisateurConnecte() instanceof Utilisateur) {
            this.utilisateurConnecte = Utilisateur.getUtilisateurConnecte();
        }

        setEnseignant(utilisateurConnecte);
        afficherSemaine();
        chargerCours();
    }

    public void setEnseignant(Utilisateur enseignant) {
        this.utilisateurConnecte = enseignant;
        labelBienvenue.setText("Bienvenue, " + enseignant.getPrenom() + " " + enseignant.getNom());
    }

    private void chargerCours() {
        try {
            listeCours = new ArrayList<>();
            List<String[]> lignes = CRUDcsvController.lire(CHEMIN_COURS);
            if (!lignes.isEmpty() && lignes.get(0)[0].equalsIgnoreCase("idCours")) {
                lignes.remove(0);
            }

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    if (cours.getEnseignantId() == utilisateurConnecte.getIdUtilisateur()) {
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing cours: " + e.getMessage());
                }
            }

            GrilleUtil.afficherGrille(grilleEmploi, listeCours, dateActuelle); // Utiliser la méthode de GrilleUtil
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours: " + e.getMessage());
        }
    }

    private void afficherSemaine() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        labelDate.setText(dateActuelle.format(formatter));

        int numeroSemaine = dateActuelle.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        labelSemaine.setText("Semaine " + numeroSemaine);
    }

    private void afficherStats() {
        int nbCours = 0;
        for (Cours cours : listeCours) {
            if (GrilleUtil.estDansLaSemaineActuelle(cours.getDate(), dateActuelle)) {
                nbCours++;
            }
        }
        labelStats.setText("Nombre de cours cette semaine : " + nbCours);
    }

    @FXML
    private void semainePrecedente() {
        dateActuelle = dateActuelle.minusWeeks(1);
        afficherSemaine();
        chargerCours();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        afficherSemaine();
        chargerCours();
    }

    @FXML
    private void ajouterCours() {
        System.out.println("Ajouter cours (non implémenté)");
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        System.out.println("Déconnexion de l'utilisateur.");
        Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
        NavigationUtil.changerScene(stageActuel, "/projet/fxml/login.fxml", "Connexion", null);
    }
}