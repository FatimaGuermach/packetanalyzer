package model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class KDDTrain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        TableView<KDDRecord> table = new TableView<>();
        ObservableList<KDDRecord> data = FXCollections.observableArrayList();

        // Define columns
        TableColumn<KDDRecord, String> feature1Col = new TableColumn<>("Feature 1");
        feature1Col.setCellValueFactory(new PropertyValueFactory<>("feature1"));

        TableColumn<KDDRecord, String> feature2Col = new TableColumn<>("Feature 2");
        feature2Col.setCellValueFactory(new PropertyValueFactory<>("feature2"));

        table.getColumns().addAll(feature1Col, feature2Col);

        // Load data from CSV
        String csvFile = "C:\\Users\\HP\\Desktop\\nsl-kdd\\KDDTrain+.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming the CSV has at least 2 values per line
                String[] values = line.split(",");
                if (values.length >= 2) {
                    data.add(new KDDRecord(values[0], values[1]));
                } else {
                    // Handle invalid or incomplete data (optional)
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        table.setItems(data);

        // Set up the scene and stage
        Scene scene = new Scene(table, 600, 400);
        stage.setScene(scene);
        stage.setTitle("NSL-KDD Data Viewer");
        stage.show();
    }
}
