package com.example.premierservices.Applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BusquedaServicioApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                BusquedaServicioApplication.class.getResource(
                        "/Busqueda-Servicios.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);  // Tamaño más grande
        stage.setTitle("Premier Services - Búsqueda de Servicios");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}