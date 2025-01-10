package App;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.LogEntity;
import model.PacketEntity;
import model.ThreatEntity;
import repository.DatabaseManager;
import repository.LogRepository;
import repository.PacketRepository;
import repository.ThreatRepository;
import repository.ThreatPacketsRepository;
import services.PDFReportService;
import services.TrafficAnalysisService;
import services.TrafficCaptureService;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class Main extends Application {

    private int currentLogId;
    private LocalDateTime currentLogTimestamp;
    private TrafficCaptureService trafficCaptureService;
    private TrafficAnalysisService trafficAnalysisService;
    private BarChart<String, Number> barChart;
    private XYChart.Series<String, Number> series;
    private ListView<PacketEntity> recurrentPacketsListView;
    private ListView<ThreatEntity> suspiciousPacketsListView;
    private List<PacketEntity> recurrentPacketsList;
    private List<ThreatEntity> suspiciousPacketsList;

    @Override
    public void start(Stage primaryStage) {

        // Initialisation des repositories
        PacketRepository packetRepository = new PacketRepository();
        ThreatRepository threatRepository = new ThreatRepository();
        LogRepository logRepository = new LogRepository();
        ThreatPacketsRepository threatPacketsRepository = new ThreatPacketsRepository();

        // Initialisation des services
        trafficCaptureService = new TrafficCaptureService(packetRepository, logRepository);

        LogEntity logEntity = new LogEntity(0, LocalDateTime.now());
        try {
            logRepository.createLog(logEntity);
            currentLogId = logEntity.getId();
            currentLogTimestamp = logEntity.getLaunchTime();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        trafficAnalysisService = new TrafficAnalysisService(packetRepository, threatRepository, threatPacketsRepository, currentLogId);

        // Boutons
        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");
        Button generateReportButton = new Button("Generate Report");
        stopButton.setDisable(true);

        // BarChart configuration
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Live Packet Statistics");
        xAxis.setLabel("Packet Type");
        yAxis.setLabel("Count");

        series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("IPv4 Packets", 0));
        series.getData().add(new XYChart.Data<>("TCP Packets", 0));
        series.getData().add(new XYChart.Data<>("UDP Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Data (KB)", 0));
        barChart.getData().add(series);

        setupLists();

        // Align buttons horizontally
        HBox buttonBox = new HBox(10, startButton, stopButton, generateReportButton);
        buttonBox.setAlignment(Pos.CENTER);

        startButton.setOnAction(e -> {
            trafficCaptureService.startCapture();
            trafficAnalysisService.startAnalysis();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            startRealTimeUpdates();
        });

        stopButton.setOnAction(e -> {
            trafficCaptureService.stopCapture();
            trafficAnalysisService.stopAnalysis();
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        generateReportButton.setOnAction(e -> generateReport());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(buttonBox, barChart, recurrentPacketsListView, suspiciousPacketsListView);

        Scene scene = new Scene(layout, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Traffic Capture with Analysis Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> DatabaseManager.closeConnection());
    }

    private void setupLists() {
        recurrentPacketsListView = new ListView<>();
        suspiciousPacketsListView = new ListView<>();
    }

    private void startRealTimeUpdates() {
        new Thread(() -> {
            while (trafficCaptureService.isCapturing()) {
                Platform.runLater(() -> {
                    updateGraph(); // Met à jour le graphique
                    updateLists(); // Met à jour les listes
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

    private void updateLists() {
        recurrentPacketsList = trafficAnalysisService.getRecurrentPackets(currentLogId);
        suspiciousPacketsList = trafficAnalysisService.getThreatsByLogId(currentLogId);

        if (recurrentPacketsList != null) {
            recurrentPacketsListView.getItems().clear();
            recurrentPacketsListView.getItems().addAll(recurrentPacketsList);
        } else {
            recurrentPacketsListView.getItems().clear();
        }

        if (suspiciousPacketsList != null) {
            suspiciousPacketsListView.getItems().clear();
            suspiciousPacketsListView.getItems().addAll(suspiciousPacketsList);
        } else {
            suspiciousPacketsListView.getItems().clear();
        }
    }

    private void generateReport() {
        WritableImage graphImage = barChart.snapshot(new SnapshotParameters(), null);
        File graphFile = new File("graph.png");
        try {
            ImageIO.write(toBufferedImage(graphImage), "png", graphFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<PacketEntity> recurrentPackets = recurrentPacketsListView.getItems();
        List<ThreatEntity> suspiciousPackets = suspiciousPacketsListView.getItems();

        String reportFileName = "traffic_report_" + currentLogTimestamp.toString().replace(":", "-") + ".pdf";
        PDFReportService reportService = new PDFReportService();
        reportService.generateReportWithGraph(reportFileName, trafficCaptureService.getTotalPacketsCaptured(), recurrentPackets, suspiciousPackets, graphFile);
    }

    private java.awt.image.BufferedImage toBufferedImage(WritableImage img) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();

        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                javafx.scene.paint.Color fxColor = img.getPixelReader().getColor(x, y);
                int color = ((int) (fxColor.getOpacity() * 255) << 24) |
                        ((int) (fxColor.getRed() * 255) << 16) |
                        ((int) (fxColor.getGreen() * 255) << 8) |
                        ((int) (fxColor.getBlue() * 255));
                bufferedImage.setRGB(x, y, color);
            }
        }
        return bufferedImage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
