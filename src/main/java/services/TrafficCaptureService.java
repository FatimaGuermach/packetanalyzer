package services;

import model.PacketEntity;
import repository.PacketRepository;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TrafficCaptureService {
    private final PacketRepository packetRepository;
    private volatile boolean capturing = false;

    public TrafficCaptureService(PacketRepository packetRepository) {
        this.packetRepository = packetRepository;
    }

    public void startCapture() {
        capturing = true;

        new Thread(() -> {
            try {
                PcapNetworkInterface nif = getNetworkInterface();
                if (nif == null) {
                    System.out.println("No suitable network interface found.");
                    return;
                }

                try (PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10)) {
                    handle.loop(-1, (PacketListener) packet -> {
                        if (!capturing) {
                            try {
                                handle.breakLoop();
                            } catch (NotOpenException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        PacketEntity entity = parsePacket(packet);
                        if (entity != null) {
                            try {
                                packetRepository.createPacket(entity);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } catch (PcapNativeException | NotOpenException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopCapture() {
        capturing = false;
    }

    private PcapNetworkInterface getNetworkInterface() throws PcapNativeException {
        return Pcaps.findAllDevs().stream()
                .filter(dev -> dev.getName().toLowerCase().contains("wifi"))
                .findFirst()
                .orElse(null);
    }

    private PacketEntity parsePacket(Packet packet) {
        try {
            String sourceIp = "Unknown";
            String destinationIp = "Unknown";
            int packetSize = packet.length();
            String protocol = "Unknown";
            Integer sourcePort = null;
            Integer destinationPort = null;

            if (packet.contains(IpV4Packet.class)) {
                IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);
                sourceIp = ipv4Packet.getHeader().getSrcAddr().toString();
                destinationIp = ipv4Packet.getHeader().getDstAddr().toString();
                protocol = ipv4Packet.getHeader().getProtocol().toString();
            }

            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
                destinationPort = tcpPacket.getHeader().getDstPort().valueAsInt();
                protocol = "TCP";
            } else if (packet.contains(UdpPacket.class)) {
                UdpPacket udpPacket = packet.get(UdpPacket.class);
                sourcePort = udpPacket.getHeader().getSrcPort().valueAsInt();
                destinationPort = udpPacket.getHeader().getDstPort().valueAsInt();
                protocol = "UDP";
            }

            return new PacketEntity(0, sourceIp, destinationIp, packetSize, protocol, sourcePort, destinationPort,
                    LocalDateTime.now(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
