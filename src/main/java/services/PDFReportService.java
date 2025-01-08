package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import model.PacketEntity;
import model.ThreatEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFReportService {

    public void generateReportWithGraph(String filePath, List<PacketEntity> packets, List<ThreatEntity> threats, File graphFile) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Traffic Capture Report"));
            document.add(new Paragraph(" "));

            // Ajouter le graphique
            if (graphFile.exists()) {
                Image graph = Image.getInstance(graphFile.getAbsolutePath());
                graph.scaleToFit(500, 300);
                document.add(graph);
                document.add(new Paragraph(" ")); // Espace entre le graphique et le texte
            }

            document.add(new Paragraph("Packet Statistics"));
            document.add(new Paragraph("Total Packets: " + packets.size()));
            document.add(new Paragraph("Suspicious Packets: " + threats.size()));
            document.add(new Paragraph(" "));

            // Ajouter les données des paquets récurrents
            document.add(new Paragraph("Recurrent Packets:"));
            for (PacketEntity packet : packets) {
                document.add(new Paragraph(" - Source IP: " + packet.getSourceIp()
                        + ", Destination IP: " + packet.getDestinationIp()
                        + ", Count: " + packet.getPacketSize())); // Utilisé pour afficher le nombre de paquets
            }

            document.add(new Paragraph(" "));

            // Ajouter les données des menaces détectées
            document.add(new Paragraph("Suspicious Packets:"));
            for (ThreatEntity threat : threats) {
                document.add(new Paragraph(" - " + threat.getThreatLevel()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
