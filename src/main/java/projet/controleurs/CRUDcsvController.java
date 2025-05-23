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

public class CRUDcsvController {

    private static final Map<String, Integer> FILE_COLUMN_COUNTS = new HashMap<>();

    static {
        FILE_COLUMN_COUNTS.put("utilisateurs.csv", 9);
        FILE_COLUMN_COUNTS.put("salle.csv", 6);
        FILE_COLUMN_COUNTS.put("notifications.csv", 6);
        FILE_COLUMN_COUNTS.put("cours.csv", 11);
        FILE_COLUMN_COUNTS.put("matieres.csv", 5);
    }

    private static int getExpectedColumnCount(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return FILE_COLUMN_COUNTS.getOrDefault(fileName, 0);
    }

    public static List<String[]> lire(String chemin) throws IOException {
        List<String[]> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                lignes.add(ligne.split(";", -1));
            }
        }
        return lignes;
    }

    public static void ajouter(String chemin, String[] ligne) throws IOException {
        Path path = Paths.get(chemin);
        String fileName = path.getFileName().toString();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin, true))) {
            if (FILE_COLUMN_COUNTS.containsKey(fileName)) {
                bw.newLine();
            }
            bw.write(String.join(";", ligne));
        }
    }

    public static void ecrire(String chemin, List<String[]> lignes) throws IOException {
        int expectedColumnCount = getExpectedColumnCount(chemin);

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
                System.out.println("Écriture ligne (avec padding si nécessaire) : " + String.join(";", ligneAEcrire));
                bw.write(String.join(";", ligneAEcrire));
                bw.newLine();
            }
        }
    }

    public static List<String[]> rechercher(String chemin, int indexColonneClef, String valeurClef) throws IOException {
        List<String[]> lignesTrouvees = new ArrayList<>();
        List<String[]> toutesLesLignes = lire(chemin);

        System.out.println("testimate : " + toutesLesLignes.size());


        for (String[] ligne : toutesLesLignes) {
            if (ligne.length > indexColonneClef && ligne[indexColonneClef].trim().equals(valeurClef)) {
                lignesTrouvees.add(ligne);
            }
        }
        return lignesTrouvees;
    }

    public static void mettreAJour(String chemin, int indexColonneClef, String valeurClef, String[] nouvelleLigne) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin);
        boolean trouve = false;
        for (int i = 0; i < toutesLesLignes.size(); i++) {
            String[] ligneActuelle = toutesLesLignes.get(i);
            if (ligneActuelle.length > indexColonneClef && ligneActuelle[indexColonneClef].trim().equals(valeurClef)) {
                toutesLesLignes.set(i, nouvelleLigne);
                trouve = true;
                break;
            }
        }
        if (trouve) {
            ecrire(chemin, toutesLesLignes);
        } else {
            System.err.println("Mise à jour échouée : Clé '" + valeurClef + "' non trouvée dans le fichier " + chemin);
        }
    }

    public static void supprimerLigne(String chemin, int indexColonneClef, String valeurClef) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin);
        List<String[]> lignesApresSuppression = new ArrayList<>();
        boolean trouve = false;

        if (!toutesLesLignes.isEmpty() && !toutesLesLignes.get(0)[0].matches("\\d+")) {
            lignesApresSuppression.add(toutesLesLignes.get(0));
            toutesLesLignes = toutesLesLignes.subList(1, toutesLesLignes.size());
        }

        for (String[] ligne : toutesLesLignes) {
            if (ligne.length > indexColonneClef && ligne[indexColonneClef].trim().equals(valeurClef)) {
                trouve = true;
            } else {
                lignesApresSuppression.add(ligne);
            }
        }

        if (trouve) {
            ecrire(chemin, lignesApresSuppression);
        } else {
            System.err.println("Suppression échouée : Clé '" + valeurClef + "' non trouvée dans le fichier " + chemin);
        }
    }
}