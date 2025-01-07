package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainController {
    @FXML
    private TableView<?> trafficTable;
    @FXML
    private TableColumn<?, ?> srcIpColumn;
    @FXML
    private TableColumn<?, ?> dstIpColumn;
    @FXML
    private TableColumn<?, ?> typeColumn;
    @FXML
    private Label alertLabel;

    public void startMonitoring() {
        alertLabel.setText("Surveillance en cours...");
        alertLabel.setText("-fx-text-fill: blue;");
        // Ajouter le code pour démarrer la surveillance réseau
    }
}
