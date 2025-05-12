package projet;

import java.util.List;

class Salle {
    private int idSalle;
    private String numeroSalle;
    private int capacite;
    private String localisation;
    private List<Integer> materielIds;

    public Salle(int idSalle, String numeroSalle, int capacite, String localisation, List<Integer> materielIds) {
        this.idSalle = idSalle;
        this.numeroSalle = numeroSalle;
        this.capacite = capacite;
        this.localisation = localisation;
        this.materielIds = materielIds;
    }

    public int getIdSalle() {
        return idSalle;
    }

    public void setIdSalle(int idSalle) {
        this.idSalle = idSalle;
    }

    public String getNumeroSalle() {
        return numeroSalle;
    }

    public void setNumeroSalle(String numeroSalle) {
        this.numeroSalle = numeroSalle;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public List<Integer> getMaterielIds() {
        return materielIds;
    }

    public void setMaterielIds(List<Integer> materielIds) {
        this.materielIds = materielIds;
    }
}
