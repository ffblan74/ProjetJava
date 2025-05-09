package projet.projetjava;

public class Etudiant extends Utilisateur {
    public Etudiant(String identifiant, String motDePasse, String nom) {
        super(identifiant, motDePasse, nom, "Etudiant");
    }
}