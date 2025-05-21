package projet.models;

import projet.models.Role;

import java.util.Arrays;

public class Utilisateur {
    protected int idUtilisateur;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String motDePasse;
    protected Role role;

    private static Utilisateur utilisateurConnecte;

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse, Role role) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public static void connecter(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String[] toCSVArray(String groupe, String emploiDuTempsId, String matiereEnseignee) {
        // Gérer les cas où les champs spécifiques pourraient être null ou "None" pour les convertir en chaîne vide pour CSV
        String groupeCSV = (groupe == null || groupe.trim().isEmpty() || groupe.equalsIgnoreCase("None")) ? "" : groupe;
        String emploiDuTempsIdCSV = (emploiDuTempsId == null || emploiDuTempsId.trim().isEmpty() || emploiDuTempsId.equalsIgnoreCase("None")) ? "" : emploiDuTempsId;
        String matiereEnseigneeCSV = (matiereEnseignee == null || matiereEnseignee.trim().isEmpty() || matiereEnseignee.equalsIgnoreCase("None")) ? "" : matiereEnseignee;

        // Si la matière enseignée contient un ; ou des " , l'entourer de guillemets et échapper les "
        if (matiereEnseigneeCSV.contains(";") || matiereEnseigneeCSV.contains("\"")) {
            matiereEnseigneeCSV = "\"" + matiereEnseigneeCSV.replace("\"", "\"\"") + "\"";
        }

        return new String[]{
                String.valueOf(getIdUtilisateur()),
                getNom(),
                getPrenom(),
                getEmail(),
                getMotDePasse(),
                getRole().name(),
                groupeCSV,
                emploiDuTempsIdCSV,
                matiereEnseigneeCSV
        };
    }

}