package com.example.premierservices.Applications.GestorSuplidores;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GestorSuplidoresApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestorSuplidoresPanel.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Premier Services - Gestión de Suplidores");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
