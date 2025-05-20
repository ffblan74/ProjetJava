package projet.models;

import projet.models.Role;
import projet.models.Utilisateur;

public class Etudiant extends Utilisateur {
    private String groupe;
    private int emploiDuTempsId;

    public Etudiant(int idUtilisateur, String nom, String prenom, String email, String motDePasse, String groupe, int emploiDuTempsId) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ETUDIANT);
        this.groupe = groupe;
        this.emploiDuTempsId = emploiDuTempsId;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public int getEmploiDuTempsId() {
        return emploiDuTempsId;
    }

    public void setEmploiDuTempsId(int emploiDuTempsId) {
        this.emploiDuTempsId = emploiDuTempsId;
    }
}