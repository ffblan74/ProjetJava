package projet.models;

import projet.models.Role;
import projet.models.Utilisateur;

public class Administrateur extends Utilisateur {

    public Administrateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ADMINISTRATEUR);
    }

}