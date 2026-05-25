package com.example.premierservices.Applications.Admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;

public class GestionReservasApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el FXML
        URL fxmlUrl = getClass().getResource("/AdminGestionReservas.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERROR: No se encontró AdminGestionReservas.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Configurar la ventana
        primaryStage.setTitle("Gestión de Reservas - Premier Services");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);

        // Cargar icono
        try {
            File iconFile = new File("IMG/Logo.png");
            if (iconFile.exists()) {
                primaryStage.getIcons().add(new Image(iconFile.toURI().toString()));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}