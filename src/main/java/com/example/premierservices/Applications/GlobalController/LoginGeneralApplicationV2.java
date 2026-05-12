package com.example.premierservices.Applications.GlobalController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginGeneralApplicationV2 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el FXML (ahora sin espacios)
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/LoginGeneralV2.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setTitle("Premier Services - Iniciar Sesión");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}