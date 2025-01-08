import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.PacketEntity;
import model.ThreatEntity;
import repository.LogRepository;
import repository.PacketRepository;
import repository.ThreatRepository;
import services.PDFReportService;
import services.TrafficAnalysisService;
import services.TrafficCaptureService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main extends Application {

    private TrafficCaptureService trafficCaptureService;
    private TrafficAnalysisService trafficAnalysisService;
    private BarChart<String, Number> barChart;
    private XYChart.Series<String, Number> series;
    private TableView<PacketEntity> recurrentPacketsTable;
    private TableView<ThreatEntity> suspiciousPacketsTable;

    @Override
    public void start(Stage primaryStage) {
        // Connexion à la base de données
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/traffic_db", "username", "password");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }

        // Initialisation des repositories
        PacketRepository packetRepository = new PacketRepository();
        ThreatRepository threatRepository = new ThreatRepository();
        LogRepository logRepository = new LogRepository();

        // Initialisation des services
        trafficCaptureService = new TrafficCaptureService(packetRepository, logRepository);

        // Obtenir le logId pour la session en cours
        int currentLogId;
        try {
            currentLogId = logRepository.getCurrentLogId();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve current log ID", e);
        }
        trafficAnalysisService = new TrafficAnalysisService(packetRepository, threatRepository, currentLogId);

        // Boutons
        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");
        Button generateReportButton = new Button("Generate Report");
        stopButton.setDisable(true);

        // BarChart : configuration des axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Live Packet Statistics");
        xAxis.setLabel("Packet Type");
        yAxis.setLabel("Count");

        // Initialisation des données du graphe
        series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("IPv4 Packets", 0));
        series.getData().add(new XYChart.Data<>("TCP Packets", 0));
        series.getData().add(new XYChart.Data<>("UDP Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Data (KB)", 0));
        barChart.getData().add(series);

        // Initialisation des tableaux
        setupTables();

        // Actions des boutons
        startButton.setOnAction(e -> {
            trafficCaptureService.startCapture();
            trafficAnalysisService.startAnalysis(); // Démarrer l'analyse
            startButton.setDisable(true);
            stopButton.setDisable(false);
            startRealTimeUpdates();
        });

        stopButton.setOnAction(e -> {
            trafficCaptureService.stopCapture();
            trafficAnalysisService.stopAnalysis(); // Arrêter l'analyse
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        generateReportButton.setOnAction(e -> generateReport());

        // Mise en page
        VBox layout = new VBox(10);
        layout.getChildren().addAll(startButton, stopButton, generateReportButton, barChart, recurrentPacketsTable, suspiciousPacketsTable);

        // Configuration de la scène
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setTitle("Traffic Capture with Analysis Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupTables() {
        recurrentPacketsTable = new TableView<>();
        suspiciousPacketsTable = new TableView<>();

        // Colonnes pour les paquets récurrents
        TableColumn<PacketEntity, String> sourceIpColumn = new TableColumn<>("Source IP");
        sourceIpColumn.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));

        TableColumn<PacketEntity, String> destinationIpColumn = new TableColumn<>("Destination IP");
        destinationIpColumn.setCellValueFactory(new PropertyValueFactory<>("destinationIp"));

        TableColumn<PacketEntity, Integer> packetCountColumn = new TableColumn<>("Packet Count");
        packetCountColumn.setCellValueFactory(new PropertyValueFactory<>("packetSize")); // Utilisé pour afficher le nombre de paquets

        recurrentPacketsTable.getColumns().addAll(sourceIpColumn, destinationIpColumn, packetCountColumn);

        // Colonnes pour les paquets suspects
        TableColumn<ThreatEntity, String> threatLevelColumn = new TableColumn<>("Threat Level");
        threatLevelColumn.setCellValueFactory(new PropertyValueFactory<>("threatLevel"));

        suspiciousPacketsTable.getColumns().add(threatLevelColumn);
    }

    private void startRealTimeUpdates() {
        new Thread(() -> {
            while (trafficCaptureService.isCapturing()) {
                Platform.runLater(() -> {
                    updateGraph();
                    updateTables();
                });
                try {
                    Thread.sleep(1000); // Mise à jour toutes les secondes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void updateGraph() {
        series.getData().get(0).setYValue(trafficCaptureService.getIpv4Count());
        series.getData().get(1).setYValue(trafficCaptureService.getTcpCount());
        series.getData().get(2).setYValue(trafficCaptureService.getUdpCount());
        series.getData().get(3).setYValue(trafficCaptureService.getTotalPacketsCaptured());
        series.getData().get(4).setYValue(trafficCaptureService.getTotalDataSize() / 1024); // Convertir en KB
    }

    private void updateTables() {
        List<PacketEntity> recurrentPackets = trafficAnalysisService.getRecurrentPackets();
        List<ThreatEntity> suspiciousPackets = trafficAnalysisService.getThreats();
        recurrentPacketsTable.getItems().setAll(recurrentPackets);
        suspiciousPacketsTable.getItems().setAll(suspiciousPackets);
    }

    private void generateReport() {
        WritableImage graphImage = barChart.snapshot(null, null);
        List<PacketEntity> recurrentPackets = recurrentPacketsTable.getItems();
        List<ThreatEntity> suspiciousPackets = suspiciousPacketsTable.getItems();

        PDFReportService reportService = new PDFReportService();
        reportService.generateReportWithGraph("traffic_report.pdf", recurrentPackets, suspiciousPackets, graphImage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
