package projet.models;

import projet.models.Role;
import projet.models.Utilisateur;
import java.util.List;

class Enseignant extends Utilisateur {
    private List<String> matiereEnseignee;
    private int idEnseignant;

    public Enseignant(int idUtilisateur, String nom, String prenom, String email, String motDePasse, List<String> matiereEnseignee, int idEnseignant) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ENSEIGNANT);
        this.matiereEnseignee = matiereEnseignee;
        this.idEnseignant = idEnseignant;
    }

    public List<String> getMatiereEnseignee() {
        return matiereEnseignee;
    }

    public void setMatiereEnseignee(List<String> matiereEnseignee) {
        this.matiereEnseignee = matiereEnseignee;
    }

    public int getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
    }
}
}