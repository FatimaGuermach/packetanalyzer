package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import model.PacketEntity;
import model.ThreatEntity;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFReportService {

    public void generateReportWithGraph(String filePath, List<PacketEntity> packets, List<ThreatEntity> threats, WritableImage graphImage) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Traffic Capture Report"));
            document.add(new Paragraph(" "));

            // Ajouter le graphique
            File graphFile = new File("graph.png");
            ImageIO.write(SwingFXUtils.fromFXImage(graphImage, null), "png", graphFile);
            Image graph = Image.getInstance(graphFile.getAbsolutePath());
            graph.scaleToFit(500, 300);
            document.add(graph);

            document.add(new Paragraph("Packet Statistics"));
            document.add(new Paragraph("Total Packets: " + packets.size()));
            document.add(new Paragraph("Suspicious Packets: " + threats.size()));
            document.add(new Paragraph(" "));

            // Ajouter les données des tableaux
            document.add(new Paragraph("Recurrent Packets:"));
            for (PacketEntity packet : packets) {
                document.add(new Paragraph(packet.toString()));
            }

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Suspicious Packets:"));
            for (ThreatEntity threat : threats) {
                document.add(new Paragraph(threat.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
