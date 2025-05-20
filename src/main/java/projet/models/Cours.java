package projet.models;

import java.time.LocalDate;
import java.time.Duration;

public class Cours {
    private int idCours;
    private String nomMatiere;
    private String codeCours;
    private Duration duree;
    private int enseignantId;
    private int salleId;
    private int horaireId;
    private LocalDate date;  // Ajout de l'attribut date
    private String horaire; // Ajout de l'attribut horaire
    private String classeCible;

    public Cours(int idCours, String nomMatiere, String codeCours, Duration duree, int enseignantId, int salleId, int horaireId, LocalDate date, String horaire, String classeCible) {
        this.idCours = idCours;
        this.nomMatiere = nomMatiere;
        this.codeCours = codeCours;
        this.duree = duree;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
        this.horaireId = horaireId;
        this.date = date;
        this.horaire = horaire;
        this.classeCible = classeCible;
    }

    // Getters
    public int getIdCours() { return idCours; }
    public String getNomMatiere() { return nomMatiere; }
    public String getCodeCours() { return codeCours; }
    public Duration getDuree() { return duree; }
    public int getEnseignantId() { return enseignantId; }
    public int getSalleId() { return salleId; }
    public int getHoraireId() { return horaireId; }
    public LocalDate getDate() { return date; }
    public String getHoraire() { return horaire; }
    public String getClasseCible() { return classeCible; }

    // Setters
    public void setIdCours(int idCours) { this.idCours = idCours; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }
    public void setCodeCours(String codeCours) { this.codeCours = codeCours; }
    public void setDuree(Duration duree) { this.duree = duree; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    public void setHoraireId(int horaireId) { this.horaireId = horaireId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHoraire(String horaire) { this.horaire = horaire; }
    public void setClasseCible(String classeCible) { this.classeCible = classeCible; }
}
