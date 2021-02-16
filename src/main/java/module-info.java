module GB.java3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires sqlite.jdbc;

    opens GB.client to javafx.fxml;
    exports GB.client;
}