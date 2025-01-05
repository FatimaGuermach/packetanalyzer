package model;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;

public class PacketAnalyzer{
    public static void main(String[] args) {
        try {
            // Récupérer la première interface réseau disponible
            PcapNetworkInterface nif = Pcaps.findAllDevs().get(6);
            if (nif == null) {
                System.out.println("No network interfaces found!");
                return;
            }
            System.out.println("Using interface: " + nif.getName());

            // Configurer les paramètres de capture
            int snapshotLength = 65536; // Taille maximale du paquet
            int timeout = 10; // Délai en millisecondes
            PcapHandle handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

            System.out.println("Listening for packets...");

            // Capturer 10 paquets
            handle.loop(10, new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    System.out.println("===== New Packet Captured =====");

                    // Afficher les données brutes du paquet
                    System.out.println("Raw Data: " + bytesToHex(packet.getRawData()));

                    // Analyse des couches de protocoles
                    if (packet.contains(EthernetPacket.class)) {
                        EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
                        System.out.println("Ethernet Header:");
                        System.out.println(" - Source MAC: " + ethernetPacket.getHeader().getSrcAddr());
                        System.out.println(" - Destination MAC: " + ethernetPacket.getHeader().getDstAddr());
                        System.out.println(" - Type: " + ethernetPacket.getHeader().getType());
                    }

                    if (packet.contains(IpV4Packet.class)) {
                        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                        System.out.println("IPv4 Header:");
                        System.out.println(" - Source IP: " + ipV4Packet.getHeader().getSrcAddr());
                        System.out.println(" - Destination IP: " + ipV4Packet.getHeader().getDstAddr());
                        System.out.println(" - Protocol: " + ipV4Packet.getHeader().getProtocol());
                    }

                    if (packet.contains(TcpPacket.class)) {
                        TcpPacket tcpPacket = packet.get(TcpPacket.class);
                        System.out.println("TCP Header:");
                        System.out.println(" - Source Port: " + tcpPacket.getHeader().getSrcPort());
                        System.out.println(" - Destination Port: " + tcpPacket.getHeader().getDstPort());
                    } else if (packet.contains(UdpPacket.class)) {
                        UdpPacket udpPacket = packet.get(UdpPacket.class);
                        System.out.println("UDP Header:");
                        System.out.println(" - Source Port: " + udpPacket.getHeader().getSrcPort());
                        System.out.println(" - Destination Port: " + udpPacket.getHeader().getDstPort());
                    }
                }
            });

            // Fermer la session de capture
            handle.close();
        } catch (PcapNativeException | NotOpenException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour convertir les données brutes en hexadécimal
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
