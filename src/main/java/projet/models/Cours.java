package projet.models;

import java.time.Duration;
// Ajoutez ou assurez-vous que java.time.LocalTime est importé si vous l'utilisez directement dans Cours
// import java.time.LocalTime;

public class Cours {
    private int idCours;
    private String matiere;
    private String codeCours;
    private String description;
    private Duration duree;
    private int enseignantId;
    private int salleId;
    private int horaireId; // L'ID de l'emploi du temps ou de la période (peut être 0 si non utilisé pour l'instant)

    // Nouvelles propriétés pour le jour et l'heure spécifiques du cours
    private String jour;
    private String heureDebut; // Format HH:mm
    private String heureFin;   // Format HH:mm
    private String classe;     // Le groupe d'étudiants (ex: "L3 MIAGE B")

    // Constructeur complet avec les nouvelles propriétés
    public Cours(int idCours, String matiere, String codeCours, String description, Duration duree,
                 int enseignantId, int salleId, int horaireId,
                 String jour, String heureDebut, String heureFin, String classe) {
        this.idCours = idCours;
        this.matiere = matiere;
        this.codeCours = codeCours;
        this.description = description;
        this.duree = duree;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
        this.horaireId = horaireId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.classe = classe;
    }

    // --- Getters ---
    public int getIdCours() {
        return idCours;
    }

    public String getMatiere() {
        return matiere;
    }

    public String getCodeCours() {
        return codeCours;
    }

    public String getDescription() {
        return description;
    }

    public Duration getDuree() {
        return duree;
    }

    public int getEnseignantId() {
        return enseignantId;
    }

    public int getSalleId() {
        return salleId;
    }

    public int getHoraireId() {
        return horaireId;
    }

    public String getJour() {
        return jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public String getClasse() {
        return classe;
    }

    // --- Setters (nécessaires si vous modifiez ces propriétés après création) ---
    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public void setCodeCours(String codeCours) {
        this.codeCours = codeCours;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuree(Duration duree) {
        this.duree = duree;
    }

    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }

    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public void setHoraireId(int horaireId) {
        this.horaireId = horaireId;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "idCours=" + idCours +
                ", matiere='" + matiere + '\'' +
                ", codeCours='" + codeCours + '\'' +
                ", description='" + description + '\'' +
                ", duree=" + duree.toHours() + "h" +
                ", enseignantId=" + enseignantId +
                ", salleId=" + salleId +
                ", horaireId=" + horaireId +
                ", jour='" + jour + '\'' +
                ", heureDebut='" + heureDebut + '\'' +
                ", heureFin='" + heureFin + '\'' +
                ", classe='" + classe + '\'' +
                '}';
    }
}