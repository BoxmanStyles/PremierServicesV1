package com.example.premierservices.Applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class applicationGestionPagosFacturas extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminGestionPagosFacturas.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Gestión de Pagos y Facturas - Premier Services");
            primaryStage.setScene(new Scene(root, 1285, 1968));
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