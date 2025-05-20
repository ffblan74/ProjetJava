package projet.controleurs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * CRUDcsvController - Contrôleur générique pour manipuler des fichiers CSV.
 */
public class CRUDcsvController {

    /**
     * Lire toutes les lignes du fichier CSV.
     *
     * @param chemin Chemin du fichier CSV.
     * @return Liste de toutes les lignes, chaque ligne étant représentée comme un tableau de chaînes.
     * @throws IOException En cas de problème de lecture du fichier.
     */
    public static List<String[]> lire(String chemin) throws IOException {
        List<String[]> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;

            while ((ligne = br.readLine()) != null) {
                lignes.add(ligne.split(";")); // Découpe chaque ligne par le ";"
            }
        }
        return lignes;
    }

    /**
     * Ajouter une ligne au fichier CSV.
     *
     * @param chemin Chemin du fichier CSV.
     * @param ligne  Tableau de chaînes représentant les colonnes de la nouvelle ligne.
     * @throws IOException En cas de problème d'écriture dans le fichier.
     */
    public static void ajouter(String chemin, String[] ligne) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin, true))) {
            bw.write(String.join(";", ligne)); // Concatène les colonnes avec un délimiteur ";"
            bw.newLine(); // Passe à la ligne suivante
        }
    }

    /**
     * Réécrire le fichier entier avec de nouvelles données.
     *
     * @param chemin Chemin du fichier CSV.
     * @param lignes Liste des lignes à écrire (chaque ligne est un tableau de colonnes).
     * @throws IOException En cas de problème d'écriture.
     */
    public static void ecrire(String chemin, List<String[]> lignes) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin))) {
            for (String[] ligne : lignes) {
                bw.write(String.join(";", ligne)); // Concatène les colonnes avec un délimiteur ";"
                bw.newLine();
            }
        }
    }

    /**
     * Supprimer une ligne spécifique en fonction d'une condition (valeur spécifique dans une colonne donnée).
     *
     * @param chemin       Chemin du fichier CSV.
     * @param indexColonne Index de la colonne où chercher la valeur.
     * @param valeur       Valeur utilisée pour identifier les lignes à supprimer.
     * @throws IOException En cas de problème d'accès ou d'écriture.
     */
    public static void supprimerLigne(String chemin, int indexColonne, String valeur) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin);
        List<String[]> lignesFiltrees = new ArrayList<>();

        // Garder les lignes qui NE correspondent PAS à la valeur donnée
        for (String[] ligne : toutesLesLignes) {
            if (indexColonne < ligne.length && !ligne[indexColonne].equals(valeur)) {
                lignesFiltrees.add(ligne);
            }
        }

        // Réécrire le fichier avec les lignes restantes
        ecrire(chemin, lignesFiltrees);
    }

    /**
     * Mettre à jour une ligne spécifique dans le fichier CSV en fonction d'une condition.
     *
     * @param chemin           Chemin du fichier CSV.
     * @param indexColonne     Index de la colonne clé pour identifier la ligne à mettre à jour.
     * @param valeurRecherchee Valeur à rechercher pour localiser la ligne.
     * @param nouvellesValeurs Tableau représentant la nouvelle version complète de la ligne.
     * @throws IOException En cas de problème d'accès ou d'écriture.
     */
    public static void mettreAJour(String chemin, int indexColonne, String valeurRecherchee, String[] nouvellesValeurs) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin);

        // Modifier la ligne correspondante
        for (int i = 0; i < toutesLesLignes.size(); i++) {
            String[] ligne = toutesLesLignes.get(i);
            if (indexColonne < ligne.length && ligne[indexColonne].equals(valeurRecherchee)) {
                toutesLesLignes.set(i, nouvellesValeurs); // Remplacement de la ligne
                break;
            }
        }

        // Réécrire tout le fichier avec les modifications
        ecrire(chemin, toutesLesLignes);
    }

    /**
     * Rechercher des lignes contenant une valeur spécifique dans une colonne donnée.
     */
    public static List<String[]> rechercher(String chemin, int indexColonne, String valeur) throws IOException {
        List<String[]> resultat = new ArrayList<>();
        List<String[]> toutesLesLignes = lire(chemin);

        // Filtrer les lignes correspondantes à la valeur
        for (String[] ligne : toutesLesLignes) {
            if (indexColonne < ligne.length && ligne[indexColonne].equals(valeur)) {
                resultat.add(ligne);
            }
        }
        return resultat;
    }
}
