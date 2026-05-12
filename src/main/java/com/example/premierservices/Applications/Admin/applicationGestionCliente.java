package com.example.premierservices.Applications.Admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class applicationGestionCliente extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminGestionCliente.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Gestión de Clientes - Premier Services");
            primaryStage.setScene(new Scene(root, 1283, 1302));
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar el FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}