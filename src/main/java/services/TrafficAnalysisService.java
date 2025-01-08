package services;

import model.PacketEntity;
import model.ThreatEntity;
import repository.PacketRepository;
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
    private final Map<String, Integer> ipRequestCounts = new HashMap<>();
    private final int currentLogId;

    private static final int SUSPICIOUS_REQUEST_THRESHOLD = 100; // Nombre élevé de paquets
    private static final int SUSPICIOUS_PACKET_SIZE_THRESHOLD = 1024 * 1024; // Taille > 1 MB

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread updateThread;
    private List<PacketEntity> topDestinations = new ArrayList<>();

    public TrafficAnalysisService(PacketRepository packetRepository, ThreatRepository threatRepository, int currentLogId) {
        this.packetRepository = packetRepository;
        this.threatRepository = threatRepository;
        this.currentLogId = currentLogId;
    }

    /**
     * Renvoie une copie synchronisée des paquets récurrents.
     */
    public synchronized List<PacketEntity> getRecurrentPackets() {
        return new ArrayList<>(topDestinations);
    }

    /**
     * Démarre l'analyse continue dans un sous-thread.
     */
    public void startAnalysis() {
        if (running.get()) {
            System.out.println("Analysis already running.");
            return;
        }

        running.set(true);
        updateThread = new Thread(() -> {
            while (running.get()) {
                try {
                    // Mettre à jour la liste des top destinations
                    updateTopDestinations();
                    // Analyser les paquets de la session
                    analyzePackets();
                    Thread.sleep(1000); // Mise à jour toutes les secondes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Arrête l'analyse continue.
     */
    public void stopAnalysis() {
        running.set(false);
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    /**
     * Met à jour la liste des destinations les plus fréquentes dans la session actuelle.
     */
    private synchronized void updateTopDestinations() {
        try {
            topDestinations = packetRepository.findTopDestinations(currentLogId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyse les paquets de la session et détermine s'ils représentent une menace.
     */
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

    /**
     * Renvoie la liste des menaces détectées.
     */
    public List<ThreatEntity> getThreats() {
        try {
            return threatRepository.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
