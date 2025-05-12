package projet;

class Administrateur extends Utilisateur {
    private int idAdministrateur;

    public Administrateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse, int idAdministrateur) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ADMINISTRATEUR);
        this.idAdministrateur = idAdministrateur;
    }

    public int getIdAdministrateur() {
        return idAdministrateur;
    }

    public void setIdAdministrateur(int idAdministrateur) {
        this.idAdministrateur = idAdministrateur;
    }
}
}