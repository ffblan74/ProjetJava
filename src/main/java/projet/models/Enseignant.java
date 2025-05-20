package projet.models;

import java.util.List;

public class Enseignant {
    private int idEnseignant;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private List<String> matiereEnseignee;

    public Enseignant(int idEnseignant, String nom, String prenom, String email, String password, List<String> matiereEnseignee) {
        this.idEnseignant = idEnseignant;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.matiereEnseignee = matiereEnseignee;
    }

    public int getIdEnseignant() { return idEnseignant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<String> getMatiereEnseignee() { return matiereEnseignee; }

    public void setIdEnseignant(int idEnseignant) { this.idEnseignant = idEnseignant; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setMatiereEnseignee(List<String> matiereEnseignee) { this.matiereEnseignee = matiereEnseignee; }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + email + ")";
    }
}