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
        // Initialisation des noms de fichiers et de leurs nombres de colonnes attendus
        FILE_COLUMN_COUNTS.put("utilisateurs.csv", 9);
        FILE_COLUMN_COUNTS.put("salle.csv", 6);
        FILE_COLUMN_COUNTS.put("notification.csv", 6);
        FILE_COLUMN_COUNTS.put("cours.csv", 11);
        // CORRECTION : matieres.csv a 5 colonnes.
        FILE_COLUMN_COUNTS.put("matieres.csv", 5);
    }

    /**
     * Récupère le nombre de colonnes attendu pour un fichier CSV donné.
     *
     * @param filePath Chemin complet du fichier CSV.
     * @return Le nombre de colonnes attendu, ou 0 si le fichier n'est pas reconnu.
     */
    private static int getExpectedColumnCount(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString(); // Extrait le nom du fichier (ex: "utilisateurs.csv")
        return FILE_COLUMN_COUNTS.getOrDefault(fileName, 0); // Retourne le compte ou 0 si non trouvé
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
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                lignes.add(ligne.split(";", -1)); // Le -1 assure que les champs vides de fin de ligne sont inclus
            }
        }
        return lignes;
    }

    /**
     * Ajouter une ligne au fichier CSV.
     * Le comportement d'ajout d'une nouvelle ligne avant l'écriture des données
     * est appliqué uniquement pour les fichiers CSV connus.
     * Pour les autres fichiers, l'ajout se fait de manière standard (données puis nouvelle ligne).
     *
     * ATTENTION : Pour les fichiers CSV connus, cette approche va insérer une ligne vide
     * avant chaque nouvelle entrée (y compris la première si le fichier est vide),
     * ce qui n'est pas le format standard pour les fichiers CSV.
     * De plus, les lignes ne seront pas terminées par un saut de ligne dans le fichier,
     * ce qui peut causer des problèmes de lecture ultérieurs.
     *
     * @param chemin Chemin du fichier CSV.
     * @param ligne  Tableau de chaînes représentant les colonnes de la nouvelle ligne.
     * @throws IOException En cas de problème d'écriture dans le fichier.
     */
    public static void ajouter(String chemin, String[] ligne) throws IOException {
        Path path = Paths.get(chemin);
        String fileName = path.getFileName().toString();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin, true))) {
            // Vérifie si le fichier est l'un des CSV connus
            if (FILE_COLUMN_COUNTS.containsKey(fileName)) {
                // Comportement spécifique demandé : ajouter une nouvelle ligne AVANT d'écrire les données.
                // Cela créera une ligne vide avant chaque nouvelle entrée.
                bw.newLine();
            }
            // Écrit les données de la ligne.
            bw.write(String.join(";", ligne));
            // Ne pas ajouter de bw.newLine() ici si le bw.newLine() précédent est le comportement désiré.
            // Cela laisserait la ligne non terminée.
        }
    }

    /**
     * Réécrire le fichier entier avec de nouvelles données,
     * en s'assurant que chaque ligne a le nombre de colonnes attendu.
     *
     * @param chemin Chemin du fichier CSV.
     * @param lignes Liste des lignes à écrire (chaque ligne est un tableau de colonnes).
     * @throws IOException En cas de problème d'écriture.
     */
    public static void ecrire(String chemin, List<String[]> lignes) throws IOException {
        int expectedColumnCount = getExpectedColumnCount(chemin); // Récupère le nombre de colonnes attendu

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin))) {
            for (String[] ligne : lignes) {
                String[] ligneAEcrire = ligne;

                // Si le fichier est reconnu et que la ligne est plus courte que prévu, on la padde
                if (expectedColumnCount > 0 && ligne.length < expectedColumnCount) {
                    ligneAEcrire = Arrays.copyOf(ligne, expectedColumnCount);
                    // Remplir les éléments nouvellement ajoutés (qui sont null par défaut) par des chaînes vides
                    for (int i = ligne.length; i < expectedColumnCount; i++) {
                        if (ligneAEcrire[i] == null) {
                            ligneAEcrire[i] = "";
                        }
                    }
                }
                // Si la ligne est plus longue que prévu, on l'écrit telle quelle (les colonnes supplémentaires seront incluses)

                System.out.println("Écriture ligne (avec padding si nécessaire) : " + String.join(";", ligneAEcrire)); // Pour le débogage
                bw.write(String.join(";", ligneAEcrire)); // Concatène les colonnes avec un délimiteur ";"
                bw.newLine(); // Passe à la ligne suivante (terminaison standard de ligne)
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
        List<String[]> toutesLesLignes = lire(chemin); // Lire toutes les lignes du fichier

        System.out.println("testimate : " + toutesLesLignes.size());


        for (String[] ligne : toutesLesLignes) {
            // Assurer que l'index de la colonne est valide pour cette ligne
            // et que la valeur de la colonne correspond à la valeur recherchée (ignorante des espaces blancs).
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
        List<String[]> toutesLesLignes = lire(chemin);
        boolean trouve = false;
        for (int i = 0; i < toutesLesLignes.size(); i++) {
            String[] ligneActuelle = toutesLesLignes.get(i);
            // Utilise .trim() pour gérer les espaces blancs autour de la valeur
            if (ligneActuelle.length > indexColonneClef && ligneActuelle[indexColonneClef].trim().equals(valeurClef)) {
                toutesLesLignes.set(i, nouvelleLigne); // Remplace la ligne
                trouve = true;
                break;
            }
        }
        if (trouve) {
            ecrire(chemin, toutesLesLignes); // Réécrit tout le fichier avec la ligne mise à jour
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
        List<String[]> toutesLesLignes = lire(chemin);
        List<String[]> lignesApresSuppression = new ArrayList<>();
        boolean trouve = false;

        // Préserve l'en-tête si elle existe (si la première colonne de la première ligne n'est pas un nombre)
        if (!toutesLesLignes.isEmpty() && !toutesLesLignes.get(0)[0].matches("\\d+")) {
            lignesApresSuppression.add(toutesLesLignes.get(0));
            toutesLesLignes = toutesLesLignes.subList(1, toutesLesLignes.size()); // Traite le reste des lignes
        }

        for (String[] ligne : toutesLesLignes) {
            // Utilise .trim() pour gérer les espaces blancs autour de la valeur
            if (ligne.length > indexColonneClef && ligne[indexColonneClef].trim().equals(valeurClef)) {
                trouve = true; // La ligne à supprimer a été trouvée, ne l'ajoute pas à la nouvelle liste
            } else {
                lignesApresSuppression.add(ligne); // Ajoute toutes les autres lignes
            }
        }

        if (trouve) {
            ecrire(chemin, lignesApresSuppression); // Réécrit le fichier sans la ligne supprimée
        } else {
            System.err.println("Suppression échouée : Clé '" + valeurClef + "' non trouvée dans le fichier " + chemin);
        }
    }
}