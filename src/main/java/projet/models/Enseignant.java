package projet.models;

import projet.models.Role;
import projet.models.Utilisateur;
import java.util.List;

public class Enseignant extends Utilisateur {
    private List<String> matiereEnseignee;

    public Enseignant(int idUtilisateur, String nom, String prenom, String email, String motDePasse, List<String> matiereEnseignee) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ENSEIGNANT);
        this.matiereEnseignee = matiereEnseignee;
    }

    public List<String> getMatiereEnseignee() {
        return matiereEnseignee;
    }

    public void setMatiereEnseignee(List<String> matiereEnseignee) {
        this.matiereEnseignee = matiereEnseignee;
    }

}