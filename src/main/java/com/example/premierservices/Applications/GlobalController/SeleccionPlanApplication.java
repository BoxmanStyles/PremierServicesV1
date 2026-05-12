package com.example.premierservices.Applications.GlobalController;

import com.example.premierservices.Controllers.GlobalController.SeleccionPlanController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SeleccionPlanApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SeleccionPlan.fxml"));
        Parent root = loader.load();
        SeleccionPlanController controller = loader.getController();
        controller.setIdSuplidor(1); // ID de prueba
        stage.setScene(new Scene(root));
        stage.setTitle("Premier Services — Selección de Plan");
        stage.centerOnScreen();
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}