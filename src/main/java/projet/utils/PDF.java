package projet.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import projet.models.Cours;
import projet.models.Utilisateur;

import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream; // ✅ à ne pas oublier

public class PDF {

    public static void genererEmploiDuTempsHebdomadaire(Utilisateur utilisateur, List<Cours> tousLesCours, String cheminFichier) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Emploi du temps de la semaine", titreFont));
            document.add(new Paragraph("Nom : " + utilisateur.getNom(), normalFont));
            document.add(new Paragraph("Email : " + utilisateur.getEmail(), normalFont));
            document.add(new Paragraph("Rôle : " + utilisateur.getRole(), normalFont));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5); // Date, Cours, Horaire, Salle, Enseignant
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            Stream.of("Date", "Cours", "Horaire", "Salle", "Enseignant").forEach(titre -> {
                PdfPCell cell = new PdfPCell(new Phrase(titre, titreFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            });

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
