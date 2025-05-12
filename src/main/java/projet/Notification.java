package projet;

import java.time.LocalDateTime;

//Il manque dans le diagramme UML emetteurID a rajouter

public class Notification {
    private int idNotification;
    private LocalDateTime dateCreation;
    private String message;
    private String type; // type de notification (par exemple : modification emploi du temps, conflit de salle, etc.)
    private String statut; // statut de la notification (par exemple : lue, non lue)
    private int emetteurId; // ID de l'émetteur (par exemple, un enseignant ou un administrateur)
    private int destinataireId; // ID du destinataire (par exemple, un étudiant ou un enseignant)
    private String destinataireType; // type du destinataire (ETUDIANT, ENSEIGNANT, ADMINISTRATEUR)
    private int coursId; // ID du cours auquel la notification pourrait être liée (si applicable)

    public Notification(int idNotification, LocalDateTime dateCreation, String message, String type, String statut,
                        int emetteurId, int destinataireId, String destinataireType, int coursId) {
        this.idNotification = idNotification;
        this.dateCreation = dateCreation;
        this.message = message;
        this.type = type;
        this.statut = statut;
        this.emetteurId = emetteurId;
        this.destinataireId = destinataireId;
        this.destinataireType = destinataireType;
        this.coursId = coursId;
    }

    // Getters et Setters
    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getEmetteurId() {
        return emetteurId;
    }

    public void setEmetteurId(int emetteurId) {
        this.emetteurId = emetteurId;
    }

    public int getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(int destinataireId) {
        this.destinataireId = destinataireId;
    }

    public String getDestinataireType() {
        return destinataireType;
    }

    public void setDestinataireType(String destinataireType) {
        this.destinataireType = destinataireType;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }
}
