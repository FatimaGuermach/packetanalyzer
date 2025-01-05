package services;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private TrafficCaptureService trafficCaptureService;
    private BarChart<String, Number> barChart;
    private XYChart.Series<String, Number> series;

    @Override
    public void start(Stage primaryStage) {
        trafficCaptureService = new TrafficCaptureService("captured_packets.csv");

        // Create the buttons
        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");
        stopButton.setDisable(true);  // Initially disabled

        // Create the bar chart for visualization
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Live Packet Statistics");
        xAxis.setLabel("Packet Type");
        yAxis.setLabel("Count");

        // Initialize data series for the graph
        series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("TCP Packets", 0));
        series.getData().add(new XYChart.Data<>("UDP Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Packets", 0));
        series.getData().add(new XYChart.Data<>("Total Data (KB)", 0));
        barChart.getData().add(series);

        // Start Button Action
        startButton.setOnAction(e -> {
            trafficCaptureService.startCapture();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            updateGraph();  // Start updating the graph
        });

        // Stop Button Action
        stopButton.setOnAction(e -> {
            trafficCaptureService.stopCapture();
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        // Layout Setup
        VBox layout = new VBox(10);
        layout.getChildren().addAll(startButton, stopButton, barChart);

        // Set the Scene
        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setTitle("Traffic Capture with Graph");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to update the graph dynamically
    private void updateGraph() {
        new Thread(() -> {
            while (trafficCaptureService.isCapturing()) {
                try {
                    Thread.sleep(1000); // Update every second
                    int tcpCount = trafficCaptureService.getTcpCount();
                    int udpCount = trafficCaptureService.getUdpCount();
                    int totalPackets = trafficCaptureService.getTotalPacketsCaptured();
                    long totalDataSize = trafficCaptureService.getTotalDataSize();

                    // Update the graph on the JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        series.getData().get(0).setYValue(tcpCount);
                        series.getData().get(1).setYValue(udpCount);
                        series.getData().get(2).setYValue(totalPackets);
                        series.getData().get(3).setYValue(totalDataSize / 1024); // Convert to KB
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
