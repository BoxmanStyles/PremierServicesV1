package com.example.premierservices.Applications.Admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminPaginaPrincipalApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/AdminPaginaPrincipal.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Administración de Portafolios - Premier Services");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}