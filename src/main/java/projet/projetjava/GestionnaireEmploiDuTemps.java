package projet.projetjava;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireEmploiDuTemps {
    private List<Cours> listeDesCours;
    private List<Salles> listeDesSalles;
    private List<Utilisateur> listeDesUtilisateurs; // Contient les Etudiants, Enseignants, Administrateurs

    public GestionnaireEmploiDuTemps() {
        this.listeDesCours = new ArrayList<>();
        this.listeDesSalles = new ArrayList<>();
        this.listeDesUtilisateurs = new ArrayList<>();
    }

    public void ajouterCours(Cours cours) {
        // Ajouter des vérifications de conflits ici
        listeDesCours.add(cours);
    }

    public void ajouterSalle(Salles salles) {
        listeDesSalles.add(salles);
    }

    public void ajouterUtilisateur(Utilisateur utilisateur) {
        listeDesUtilisateurs.add(utilisateur);
    }

    public List<Cours> getCoursPourEtudiant(Etudiant etudiant) {
        // Logique pour récupérer les cours auxquels un étudiant est inscrit
        // Cela pourrait nécessiter une autre classe (e.g., Inscription)
        return new ArrayList<>(); // À implémenter
    }

    public List<Cours> getCoursPourEnseignant(Enseignant enseignant) {
        List<Cours> coursEnseignant = new ArrayList<>();
        for (Cours cours : listeDesCours) {
            if (cours.getEnseignant() != null && cours.getEnseignant().equals(enseignant)) {
                coursEnseignant.add(cours);
            }
        }
        return coursEnseignant;
    }

    public List<Cours> getAllCours() {
        return listeDesCours;
    }

    public List<Salles> getAllSalles() { // Changement ici : List<Salle> -> List<Salles>
        return listeDesSalles;
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return listeDesUtilisateurs;
    }

    // Méthodes pour modifier les cours, les salles, affecter des enseignants,
    // vérifier les conflits d'horaires et de salles, etc.
    // Ces méthodes nécessiteront une logique plus complexe.

    public boolean verifierDisponibiliteSalle(Salles salle, DayOfWeek jour, LocalTime debut, LocalTime fin) { // Changement ici : Salle salle -> Salles salle
        for (Cours cours : listeDesCours) {
            if (cours.getSalles() != null && cours.getSalles().equals(salle) && cours.getJourDeLaSemaine() == jour) { // Changement ici : cours.getSalle() -> cours.getSalles()
                if ((debut.isBefore(cours.getHeureFin()) && debut.isAfter(cours.getHeureDebut())) ||
                        (fin.isAfter(cours.getHeureDebut()) && fin.isBefore(cours.getHeureFin())) ||
                        (debut.equals(cours.getHeureDebut()) || fin.equals(cours.getHeureFin())) ||
                        (debut.isBefore(cours.getHeureDebut()) && fin.isAfter(cours.getHeureFin()))) {
                    return false; // La salle est déjà occupée à ce moment-là
                }
            }
        }
        return true; // La salle est disponible
    }

}