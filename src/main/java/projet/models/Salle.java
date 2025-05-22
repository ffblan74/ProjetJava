package projet.models;

import projet.controleurs.CRUDcsvController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Salle {
    private int idSalle;
    private String numeroSalle;
    private int capacite;
    private String localisation;
    private List<String> materielNom;
    private List<String> materielsDescription;


    public Salle(int idSalle, String numeroSalle, int capacite, String localisation, List<String> materielNom, List<String> materielsDescription) {
        this.idSalle = idSalle;
        this.numeroSalle = numeroSalle;
        this.capacite = capacite;
        this.localisation = localisation;
        this.materielNom = (materielNom != null) ? materielNom : new ArrayList<>();
        this.materielsDescription = (materielsDescription != null) ? materielsDescription : new ArrayList<>();
    }

    // Nouveau constructeur pour faciliter la création depuis des données CSV (où les listes sont des chaînes)
    public Salle(int idSalle, String numeroSalle, int capacite, String localisation, String materielNomCsv, String materielsDescriptionCsv) {
        this(
                idSalle,
                numeroSalle,
                capacite,
                localisation,
                (materielNomCsv != null && !materielNomCsv.isEmpty()) ? Arrays.asList(materielNomCsv.split(",")) : new ArrayList<>(),
                (materielsDescriptionCsv != null && !materielsDescriptionCsv.isEmpty()) ? Arrays.asList(materielsDescriptionCsv.split(",")) : new ArrayList<>()
        );
    }

    // --- Getters et Setters existants ---
    public int getIdSalle() { return idSalle; }
    public void setIdSalle(int idSalle) { this.idSalle = idSalle; }
    public String getNumeroSalle() { return numeroSalle; }
    public void setNumeroSalle(String numeroSalle) { this.numeroSalle = numeroSalle; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public List<String> getMaterielNom() {
        return materielNom;
    }

    public void setMaterielNom(List<String> materielNom) {
        this.materielNom = materielNom;
    }

    public List<String> getMaterielsDescription() {
        return materielsDescription;
    }

    public void setMaterielsDescription(List<String> materielsDescription) {
        this.materielsDescription = materielsDescription;
    }


    // Méthode pour obtenir une chaîne affichable de tous les noms de matériels (pour l'affichage en tableau par exemple)
    public String getMaterielsNomsAffichables() {
        if (materielNom == null || materielNom.isEmpty()) {
            return "Aucun";
        }
        return String.join(", ", materielNom);
    }

    // Méthode pour obtenir une chaîne affichable de toutes les descriptions de matériels
    public String getMaterielsDescriptionsAffichables() {
        if (materielsDescription == null || materielsDescription.isEmpty()) {
            return "Aucune";
        }
        return String.join(", ", materielsDescription);
    }


    /**
     * Convertit l'objet Salle en tableau de String pour l'écriture CSV.
     * Les listes de matériel sont converties en chaînes séparées par des virgules.
     */
    public String[] toCSVArray() {
        String materielNomCsv = (materielNom != null && !materielNom.isEmpty())
                ? String.join(",", materielNom)
                : "";
        String materielsDescriptionCsv = (materielsDescription != null && !materielsDescription.isEmpty())
                ? String.join(",", materielsDescription)
                : "";
        return new String[]{
                String.valueOf(idSalle),
                numeroSalle,
                String.valueOf(capacite),
                localisation,
                materielNomCsv, // Stocké comme une chaîne séparée par des virgules
                materielsDescriptionCsv // Stocké comme une chaîne séparée par des virgules
        };
    }
}