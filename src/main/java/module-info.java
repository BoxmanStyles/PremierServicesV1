module com.example.premierservices {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Exportar paquetes necesarios
    exports com.example.premierservices;
    exports com.example.premierservices.Controllers;
    exports com.example.premierservices.Models;
    exports com.example.premierservices.Applications;   // NUEVO

    // Abrir paquetes para reflexión (JavaFX)
    opens com.example.premierservices to javafx.fxml;
    opens com.example.premierservices.Controllers to javafx.fxml;
    opens com.example.premierservices.Models to javafx.base, javafx.fxml;
    opens com.example.premierservices.Applications to javafx.graphics;   // NUEVO
}