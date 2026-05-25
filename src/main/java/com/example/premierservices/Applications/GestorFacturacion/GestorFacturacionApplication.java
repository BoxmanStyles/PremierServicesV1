package com.example.premierservices.Applications.GestorFacturacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GestorFacturacionApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestorFacturacionPanel.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Premier Services - Gestión de Facturación");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
