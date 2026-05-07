module com.example.premierservices {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.graphics;

    opens com.example.premierservices to javafx.fxml;
    opens com.example.premierservices.Controllers to javafx.fxml;
    opens com.example.premierservices.Applications to javafx.fxml;

    exports com.example.premierservices;
    exports com.example.premierservices.Controllers;
    exports com.example.premierservices.Applications;
}