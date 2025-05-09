package projet.projetjava;


public class Utilisateur {
    private String identifiant;
    private String motDePasse;
    private String nom;
    private String role;

    public Utilisateur(String identifiant, String motDePasse, String nom, String role) {
        this.identifiant = identifiant;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.role = role;
    }

    // Getters et setters

    public String getIdentifiant() {
        return identifiant;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return nom + " (" + role + ")";
    }
}

