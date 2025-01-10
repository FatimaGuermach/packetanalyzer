package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import model.PacketEntity;
import model.ThreatEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFReportService {

    public void generateReportWithGraph(String filePath, int totalPackets, List<PacketEntity> packets, List<ThreatEntity> threats, File graphFile) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Ajouter le logo
            Image logo = Image.getInstance("logo.jpg"); // Le chemin vers le fichier logo
            logo.scaleToFit(150, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            document.add(new Paragraph(" ")); // Espace

            // Ajouter le graphique
            if (graphFile.exists()) {
                Image graph = Image.getInstance(graphFile.getAbsolutePath());
                graph.scaleToFit(500, 300);
                graph.setAlignment(Element.ALIGN_CENTER);
                document.add(graph);
            }

            document.add(new Paragraph(" ")); // Espace

            // Ajouter les statistiques des paquets
            document.add(new Paragraph("Analyzer Statistics"));
            document.add(new Paragraph(" - Total Packets: " + totalPackets));
            document.add(new Paragraph(" - Total Data Size: " + calculateTotalDataSize(packets) + " KB"));
            document.add(new Paragraph(" - Suspicious Packets: " + threats.size()));

            document.add(new Paragraph(" ")); // Espace

            // Ajouter les paquets récurrents
            document.add(new Paragraph("Recurrent Packets:"));
            PdfPTable packetTable = new PdfPTable(6); // Six colonnes : Source IP, Destination IP, Protocol, Source Port, Destination Port, Packet Size
            packetTable.addCell("Source IP");
            packetTable.addCell("Destination IP");
            packetTable.addCell("Protocol");
            packetTable.addCell("Source Port");
            packetTable.addCell("Destination Port");
            packetTable.addCell("Packet Size");
            for (PacketEntity packet : packets) {
                packetTable.addCell(packet.getSourceIp());
                packetTable.addCell(packet.getDestinationIp());
                packetTable.addCell(packet.getProtocol());
                packetTable.addCell(String.valueOf(packet.getSourcePort()));
                packetTable.addCell(String.valueOf(packet.getDestinationPort()));
                packetTable.addCell(String.valueOf(packet.getPacketSize()));
            }
            document.add(packetTable);

            document.add(new Paragraph(" ")); // Espace

            // Ajouter les paquets suspects
            document.add(new Paragraph("Suspicious Packets:"));
            PdfPTable threatTable = new PdfPTable(1); // Une colonne : Threat Level
            threatTable.addCell("Threat Level");
            for (ThreatEntity threat : threats) {
                threatTable.addCell(threat.getThreatLevel());
            }
            document.add(threatTable);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private int calculateTotalDataSize(List<PacketEntity> packets) {
        return packets.stream().mapToInt(PacketEntity::getPacketSize).sum(); // Convertir en KB
    }

}
