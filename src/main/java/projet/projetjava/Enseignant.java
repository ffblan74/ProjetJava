package projet.projetjava;

public class Enseignant extends Utilisateur {
    public Enseignant(String identifiant, String motDePasse, String nom) {
        super(identifiant, motDePasse, nom, "Enseignant");
    }
}