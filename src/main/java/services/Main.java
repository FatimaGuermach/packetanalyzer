package services;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    private TrafficCaptureService trafficCaptureService;

    @Override
    public void start(Stage primaryStage) {
        // Create an instance of the TrafficCaptureService
        trafficCaptureService = new TrafficCaptureService("captured_packes.csv");

        // Create the start and stop buttons
        Button startButton = new Button("Start Capture");
        Button stopButton = new Button("Stop Capture");

        startButton.setOnAction(e -> {
            trafficCaptureService.startCapture();
            startButton.setDisable(true); // Disable the start button while capturing
            stopButton.setDisable(false); // Enable the stop button when capturing
        });

        stopButton.setOnAction(e -> {
            trafficCaptureService.stopCapture();
            startButton.setDisable(false); // Enable the start button after stopping
            stopButton.setDisable(true); // Disable the stop button after stopping
        });
        stopButton.setDisable(true); // Disable the stop button initially

        // Create a StackPane layout and add buttons
        StackPane root = new StackPane();
        root.getChildren().addAll(startButton, stopButton);

        // Create and set the scene
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Traffic Capture Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
