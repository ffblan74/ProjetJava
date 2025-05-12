package projet.controleurs;

import projet.models.Administrateur;
import projet.models.Enseignant;
import projet.models.Etudiant;
import projet.models.Utilisateur;
import projet.models.Role;

import java.util.ArrayList;
import java.util.List;

public class LoginController {

    // Liste simulée d'utilisateurs enregistrés (normalement, tu utiliserais une base de données)
    private List<Utilisateur> utilisateurs;

    public LoginController() {
        utilisateurs = new ArrayList<>();
        // Exemple d'utilisateurs
        utilisateurs.add(new Etudiant(1, "Dupont", "Pierre", "pierre.dupont@mail.com", "motdepasse123", "Groupe A", 1));
        utilisateurs.add(new Enseignant(2, "Durand", "Marie", "marie.durand@mail.com", "motdepasse123", new ArrayList<String>(), 2));
        utilisateurs.add(new Administrateur(3, "Martin", "Jean", "jean.martin@mail.com", "admin123", 3));
    }

    // Méthode pour se connecter
    public Utilisateur login(String email, String motDePasse) {
        for (Utilisateur utilisateur : utilisateurs) {
            // Si l'email et le mot de passe correspondent
            if (utilisateur.getEmail().equals(email) && utilisateur.getMotDePasse().equals(motDePasse)) {
                System.out.println("Connexion réussie pour: " + utilisateur.getNom() + " " + utilisateur.getPrenom());
                return utilisateur; // Retourne l'utilisateur authentifié
            }
        }
        System.out.println("Echec de la connexion: identifiants incorrects.");
        return null; // Aucun utilisateur trouvé avec ces identifiants
    }

    // Méthode pour rediriger l'utilisateur en fonction de son rôle
    public void redirectToRolePage(Utilisateur utilisateur) {
        if (utilisateur != null) {
            switch (utilisateur.getRole()) {
                case ETUDIANT:
                    System.out.println("Redirection vers la page de l'étudiant.");
                    break;
                case ENSEIGNANT:
                    System.out.println("Redirection vers la page de l'enseignant.");
                    break;
                case ADMINISTRATEUR:
                    System.out.println("Redirection vers la page de l'administrateur.");
                    break;
                default:
                    System.out.println("Rôle non reconnu.");
            }
        }
    }

    // Méthode de test pour vérifier la connexion
    public static void main(String[] args) {
        LoginController loginController = new LoginController();

        // Exemple de tentative de connexion
        Utilisateur utilisateur = loginController.login("pierre.dupont@mail.com", "motdepasse123");

        // Si la connexion est réussie, on redirige l'utilisateur en fonction de son rôle
        if (utilisateur != null) {
            loginController.redirectToRolePage(utilisateur);
        }
    }
}
