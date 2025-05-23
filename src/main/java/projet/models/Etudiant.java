package projet.models;

import projet.models.Role;
import projet.models.Utilisateur;

public class Etudiant extends Utilisateur {
    private String groupe;
    private int emploiDuTempsId;

    public Etudiant(int idUtilisateur, String nom, String prenom, String email, String motDePasse, String groupe) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ETUDIANT);
        this.groupe = groupe;
        this.emploiDuTempsId = emploiDuTempsId;
    }

    public String getGroupe() {
        return groupe;
    }

}