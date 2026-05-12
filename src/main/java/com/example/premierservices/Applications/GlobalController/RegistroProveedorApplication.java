package com.example.premierservices.Applications.GlobalController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistroProveedorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                RegistroProveedorApplication.class.getResource(
                        "/Register original Proveedor.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 838, 793);

        stage.setTitle("Premier Services - Registro de Proveedores");
        stage.setScene(scene);

        stage.setResizable(false);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}