package com.example.premierservices.Applications.GlobalController;

import com.example.premierservices.Controllers.GlobalController.SeleccionPlanController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SeleccionPlanApplication extends Application {

    private static int idSuplidor;

    public static void setIdSuplidor(int id) {
        idSuplidor = id;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SeleccionPlan.fxml"));
            Parent root = loader.load();

            // Obtener el controller y pasarle el idSuplidor
            SeleccionPlanController controller = loader.getController();
            controller.setIdSuplidor(idSuplidor);

            primaryStage.setTitle("Seleccionar Plan - Premier Services");
            primaryStage.setScene(new Scene(root, 500, 600));
            primaryStage.setResizable(false);
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