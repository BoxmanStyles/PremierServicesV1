module com.example.premierservices {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jbcrypt;
    requires jasperreports;
    requires java.mail;
    requires org.apache.pdfbox;  // Sin comillas funciona en Java 9+

    exports com.example.premierservices;
    exports com.example.premierservices.Models;
    exports com.example.premierservices.Controllers.Admin;
    exports com.example.premierservices.Controllers.GlobalController;
    exports com.example.premierservices.Controllers.Clientes;
    exports com.example.premierservices.Controllers.Proveedores;
    exports com.example.premierservices.Applications.GlobalController;
    exports com.example.premierservices.Applications.Admin;
    exports com.example.premierservices.Applications.Cliente;
    exports com.example.premierservices.Applications.Proveedor;
    exports com.example.premierservices.Controllers.GestorSuplidores;
    exports com.example.premierservices.Controllers.GestorFacturacion;
    exports com.example.premierservices.Applications.GestorSuplidores;
    exports com.example.premierservices.Applications.GestorFacturacion;

    opens com.example.premierservices to javafx.fxml;
    opens com.example.premierservices.Controllers.Admin to javafx.fxml;
    opens com.example.premierservices.Controllers.GlobalController to javafx.fxml;
    opens com.example.premierservices.Controllers.Clientes to javafx.fxml;
    opens com.example.premierservices.Controllers.Proveedores to javafx.fxml;
    opens com.example.premierservices.Controllers.GestorSuplidores to javafx.fxml;
    opens com.example.premierservices.Controllers.GestorFacturacion to javafx.fxml;
    opens com.example.premierservices.Models to javafx.base, javafx.fxml;
    opens com.example.premierservices.Applications.GlobalController to javafx.graphics;
    opens com.example.premierservices.Applications.Admin to javafx.graphics;
    opens com.example.premierservices.Applications.Cliente to javafx.graphics;
    opens com.example.premierservices.Applications.Proveedor to javafx.graphics;
    opens com.example.premierservices.Applications.GestorSuplidores to javafx.graphics;
    opens com.example.premierservices.Applications.GestorFacturacion to javafx.graphics;
}