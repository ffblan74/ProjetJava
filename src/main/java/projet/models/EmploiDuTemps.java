package projet.models;

import java.util.List;

public class EmploiDuTemps {
    private int idEmploiDuTemps;
    private int etudiantId;
    private List<Integer> coursIds;

    // Constructeur
    public EmploiDuTemps(int idEmploiDuTemps, int etudiantId, List<Integer> coursIds) {
        this.idEmploiDuTemps = idEmploiDuTemps;
        this.etudiantId = etudiantId;
        this.coursIds = coursIds;
    }

    // Getters et Setters
    public int getIdEmploiDuTemps() {
        return idEmploiDuTemps;
    }

    public void setIdEmploiDuTemps(int idEmploiDuTemps) {
        this.idEmploiDuTemps = idEmploiDuTemps;
    }

    public int getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(int etudiantId) {
        this.etudiantId = etudiantId;
    }

    public List<Integer> getCoursIds() {
        return coursIds;
    }

    public void setCoursIds(List<Integer> coursIds) {
        this.coursIds = coursIds;
    }
}
