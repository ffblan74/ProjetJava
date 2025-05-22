package projet.models;

import projet.controleurs.CRUDcsvController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Cours {
    private int idCours;
    private String matiere;
    private String codeCours;
    private String description;
    private int enseignantId;
    private int salleId;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String classe;

    // Attributs pour l'affichage des informations enrichies
    private String enseignantNomComplet;
    private Salle salle;

    // Constructeur
    public Cours(int idCours, String matiere, String codeCours, String description,
                 int enseignantId, int salleId, LocalDate date, LocalTime heureDebut,
                 LocalTime heureFin, String classe) {
        this.idCours = idCours;
        this.matiere = matiere;
        this.codeCours = codeCours;
        this.description = description;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
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
    public int getSalleId() { return salleId; }
    public LocalDate getDate() { return date; }
    public String getHeureDebut() { return String.valueOf(heureDebut); }
    public String getHeureFin() { return String.valueOf(heureFin); }
    public String getClasse() { return classe; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }
    public String getNumeroSalle() {

        String cheminFichier = "src/main/resources/projet/csv/salle.csv";

        String numeroSalle = null;
        try {
            List<String[]> resultats = CRUDcsvController.rechercher(cheminFichier, 0, String.valueOf(salleId));

            if (!resultats.isEmpty()) {
                String[] data = resultats.get(0); // Suppose qu'il y a seulement une correspondance
                numeroSalle = data[1].trim();
            } else {
                System.err.println("Aucune salle trouvée avec l'ID :" + String.valueOf(salleId));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier salle.csv : " + e.getMessage());
        }
        return numeroSalle;
    }

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
    public void setSalleId(int salleId) { this.salleId = salleId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public void setClasse(String classe) { this.classe = classe; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }
    public void setSalle(Salle salle) { this.salle = salle; }




    /**
     * Crée un objet Cours à partir d'un tableau de String représentant une ligne CSV.
     * Format attendu :
     * idCours;nomMatiere;codeCours;description;enseignantId;salleId;date;heureDebut;heureFin;classe
     */
    public static Cours fromCsv(String[] data) {
        if (data.length < 10) {
            throw new IllegalArgumentException("Ligne CSV cours invalide : " + Arrays.toString(data));
        }

        int idCours = Integer.parseInt(data[0].trim());
        String matiere = data[1].trim();
        String codeCours = data[2].trim();
        String description = data[3].trim();
        int enseignantId = Integer.parseInt(data[4].trim());
        int salleId = Integer.parseInt(data[5].trim());
        LocalDate date = LocalDate.parse(data[6].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime heureDebut = LocalTime.parse(data[7].trim(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime heureFin = LocalTime.parse(data[8].trim(), DateTimeFormatter.ofPattern("HH:mm"));
        String classe = data[9].trim();

        return new Cours(idCours, matiere, codeCours, description, enseignantId,
                salleId, date, heureDebut, heureFin, classe);
    }
}
