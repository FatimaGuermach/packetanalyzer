module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.controlsEmpty;
    exports services;
    requires org.pcap4j.core;
    requires java.sql;
    opens org.example.demo1 to javafx.fxml;
    exports org.example.demo1;
    exports model;
    opens model to javafx.fxml;

}

