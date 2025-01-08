package services;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private TrafficCaptureService trafficCaptureService;
    private BarChart<String, Number> barChart;
    private XYChart.Series<String, Number> series;
    private volatile Thread graphUpdateThread; // Volatile pour éviter des conflits de threads

    @Override
    public void start(Stage primaryStage) {
        // Initialisation du service de capture
        trafficCaptureService = new TrafficCaptureService("captured_packets.csv");

        // Boutons
        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");
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
        series.getData().add(new XYChart.Data<>("TCP Packets", 0));
        series.getData().add(new XYChart.Data<>("UDP Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Data (KB)", 0));
        barChart.getData().add(series);

        // Action du bouton "Start"
        startButton.setOnAction(e -> {
            trafficCaptureService.startCapture();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            startGraphUpdateThread();
        });

        // Action du bouton "Stop"
        stopButton.setOnAction(e -> {
            trafficCaptureService.stopCapture();
            stopGraphUpdateThread();
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        // Mise en page
        VBox layout = new VBox(10);
        layout.getChildren().addAll(startButton, stopButton, barChart);

        // Configuration de la scène
        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setTitle("Traffic Capture with Graph");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGraphUpdateThread() {
        graphUpdateThread = new Thread(() -> {
            while (trafficCaptureService.isCapturing()) {
                try {
                    // Délai d'une seconde entre chaque mise à jour
                    Thread.sleep(1000);

                    // Récupérer les statistiques
                    int tcpCount = trafficCaptureService.getTcpCount();
                    int udpCount = trafficCaptureService.getUdpCount();
                    int totalPackets = trafficCaptureService.getTotalPacketsCaptured();
                    long totalDataSize = trafficCaptureService.getTotalDataSize();

                    // Mettre à jour le graphique sur le thread JavaFX
                    Platform.runLater(() -> {
                        try {
                            series.getData().get(0).setYValue(tcpCount);
                            series.getData().get(1).setYValue(udpCount);
                            series.getData().get(2).setYValue(totalPackets);
                            series.getData().get(3).setYValue(totalDataSize / 1024); // Convertir bytes en KB
                        } catch (IndexOutOfBoundsException ignored) {
                            // En cas de suppression de série ou problème d'indexation
                        }
                    });
                } catch (InterruptedException e) {
                    // Interruption normale lors de l'arrêt
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        graphUpdateThread.setDaemon(true); // Pour que le thread s'arrête avec l'application
        graphUpdateThread.start();
    }

    private void stopGraphUpdateThread() {
        if (graphUpdateThread != null && graphUpdateThread.isAlive()) {
            graphUpdateThread.interrupt(); // Interrompre le thread
        }
        graphUpdateThread = null; // Libérer les ressources
    }

    public static void main(String[] args) {
        launch(args);
    }
}
