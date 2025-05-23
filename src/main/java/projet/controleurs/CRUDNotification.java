package projet.controleurs;

import projet.models.Notification;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CRUDNotification {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<Notification> lireNotifications(String cheminFichier) throws IOException {
        List<Notification> notifications = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] data = line.split(";");
                if (data.length >= 9) {
                    try {
                        notifications.add(Notification.fromCsv(data));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
        }
        return notifications;
    }

    public static void ajouterNotification(String cheminFichier, Notification notification) throws IOException {
        boolean fileExists = new File(cheminFichier).exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier, true))) {
            if (!fileExists || new File(cheminFichier).length() == 0) {
                writer.write("idNotification;dateHeure;message;type;statut;idUtilisateurEmetteur;idDestinataire;typeDestinataire;idCoursConcerne\n");
            }
            writer.write(notification.toCsvString() + "\n");
        }
    }

    public static void ecrireNotifications(String cheminFichier, List<Notification> notifications) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier, false))) {
            writer.write("idNotification;dateHeure;message;type;statut;idUtilisateurEmetteur;idDestinataire;typeDestinataire;idCoursConcerne\n");

            for (Notification notif : notifications) {
                writer.write(notif.toCsvString() + "\n");
            }
        }
    }
}
