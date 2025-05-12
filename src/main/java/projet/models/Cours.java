package projet.models;

import java.time.Duration;

class Cours {
    private int idCours;
    private String nomMatiere;
    private String codeCours;
    private String description;
    private Duration duree;
    private int enseignantId;
    private int salleId;
    private int horaireId;

    public Cours(int idCours, String nomMatiere, String codeCours, String description, Duration duree, int enseignantId, int salleId, int horaireId) {
        this.idCours = idCours;
        this.nomMatiere = nomMatiere;
        this.codeCours = codeCours;
        this.description = description;
        this.duree = duree;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
        this.horaireId = horaireId;
    }

    public int getIdCours() {
        return idCours;
    }

    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public String getNomMatiere() {
        return nomMatiere;
    }

    public void setNomMatiere(String nomMatiere) {
        this.nomMatiere = nomMatiere;
    }

    public String getCodeCours() {
        return codeCours;
    }

    public void setCodeCours(String codeCours) {
        this.codeCours = codeCours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getDuree() {
        return duree;
    }

    public void setDuree(Duration duree) {
        this.duree = duree;
    }

    public int getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }

    public int getSalleId() {
        return salleId;
    }

    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public int getHoraireId() {
        return horaireId;
    }

    public void setHoraireId(int horaireId) {
        this.horaireId = horaireId;
    }
}