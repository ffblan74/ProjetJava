package projet.projetjava;

public class Administrateur extends Utilisateur {
    public Administrateur(String identifiant, String motDePasse, String nom) {
        super(identifiant, motDePasse, nom, "Administrateur");
    }
}