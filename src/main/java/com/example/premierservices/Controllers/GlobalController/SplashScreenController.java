package com.example.premierservices.Controllers.GlobalController;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SplashScreenController {

    @FXML private Label lblEstado;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ImageView imgCorona;

    private Stage splashStage;

    @FXML
    public void initialize() {
        // Cargar imagen de corona
        File coronaFile = new File("IMG/Corona-Inclinada.png");
        if (coronaFile.exists()) {
            imgCorona.setImage(new Image(coronaFile.toURI().toString()));
        }
    }

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

        // Vincular UI con la tarea
        lblEstado.textProperty().bind(tareaCarga.messageProperty());
        progressIndicator.progressProperty().bind(tareaCarga.progressProperty());

        tareaCarga.setOnSucceeded(event -> {
            try {
                cargarPantallaPrincipal();
            } catch (IOException e) {
                e.printStackTrace();
                // No se puede usar lblEstado.setText porque está bindeado, así que mostramos error en consola
                System.err.println("Error al cargar la aplicación: " + e.getMessage());
            }
        });

        new Thread(tareaCarga).start();
    }

    private void cargarPantallaPrincipal() throws IOException {
        String[] rutas = {
                "/PaginaPrincipal(Sin_Sesion).fxml",
                "/com/example/premierservices/PaginaPrincipal(Sin_Sesion).fxml"
        };
        URL fxmlUrl = null;
        for (String ruta : rutas) {
            fxmlUrl = getClass().getResource(ruta);
            if (fxmlUrl != null) break;
        }
        if (fxmlUrl == null) {
            throw new IOException("No se encontró la pantalla principal");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        splashStage.close();

        Stage mainStage = new Stage();
        mainStage.setTitle("Premier Services - Eventos");
        mainStage.setScene(new Scene(root));
        mainStage.centerOnScreen();
        mainStage.show();
    }
}