package com.example.premierservices.Applications.GlobalController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditarPortafolioApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el FXML de edición de portafolios
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/EditarPortafolio.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestión de Portafolios (Administrador)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}