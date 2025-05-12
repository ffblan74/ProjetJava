package projet.models;

public class Materiel {
    private int idMateriel;
    private String nom;
    private String description;

    // Constructeur
    public Materiel(int idMateriel, String nom, String description) {
        this.idMateriel = idMateriel;
        this.nom = nom;
        this.description = description;
    }

    // Getters et Setters
    public int getIdMateriel() {
        return idMateriel;
    }

    public void setIdMateriel(int idMateriel) {
        this.idMateriel = idMateriel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Méthode pour afficher les informations du matériel
    public String afficherMateriel() {
        return "Nom : " + nom + " | Description : " + description;
    }
}
