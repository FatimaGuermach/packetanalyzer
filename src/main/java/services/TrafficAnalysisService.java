package services;

import model.PacketEntity;
import model.ThreatEntity;
import model.ThreatPacketEntity;
import repository.PacketRepository;
import repository.ThreatPacketsRepository;
import repository.ThreatRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrafficAnalysisService {
    private final PacketRepository packetRepository;
    private final ThreatRepository threatRepository;
    private final ThreatPacketsRepository threatPacketsRepository;
    private final Map<String, Integer> ipRequestCounts = new HashMap<>();
    private final int currentLogId;

    private static final int SUSPICIOUS_REQUEST_THRESHOLD = 100; // Nombre élevé de paquets
    private static final int SUSPICIOUS_PACKET_SIZE_THRESHOLD = 1024 * 1024; // Taille > 1 MB

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread updateThread;
    private List<PacketEntity> topDestinations = new ArrayList<>();

    public TrafficAnalysisService(PacketRepository packetRepository, ThreatRepository threatRepository, ThreatPacketsRepository threatPacketsRepository, int currentLogId) {
        this.packetRepository = packetRepository;
        this.threatRepository = threatRepository;
        this.threatPacketsRepository = threatPacketsRepository;
        this.currentLogId = currentLogId;
    }

    public synchronized List<PacketEntity> getRecurrentPackets(int id) {
        List<PacketEntity> packets = null;
        try {
            packets = packetRepository.findTopDestinations(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return packets != null ? packets : List.of(); // Retourne une liste vide si aucun paquet n'est trouvé
    }

    public List<ThreatEntity> getThreats() {
        List<ThreatEntity> threats = null;
        try {
            threats = threatRepository.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return threats != null ? threats : List.of(); // Retourne une liste vide si aucune menace n'est trouvée
    }

    public List<ThreatEntity> getThreatsByLogId(int logId) {
        List<ThreatEntity> threats = null;
        try {
            threats = threatRepository.findThreatsByLogId(logId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return threats != null ? threats : List.of(); // Retourne une liste vide si aucune menace n'est trouvée
    }

    public void startAnalysis() {
        if (running.get()) {
            System.out.println("Analysis already running.");
            return;
        }

        running.set(true);
        updateThread = new Thread(() -> {
            while (running.get()) {
                try {
                    updateTopDestinations(); // Mettre à jour la liste des top destinations
                    analyzePackets(); // Analyser les paquets de la session
                    Thread.sleep(1000); // Mise à jour toutes les secondes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopAnalysis() {
        running.set(false);
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    private synchronized void updateTopDestinations() {
        try {
            topDestinations = packetRepository.findTopDestinations(currentLogId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void analyzePackets() {
        try {
            List<PacketEntity> sessionPackets = packetRepository.findPacketsByLogId(currentLogId);
            for (PacketEntity packet : sessionPackets) {
                String sourceIp = packet.getSourceIp();
                ipRequestCounts.put(sourceIp, ipRequestCounts.getOrDefault(sourceIp, 0) + 1);

                boolean isSuspicious = ipRequestCounts.get(sourceIp) > SUSPICIOUS_REQUEST_THRESHOLD
                        || packet.getPacketSize() > SUSPICIOUS_PACKET_SIZE_THRESHOLD;

                if (isSuspicious) {
                    ThreatEntity threat = new ThreatEntity(0, "Suspicious IP: " + sourceIp);
                    try {
                        threatRepository.createThreat(threat);
                        ThreatPacketEntity threatPacket = new ThreatPacketEntity(0, threat.getId(), packet.getId());
                        threatPacketsRepository.createThreatPacket(threatPacket);
                        System.out.println("Threat detected for IP: " + sourceIp);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
