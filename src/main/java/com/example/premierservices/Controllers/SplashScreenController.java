package com.example.premierservices.Controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class SplashScreenController {

    @FXML private Label lblEstado;
    @FXML private ProgressIndicator progressIndicator;

    private Stage splashStage;

    public void init(Stage stage) {
        this.splashStage = stage;

        Task<Void> tareaCarga = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Conectando a la base de datos...");
                updateProgress(0.1, 1.0);
                Thread.sleep(800);

                updateMessage("Cargando perfiles de proveedores...");
                updateProgress(0.3, 1.0);
                Thread.sleep(800);

                updateMessage("Cargando servicios disponibles...");
                updateProgress(0.6, 1.0);
                Thread.sleep(800);

                updateMessage("Preparando interfaz...");
                updateProgress(0.9, 1.0);
                Thread.sleep(500);

                updateMessage("¡Listo!");
                updateProgress(1.0, 1.0);
                Thread.sleep(500);
                return null;
            }
        };

        lblEstado.textProperty().bind(tareaCarga.messageProperty());
        progressIndicator.progressProperty().bind(tareaCarga.progressProperty());

        tareaCarga.setOnSucceeded(event -> {
            try {
                cargarPantallaPrincipal();
            } catch (IOException e) {
                e.printStackTrace();
                lblEstado.setText("Error al cargar la aplicación: " + e.getMessage());
            }
        });

        new Thread(tareaCarga).start();
    }

    private void cargarPantallaPrincipal() throws IOException {
        // Buscar el FXML de la pantalla principal (con espacios en el nombre)
        String[] rutas = {
                "/Pagina Principal Beta.fxml",
                "/com/example/premierservices/Pagina Principal Beta.fxml"
        };
        URL fxmlUrl = null;
        for (String ruta : rutas) {
            fxmlUrl = getClass().getResource(ruta);
            if (fxmlUrl != null) {
                System.out.println("FXML principal encontrado en: " + ruta);
                break;
            }
        }
        if (fxmlUrl == null) {
            throw new IOException("No se encontró la pantalla principal");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Cerrar la ventana de splash
        splashStage.close();

        // Crear una nueva ventana para la aplicación principal
        Stage mainStage = new Stage();
        mainStage.setTitle("Premier Services - Eventos");
        mainStage.setScene(new Scene(root));
        mainStage.centerOnScreen();
        mainStage.show();
    }
}