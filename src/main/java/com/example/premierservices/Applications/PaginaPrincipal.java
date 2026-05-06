package com.example.premierservices.Applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PaginaPrincipal extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                RegistroProveedorApplication.class.getResource(
                        "/Pagina Principal Beta.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1288, 668);

        stage.setTitle("Premier Services - Registro de Proveedores");
        stage.setScene(scene);

        stage.setResizable(false);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}