package projet.models;

import java.time.Duration;
import java.time.LocalTime; // Non directement utilisé ici mais utile pour d'autres manipulations de temps
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class Cours {
    private int idCours;
    private String matiere;
    private String codeCours;
    private String description;
    private Duration duree;
    private int enseignantId;
    private int salleId;
    private int horaireId;
    private String jour;
    private String heureDebut;
    private String heureFin;
    private String classe;

    // Attributs pour l'affichage des informations enrichies (non directement depuis le CSV du cours)
    private String enseignantNomComplet; // Utilisé dans AccueilEleveController
    private String salle; // Utilisé dans AccueilEleveController pour le numéro de salle (ex: "A101")

    // Constructeur complet
    public Cours(int idCours, String matiere, String codeCours, String description, Duration duree,
                 int enseignantId, int salleId, int horaireId, String jour, String heureDebut,
                 String heureFin, String classe) {
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
        // Ces attributs sont initialisés par les setters après la lecture du CSV pour l'enrichissement
        this.enseignantNomComplet = "";
        this.salle = "";
    }

    // --- Getters ---
    public int getIdCours() { return idCours; }
    public String getMatiere() { return matiere; }
    public String getCodeCours() { return codeCours; }
    public String getDescription() { return description; }
    public Duration getDuree() { return duree; }
    public int getEnseignantId() { return enseignantId; }
    public int getSalleId() { return salleId; }
    public int getHoraireId() { return horaireId; }
    public String getJour() { return jour; }
    public String getHeureDebut() { return heureDebut; }
    public String getHeureFin() { return heureFin; }
    public String getClasse() { return classe; }

    // Getters pour les attributs enrichis/d'affichage
    public String getEnseignantNomComplet() { return enseignantNomComplet; }
    public String getSalle() { return salle; } // Renvoie le numéro de salle (String)

    // --- Setters ---
    public void setIdCours(int idCours) { this.idCours = idCours; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public void setCodeCours(String codeCours) { this.codeCours = codeCours; }
    public void setDescription(String description) { this.description = description; }
    public void setDuree(Duration duree) { this.duree = duree; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    public void setHoraireId(int horaireId) { this.horaireId = horaireId; }
    public void setJour(String jour) { this.jour = jour; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public void setClasse(String classe) { this.classe = classe; }

    // Setters pour les attributs enrichis/d'affichage
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }
    public void setSalle(String salle) { this.salle = salle; } // Définit le numéro de salle (String)

    /**
     * Crée un objet Cours à partir d'un tableau de String représentant une ligne CSV.
     * Assurez-vous que l'ordre des colonnes dans le CSV correspond à cet ordre :
     * idCours;nomMatiere;codeCours;description;duree;enseignantId;salleId;horaireId;jour;heureDebut;heureFin;classe
     * @param data La ligne CSV parsée en tableau de String.
     * @return Un objet Cours.
     * @throws IllegalArgumentException si la ligne CSV n'a pas le nombre attendu de colonnes.
     */
    public static Cours fromCsv(String[] data) {
        // Vérification de la longueur minimale des données (12 colonnes attendues)
        if (data.length < 12) {
            throw new IllegalArgumentException("Ligne CSV cours invalide (trop peu de colonnes): " + Arrays.toString(data) + ". Attendu 12, trouvé " + data.length);
        }

        int idCours = Integer.parseInt(data[0].trim());
        String matiere = data[1].trim();
        String codeCours = data[2].trim();
        String description = data[3].trim();

        // Gérer la durée : La méthode parseDurationFromString est appelée ici
        Duration duree = parseDurationFromString(data[4].trim());

        int enseignantId = Integer.parseInt(data[5].trim());
        int salleId = Integer.parseInt(data[6].trim());
        int horaireId = Integer.parseInt(data[7].trim());
        String jour = data[8].trim();
        String heureDebut = data[9].trim();
        String heureFin = data[10].trim();
        String classe = data[11].trim(); // C'est la 12ème colonne (index 11)

        // Crée et retourne le nouvel objet Cours
        return new Cours(idCours, matiere, codeCours, description, duree,
                enseignantId, salleId, horaireId, jour, heureDebut, heureFin, classe);
    }

    /**
     * Méthode utilitaire privée et statique pour parser la durée d'une String.
     * Elle gère les formats comme "2h", "1h30", et tente l'ISO_8601 ("PT2H", "PT1H30M").
     * @param dureeStr La chaîne de caractères représentant la durée.
     * @return Un objet Duration. Retourne Duration.ZERO en cas d'erreur de parsing.
     */
    private static Duration parseDurationFromString(String dureeStr) {
        dureeStr = dureeStr.trim().toLowerCase(); // Nettoyage et conversion en minuscules

        if (dureeStr.endsWith("h")) {
            try {
                int hours = Integer.parseInt(dureeStr.substring(0, dureeStr.length() - 1));
                return Duration.ofHours(hours);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour la durée (heures) : " + dureeStr + ". " + e.getMessage());
                return Duration.ZERO;
            }
        } else if (dureeStr.contains("h")) {
            // Gérer les formats comme "1h30" ou "2h00"
            String[] parts = dureeStr.split("h");
            try {
                int hours = Integer.parseInt(parts[0]);
                int minutes = 0;
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    minutes = Integer.parseInt(parts[1]);
                }
                return Duration.ofHours(hours).plusMinutes(minutes);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour la durée (heures/minutes) : " + dureeStr + ". " + e.getMessage());
                return Duration.ZERO;
            }
        }
        // Fallback : tenter de parser comme durée ISO_8601 (ex: "PT2H", "PT1H30M")
        try {
            return Duration.parse(dureeStr.toUpperCase()); // ISO_8601 est souvent en majuscules (PT2H)
        } catch (DateTimeParseException e) {
            System.err.println("Format de durée non reconnu, retour à Duration.ZERO : " + dureeStr + ". " + e.getMessage());
            return Duration.ZERO;
        }
    }
}