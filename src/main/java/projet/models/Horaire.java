package projet.models;

import java.time.LocalTime;

public class Horaire {
    private int idHoraire;
    private String jourSemaine;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    // Constructeur
    public Horaire(int idHoraire, String jourSemaine, LocalTime heureDebut, LocalTime heureFin) {
        this.idHoraire = idHoraire;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    // Getters et Setters
    public int getIdHoraire() {
        return idHoraire;
    }

    public void setIdHoraire(int idHoraire) {
        this.idHoraire = idHoraire;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    // Méthode pour afficher un horaire sous une forme lisible
    public String afficherHoraire() {
        return jourSemaine + " : " + heureDebut.toString() + " - " + heureFin.toString();
    }
}
