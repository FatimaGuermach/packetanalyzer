module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing; // Nécessaire pour utiliser javafx.embed.swing.SwingFXUtils
    requires java.sql;
    requires itextpdf; // Nécessaire pour utiliser iText pour les PDF
    requires org.pcap4j.core; // Nécessaire pour la capture de paquets réseau
    requires java.desktop; // Nécessaire pour utiliser des fonctionnalités comme `ImageIO`

    exports services; // Exporte le package "services"
    exports model; // Exporte le package "model"
    opens model to javafx.fxml; // Ouvre "model" pour les opérations de réflexion avec JavaFX
    opens org.example.demo1 to javafx.fxml; // Ouvre "org.example.demo1" pour JavaFX
}
