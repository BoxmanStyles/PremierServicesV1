package com.example.premierservices.Applications.GlobalController;

import com.example.premierservices.Controllers.GlobalController.SplashScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class InicioGlobalApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/com/example/premierservices/INICIO GLOBAL.fxml");
        if (fxmlUrl == null) {
            fxmlUrl = getClass().getResource("/INICIO GLOBAL.fxml");
        }
        if (fxmlUrl == null) {
            System.err.println("ERROR: No se encontró INICIO GLOBAL.fxml");
            return;
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
 
        SplashScreenController controller = loader.getController();
        controller.init(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}