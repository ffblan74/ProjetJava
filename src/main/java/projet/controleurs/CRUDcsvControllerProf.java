package projet.controleurs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRUDcsvControllerProf {

    // Cette map est spécifique aux fichiers que le professeur manipule
    private static final Map<String, Integer> PROF_FILE_COLUMN_COUNTS = new HashMap<>();

    static {
        // Définitions spécifiques pour les fichiers du professeur
        // Ces noms DOIVENT CORRESPONDRE EXACTEMENT aux noms de vos fichiers CSV réels dans le dossier 'csv'
        // Et les nombres de colonnes DOIVENT CORRESPONDRE à la structure de vos CSV
        PROF_FILE_COLUMN_COUNTS.put("utilisateurs.csv", 8); // Confirmé : utilisateurs.csv, 8 colonnes
        PROF_FILE_COLUMN_COUNTS.put("salle.csv", 6);       // Confirmé : salle.csv, 6 colonnes
        PROF_FILE_COLUMN_COUNTS.put("cours.csv", 9);       // Confirmé : cours.csv, 9 colonnes
    }

    /**
     * Récupère le nombre de colonnes attendu pour un fichier CSV donné,
     * en utilisant la map spécifique aux fichiers du professeur.
     *
     * @param filePath Chemin complet du fichier CSV.
     * @return Le nombre de colonnes attendu, ou 0 si le fichier n'est pas reconnu par cette classe.
     */
    private static int getExpectedColumnCountProf(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return PROF_FILE_COLUMN_COUNTS.getOrDefault(fileName, 0);
    }

    /**
     * Lit toutes les lignes d'un fichier CSV.
     *
     * @param chemin Chemin du fichier CSV.
     * @return Une liste de tableaux de chaînes, où chaque tableau représente une ligne du CSV.
     * @throws IOException En cas de problème de lecture du fichier.
     */
    public static List<String[]> lire(String chemin) throws IOException {
        List<String[]> lignes = new ArrayList<>();
        File file = new File(chemin);
        if (!file.exists()) {
            System.out.println("Le fichier CSV n'existe pas encore : " + chemin + ". Retourne une liste vide.");
            return lignes; // Retourne une liste vide si le fichier n'existe pas
        }
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                lignes.add(ligne.split(";", -1));
            }
        }
        return lignes;
    }

    /**
     * Écrit toutes les lignes dans un fichier CSV, en s'assurant que chaque ligne
     * a le nombre de colonnes attendu pour les fichiers spécifiques aux professeurs.
     *
     * @param chemin Chemin du fichier CSV.
     * @param lignes Liste des lignes à écrire (chaque ligne est un tableau de colonnes).
     * @throws IOException En cas de problème d'écriture.
     */
    public static void ecrire(String chemin, List<String[]> lignes) throws IOException {
        int expectedColumnCount = getExpectedColumnCountProf(chemin); // Utilise la map spécifique aux profs

        // Assurez-vous que le répertoire existe
        File parentDir = new File(chemin).getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Crée les répertoires parents si nécessaire
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin))) {
            for (String[] ligne : lignes) {
                String[] ligneAEcrire = ligne;

                if (expectedColumnCount > 0 && ligne.length < expectedColumnCount) {
                    ligneAEcrire = Arrays.copyOf(ligne, expectedColumnCount);
                    for (int i = ligne.length; i < expectedColumnCount; i++) {
                        if (ligneAEcrire[i] == null) {
                            ligneAEcrire[i] = "";
                        }
                    }
                }
                bw.write(String.join(";", ligneAEcrire));
                bw.newLine();
            }
        }
    }

    /**
     * Recherche des lignes dans un fichier CSV basées sur une valeur dans une colonne spécifique.
     *
     * @param chemin Chemin du fichier CSV.
     * @param indexColonneClef Index de la colonne à rechercher (ex: 0 pour l'ID).
     * @param valeurClef Valeur à rechercher dans la colonne spécifiée.
     * @return Une liste de tableaux de chaînes représentant les lignes trouvées.
     * @throws IOException En cas de problème de lecture du fichier.
     */
    public static List<String[]> rechercher(String chemin, int indexColonneClef, String valeurClef) throws IOException {
        List<String[]> lignesTrouvees = new ArrayList<>();
        List<String[]> toutesLesLignes = lire(chemin); // Utilise la lire de cette classe

        int startIndex = 0;
        if (!toutesLesLignes.isEmpty() && toutesLesLignes.get(0).length > 0) {
            try {
                Integer.parseInt(toutesLesLignes.get(0)[0].trim());
            } catch (NumberFormatException e) {
                startIndex = 1;
            }
        }

        for (int i = startIndex; i < toutesLesLignes.size(); i++) {
            String[] ligne = toutesLesLignes.get(i);
            if (ligne.length > indexColonneClef && ligne[indexColonneClef].trim().equals(valeurClef)) {
                lignesTrouvees.add(ligne);
            }
        }
        return lignesTrouvees;
    }

    /**
     * Met à jour une ligne spécifique dans le fichier CSV.
     *
     * @param chemin Chemin du fichier CSV.
     * @param indexColonneClef Index de la colonne contenant la valeur de la clé de recherche (ex: 0 pour l'ID).
     * @param valeurClef Valeur de la clé à rechercher pour identifier la ligne à modifier.
     * @param nouvelleLigne Tableau de chaînes représentant la nouvelle ligne à écrire.
     * @throws IOException En cas de problème de lecture/écriture.
     */
    public static void mettreAJour(String chemin, int indexColonneClef, String valeurClef, String[] nouvelleLigne) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin); // Utilise la lire de cette classe
        boolean trouve = false;

        String[] header = null;
        int startIndex = 0;
        if (!toutesLesLignes.isEmpty() && toutesLesLignes.get(0).length > 0) {
            try {
                Integer.parseInt(toutesLesLignes.get(0)[0].trim());
            } catch (NumberFormatException e) {
                header = toutesLesLignes.get(0);
                startIndex = 1;
            }
        }

        for (int i = startIndex; i < toutesLesLignes.size(); i++) {
            String[] ligneActuelle = toutesLesLignes.get(i);
            if (ligneActuelle.length > indexColonneClef && ligneActuelle[indexColonneClef].trim().equals(valeurClef)) {
                toutesLesLignes.set(i, nouvelleLigne);
                trouve = true;
                break;
            }
        }
        if (trouve) {
            List<String[]> lignesPourEcriture = new ArrayList<>();
            if (header != null) {
                lignesPourEcriture.add(header);
            }
            lignesPourEcriture.addAll(toutesLesLignes.subList(startIndex, toutesLesLignes.size()));
            ecrire(chemin, lignesPourEcriture); // Utilise la ecrire de cette classe
        } else {
            System.err.println("Mise à jour échouée : Clé '" + valeurClef + "' non trouvée dans le fichier " + chemin);
        }
    }

    /**
     * Supprime une ligne spécifique du fichier CSV.
     *
     * @param chemin Chemin du fichier CSV.
     * @param indexColonneClef Index de la colonne contenant la valeur de la clé de recherche (ex: 0 pour l'ID).
     * @param valeurClef Valeur de la clé à rechercher pour identifier la ligne à supprimer.
     * @throws IOException En cas de problème de lecture/écriture.
     */
    public static void supprimerLigne(String chemin, int indexColonneClef, String valeurClef) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin); // Utilise la lire de cette classe
        List<String[]> lignesApresSuppression = new ArrayList<>();
        boolean trouve = false;

        String[] header = null;
        int startIndex = 0;
        if (!toutesLesLignes.isEmpty() && toutesLesLignes.get(0).length > 0) {
            try {
                Integer.parseInt(toutesLesLignes.get(0)[0].trim());
            } catch (NumberFormatException e) {
                header = toutesLesLignes.get(0);
                startIndex = 1;
            }
        }

        for (int i = startIndex; i < toutesLesLignes.size(); i++) {
            String[] ligne = toutesLesLignes.get(i);
            if (ligne.length > indexColonneClef && ligne[indexColonneClef].trim().equals(valeurClef)) {
                trouve = true;
            } else {
                lignesApresSuppression.add(ligne);
            }
        }

        if (trouve) {
            List<String[]> finalLines = new ArrayList<>();
            if (header != null) {
                finalLines.add(header);
            }
            finalLines.addAll(lignesApresSuppression);
            ecrire(chemin, finalLines); // Utilise la ecrire de cette classe
        } else {
            System.err.println("Suppression échouée : Clé '" + valeurClef + "' non trouvée dans le fichier " + chemin);
        }
    }
}