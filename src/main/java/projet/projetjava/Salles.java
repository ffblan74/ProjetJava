package projet.projetjava;

public class Salles {
    private String numero;
    private int capacite;
    private String equipement;

    public Salles(String numero, int capacite, String equipement) {
        this.numero = numero;
        this.capacite = capacite;
        this.equipement = equipement;
    }

    // Getters et setters

    public String getNumero() {
        return numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public String getEquipement() {
        return equipement;
    }

    @Override
    public String toString() {
        return "Salle " + numero + " (Capacité: " + capacite + ", Équipement: " + equipement + ")";
    }
}