package projet.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; // Ajouté pour catch
import java.util.Arrays;
import java.util.Locale;

public class Cours {
    private int idCours;
    private String matiere;
    private String codeCours;
    private String description;
    private int enseignantId;
    private String salle; // <-- CHANGÉ DE int à String pour "B201"
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String classe;

    // Attribut pour l'affichage des informations enrichies (peut être redondant si 'salle' est déjà le nom)
    private String enseignantNomComplet;

    // Constructeur
    // Le paramètre 'salle' est maintenant de type String
    public Cours(int idCours, String matiere, String codeCours, String description,
                 int enseignantId, String salle, LocalDate date, LocalTime heureDebut, // <-- "salle" est String ici
                 LocalTime heureFin, String classe) {
        this.idCours = idCours;
        this.matiere = matiere;
        this.codeCours = codeCours;
        this.description = description;
        this.enseignantId = enseignantId;
        this.salle = salle; // <-- Assignation de String à String
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.classe = classe;
        this.enseignantNomComplet = "";
    }

    // --- Getters ---
    public int getIdCours() { return idCours; }
    public String getMatiere() { return matiere; }
    public String getCodeCours() { return codeCours; }
    public String getDescription() { return description; }
    public int getEnseignantId() { return enseignantId; }

    // getSalle() retourne maintenant directement le champ 'salle' qui est un String
    public String getSalle() { return salle; } // <-- MODIFIÉ : retourne String

    public LocalDate getDate() { return date; }

    // Ces getters restent en String comme demandé, utilisant String.valueOf()
    public String getHeureDebut() { return String.valueOf(heureDebut); }
    public String getHeureFin() { return String.valueOf(heureFin); }

    public String getClasse() { return classe; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }


    // Méthode utilitaire pour obtenir le jour de la semaine en français (ex: "lundi")
    public String getJourSemaineFrancais() {
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH);
    }

    // --- Setters ---
    public void setIdCours(int idCours) { this.idCours = idCours; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public void setCodeCours(String codeCours) { this.codeCours = codeCours; }
    public void setDescription(String description) { this.description = description; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    // setSalle() prend maintenant un String
    public void setSalle(String salle) { this.salle = salle; } // <-- MODIFIÉ : prend String

    public void setDate(LocalDate date) { this.date = date; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public void setClasse(String classe) { this.classe = classe; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }

    /**
     * Crée un objet Cours à partir d'un tableau de String représentant une ligne CSV.
     * Format attendu :
     * idCours;matiere;codeCours;description;enseignantId;salle;date;heureDebut;heureFin;classe
     * (Note: le champ 'salle' dans le CSV sera lu comme un String ici)
     */
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

            // Lecture de la salle comme String pour gérer des valeurs comme "B201"
            String salle = data[5].trim(); // <-- MODIFIÉ : Lecture directe de String

            // Assurez-vous que le format de date dans votre CSV est "yyyy-MM-dd"
            LocalDate date = LocalDate.parse(data[6].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // Assurez-vous que le format d'heure dans votre CSV est "HH:mm"
            LocalTime heureDebut = LocalTime.parse(data[7].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime heureFin = LocalTime.parse(data[8].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            String classe = data[9].trim();

            // Création de l'objet Cours avec le String 'salle'
            return new Cours(idCours, matiere, codeCours, description, enseignantId,
                    salle, date, heureDebut, heureFin, classe); // <-- 'salle' est String ici
        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Erreur de parsing des données CSV pour le cours: " + Arrays.toString(data) + " - " + e.getMessage());
            throw new IllegalArgumentException("Format de données invalide pour un cours dans le CSV.", e);
        }
    }
}