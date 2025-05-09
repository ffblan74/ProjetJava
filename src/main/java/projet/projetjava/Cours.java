package projet.projetjava;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Cours {
    private String code;
    private String nom;
    private Enseignant enseignant;
    private Salles salles; // Utilisation de "Salles"
    private DayOfWeek jourDeLaSemaine;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public Cours(String code, String nom, Enseignant enseignant, Salles salles, DayOfWeek jourDeLaSemaine, LocalTime heureDebut, LocalTime heureFin) {
        this.code = code;
        this.nom = nom;
        this.enseignant = enseignant;
        this.salles = salles; // Utilisation de "salles"
        this.jourDeLaSemaine = jourDeLaSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    // Getters et setters pour les attributs

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public Salles getSalles() { // Getter pour "salles"
        return salles;
    }

    public DayOfWeek getJourDeLaSemaine() {
        return jourDeLaSemaine;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setSalles(Salles salles) { // Setter pour "salles"
        this.salles = salles;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    @Override
    public String toString() {
        return nom + " (" + code + ") - " + jourDeLaSemaine + " " + heureDebut + "-" + heureFin + " dans " + (salles != null ? salles.getNumero() : "N/A") + " avec " + (enseignant != null ? enseignant.getNom() : "N/A");
    }
}