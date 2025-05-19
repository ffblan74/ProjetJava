package projet.controleurs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CRUDcsvController {

    // Lire toutes les lignes d'un fichier CSV
    public static List<String[]> lire(String chemin) throws IOException {
        List<String[]> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                lignes.add(ligne.split(";")); // Chaque ligne est découpée par le ";"
            }
        }
        return lignes;
    }

    // Ajouter une ligne au fichier CSV
    public static void ajouter(String chemin, String[] ligne) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin, true))) {
            bw.newLine();
            bw.write(String.join(";", ligne)); // Combine les champs avec des ";"
        }
    }

    // Écraser le fichier entier avec de nouvelles données
    public static void ecrire(String chemin, List<String[]> lignes) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin))) {
            for (String[] ligne : lignes) {
                bw.write(String.join(";", ligne)); // Combine les champs avec des ";"
                bw.newLine();
            }
        }
    }

    // Supprimer une ligne spécifique en fonction d'une condition
    public static void supprimerLigne(String chemin, int indexColonne, String valeur) throws IOException {
        List<String[]> toutesLesLignes = lire(chemin);
        List<String[]> lignesFiltrees = new ArrayList<>();

        // Garder les lignes qui NE correspondent PAS à la condition
        for (String[] ligne : toutesLesLignes) {
            if (!ligne[indexColonne].equals(valeur)) {
                lignesFiltrees.add(ligne);
            }
        }

        // Réécrire le fichier complet
        ecrire(chemin, lignesFiltrees);
    }
}