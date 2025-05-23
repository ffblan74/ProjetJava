package projet.models;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

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

    public int getIdEnseignant() { return this.idUtilisateur; }


    public static Enseignant fromCsv(String[] data) {


        if (data.length < 6) {
            throw new IllegalArgumentException("Données CSV insuffisantes pour créer un Enseignant. Reçu: " + Arrays.toString(data));
        }

        int id = Integer.parseInt(data[0].trim());
        String nom = data[1].trim();
        String prenom = data[2].trim();
        String email = data[3].trim();
        String motDePasse = data[4].trim();


        List<String> matieres = new ArrayList<>();
        if (data.length > 8 && !data[8].trim().isEmpty()) {
            String matieresCsv = data[8].trim();
            if (matieresCsv.startsWith("\"") && matieresCsv.endsWith("\"")) {
                matieresCsv = matieresCsv.substring(1, matieresCsv.length() - 1);
            }

            matieresCsv = matieresCsv.replace("\"\"", "\"");


            matieres = Arrays.asList(matieresCsv.split(","));

            for (int i = 0; i < matieres.size(); i++) {
                matieres.set(i, matieres.get(i).trim());
            }
        }

        return new Enseignant(id, nom, prenom, email, motDePasse, matieres);
    }
}