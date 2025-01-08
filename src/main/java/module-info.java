module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires org.pcap4j.core;
    requires java.sql;
    requires itextpdf; // If you are using JDBC for database access

    exports App; // Replace `App` with the actual package containing your `Main` class
}
