package com.example.premierservices.Applications.Proveedor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProveedorDashboardApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ProveedorDashboard.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Panel del Proveedor");
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}