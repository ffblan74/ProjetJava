package projet.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int idNotification;
    private LocalDateTime dateHeure;
    private String message;
    private String type;
    private String statut;
    private int idUtilisateurEmetteur;
    private int idDestinataire;
    private String typeDestinataire;
    private int idCoursConcerne;

    public Notification(int idNotification, LocalDateTime dateHeure, String message, String type,
                        String statut, int idUtilisateurEmetteur, int idDestinataire,
                        String typeDestinataire, int idCoursConcerne) {
        this.idNotification = idNotification;
        this.dateHeure = dateHeure;
        this.message = message;
        this.type = type;
        this.statut = statut;
        this.idUtilisateurEmetteur = idUtilisateurEmetteur;
        this.idDestinataire = idDestinataire;
        this.typeDestinataire = typeDestinataire;
        this.idCoursConcerne = idCoursConcerne;
    }

    public int getIdNotification() { return idNotification; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getStatut() { return statut; }
    public int getIdUtilisateurEmetteur() { return idUtilisateurEmetteur; }
    public int getIdDestinataire() { return idDestinataire; }
    public String getTypeDestinataire() { return typeDestinataire; }
    public int getIdCoursConcerne() { return idCoursConcerne; }

    public void setStatut(String statut) { this.statut = statut; }

    public static Notification fromCsv(String[] data) {
        if (data.length < 9) {
            throw new IllegalArgumentException("Données CSV de notification insuffisantes.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new Notification(
                Integer.parseInt(data[0]),
                LocalDateTime.parse(data[1], formatter),
                data[2].replace("\\;", ";"),
                data[3],
                data[4],
                Integer.parseInt(data[5]),
                Integer.parseInt(data[6]),
                data[7],
                Integer.parseInt(data[8])
        );
    }

    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%d;%s;%s;%s;%s;%d;%d;%s;%d",
                idNotification,
                dateHeure.format(formatter),
                message.replace(";", "\\;"),
                type,
                statut,
                idUtilisateurEmetteur,
                idDestinataire,
                typeDestinataire,
                idCoursConcerne
        );
    }
}