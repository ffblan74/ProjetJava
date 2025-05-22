package projet.controleurs.eleve;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import projet.controleurs.CRUDcsvController;
import projet.models.Etudiant;
import projet.models.Utilisateur;
import projet.utils.NavigationUtil;
import projet.models.Cours;
import projet.utils.GrilleUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class AccueilEleveController {

    @FXML
    private Label labelBienvenue;
    @FXML
    private Label labelSemaine;
    @FXML
    private Label labelStats;
    @FXML
    private GridPane grilleEmploi;
    @FXML
    private Label labelDate;
    @FXML
    private Button buttonDeconnexion;

    private LocalDate dateActuelle;
    private Etudiant utilisateurConnecte;
    private List<Cours> listeCours;

    // Méthode d'initialisation de la vue
    @FXML
    public void initialize() {
        dateActuelle = LocalDate.now();
        if (Utilisateur.getUtilisateurConnecte() instanceof Etudiant) {
            this.utilisateurConnecte = (Etudiant) Utilisateur.getUtilisateurConnecte();
            setEleve(utilisateurConnecte);
            afficherSemaine();
            afficherStats();
        }
    }

    public void setEleve(Utilisateur eleve) {
        this.utilisateurConnecte = (Etudiant) eleve;
        labelBienvenue.setText("Bienvenue, " + eleve.getPrenom() + " " + eleve.getNom());
        chargerCours();
    }

    private void chargerCours() {
        try {
            listeCours = new ArrayList<>();
            List<String[]> lignes = CRUDcsvController.lire("src/main/resources/projet/csv/cours.csv");
            if (!lignes.isEmpty() && lignes.get(0)[0].equalsIgnoreCase("idCours")) {
                lignes.remove(0);
            }

            // Récupérer le groupe de l'élève
            String groupeEleve = utilisateurConnecte.getGroupe(); // Méthode getGroupe() dans Utilisateur

            for (String[] ligne : lignes) {
                try {
                    Cours cours = Cours.fromCsv(ligne);
                    // Vérifiez si le cours correspond au groupe de l'élève
                    if (cours.getClasse().equals(groupeEleve)) { // Assurez-vous que Cours a une méthode getGroupe()
                        listeCours.add(cours);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur parsing cours: " + e.getMessage());
                }
            }

            GrilleUtil.afficherGrille(grilleEmploi, listeCours, dateActuelle); // Afficher les cours dans la grille
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier cours: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        NavigationUtil.deconnexion(buttonDeconnexion); // Gérer la déconnexion
    }

    @FXML
    private void semainePrecedente() {
        dateActuelle = dateActuelle.minusWeeks(1);
        afficherStats();
        afficherSemaine();
        chargerCours();
    }

    @FXML
    private void semaineSuivante() {
        dateActuelle = dateActuelle.plusWeeks(1);
        afficherStats();
        afficherSemaine();
        chargerCours();
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
}