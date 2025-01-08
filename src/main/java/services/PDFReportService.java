package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import model.PacketEntity;

import java.io.FileOutputStream;
import java.util.List;

public class PDFReportService {
    public void generateReport(String filePath, List<PacketEntity> packets) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Packet Traffic Report"));
            PdfPTable table = new PdfPTable(6);
            table.addCell("Source IP");
            table.addCell("Destination IP");
            table.addCell("Size");
            table.addCell("Protocol");
            table.addCell("Source Port");
            table.addCell("Destination Port");

            for (PacketEntity packet : packets) {
                table.addCell(packet.getSourceIp());
                table.addCell(packet.getDestinationIp());
                table.addCell(String.valueOf(packet.getPacketSize()));
                table.addCell(packet.getProtocol());
                table.addCell(String.valueOf(packet.getSourcePort()));
                table.addCell(String.valueOf(packet.getDestinationPort()));
            }

            document.add(table);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
