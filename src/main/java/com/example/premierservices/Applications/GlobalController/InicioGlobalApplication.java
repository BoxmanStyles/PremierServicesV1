package com.example.premierservices.Applications.GlobalController;

import com.example.premierservices.Controllers.GlobalController.SplashScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class InicioGlobalApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar FXML
        URL fxmlUrl = getClass().getResource("/INICIO GLOBAL.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERROR: No se encontró INICIO GLOBAL.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Configurar ventana
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();

        // Cargar logo desde múltiples ubicaciones
        cargarLogo(primaryStage);

        primaryStage.show();

        // Inicializar SplashScreen
        SplashScreenController controller = loader.getController();
        if (controller != null) {
            controller.init(primaryStage);
        }
    }

    private void cargarLogo(Stage primaryStage) {
        // Intento 1: Desde archivo en carpeta IMG (raíz del proyecto)
        try {
            File logoFile = new File("IMG/Logo.png");
            if (logoFile.exists()) {
                primaryStage.getIcons().add(new Image(logoFile.toURI().toString()));
                System.out.println("✅ Logo cargado desde archivo: " + logoFile.getAbsolutePath());
                return;
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo desde archivo: " + e.getMessage());
        }

        // Intento 2: Desde classpath con ruta correcta
        try {
            InputStream stream = getClass().getResourceAsStream("/IMG/Logo.png");
            if (stream != null) {
                primaryStage.getIcons().add(new Image(stream));
                System.out.println("✅ Logo cargado desde classpath /IMG/Logo.png");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo desde /IMG/Logo.png: " + e.getMessage());
        }

        // Intento 3: Desde classpath con ruta alternativa
        try {
            InputStream stream = getClass().getResourceAsStream("IMG/Logo.png");
            if (stream != null) {
                primaryStage.getIcons().add(new Image(stream));
                System.out.println("✅ Logo cargado desde classpath IMG/Logo.png");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo desde IMG/Logo.png: " + e.getMessage());
        }

        System.err.println("⚠️ No se pudo cargar el logo en ninguna ubicación");
    }

    public static void main(String[] args) {
        launch(args);
    }
}