package com.example.premierservices.Applications.Cliente;

import com.example.premierservices.Controllers.Clientes.PaginaPrincipalSesionController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class PaginaPrincipalSesionApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String nombreArchivo = "PaginaPrincipal(Sesion_Iniciada).fxml";
        // No hay espacios, no necesita codificación especial
        URL fxmlUrl = getClass().getResource("/" + nombreArchivo);
        if (fxmlUrl == null) {
            System.err.println("No se encontró el archivo: " + nombreArchivo);
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        PaginaPrincipalSesionController controller = loader.getController();
        // Datos de ejemplo: reemplazar con los reales tras login
        controller.setDatosCliente(1, "Jean Miguel", "C:/Users/jeanm/IdeaProjects/PremierServicesV1/IMG/Perfil 1 sin fondo.png");

        Scene scene = new Scene(root);
        primaryStage.setTitle("Premier Services - Cliente");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}