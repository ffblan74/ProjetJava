package projet.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

public class Cours {
    private int idCours;
    private String matiere;
    private String codeCours;
    private String description;
    private int enseignantId;
    private String salle;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String classe;
    private String enseignantNomComplet;

    public Cours(int idCours, String matiere, String codeCours, String description,
                 int enseignantId, String salle, LocalDate date, LocalTime heureDebut,
                 LocalTime heureFin, String classe) {
        this.idCours = idCours;
        this.matiere = matiere;
        this.codeCours = codeCours;
        this.description = description;
        this.enseignantId = enseignantId;
        this.salle = salle;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.classe = classe;
        this.enseignantNomComplet = "";
    }

    public int getIdCours() { return idCours; }
    public String getMatiere() { return matiere; }
    public String getCodeCours() { return codeCours; }
    public String getDescription() { return description; }
    public int getEnseignantId() { return enseignantId; }
    public String getSalle() { return salle; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getClasse() { return classe; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }


    public void setIdCours(int idCours) { this.idCours = idCours; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public void setCodeCours(String codeCours) { this.codeCours = codeCours; }
    public void setDescription(String description) { this.description = description; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setSalle(String salle) { this.salle = salle; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public void setClasse(String classe) { this.classe = classe; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }

    public String[] toCSVArray() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return new String[]{
                String.valueOf(this.idCours),
                this.matiere,
                this.codeCours,
                this.description,
                String.valueOf(this.enseignantId),
                this.salle,
                this.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                this.heureDebut.format(timeFormatter),
                this.heureFin.format(timeFormatter),
                this.classe
        };
    }

    public static Cours fromCsv(String[] data) {
        if (data.length < 10) {
            System.err.println("Données CSV de cours insuffisantes. Ligne: " + Arrays.toString(data));
            throw new IllegalArgumentException("Données CSV de cours insuffisantes pour la création du cours. Attendu 10 colonnes.");
        }
        try {
            int idCours = Integer.parseInt(data[0].trim());
            String matiere = data[1].trim();
            String codeCours = data[2].trim();
            String description = data[3].trim();
            int enseignantId = Integer.parseInt(data[4].trim());
            String salle = data[5].trim();
            LocalDate date = LocalDate.parse(data[6].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime heureDebut = LocalTime.parse(data[7].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime heureFin = LocalTime.parse(data[8].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            String classe = data[9].trim();

            return new Cours(idCours, matiere, codeCours, description, enseignantId,
                    salle, date, heureDebut, heureFin, classe);
        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Erreur de parsing des données CSV pour le cours: " + Arrays.toString(data) + " - " + e.getMessage());
            throw new IllegalArgumentException("Format de données invalide pour un cours dans le CSV.", e);
        }
    }
}