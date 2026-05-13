package com.example.premierservices.Controllers.GlobalController;

import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SplashScreenController {

    @FXML private Label lblEstado;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ImageView imgCorona;
    @FXML private Pane rootPane;

    private Stage splashStage;

    @FXML
    public void initialize() {
        // Cargar imagen de corona
        File coronaFile = new File("IMG/Corona-Inclinada.png");
        if (coronaFile.exists()) {
            imgCorona.setImage(new Image(coronaFile.toURI().toString()));
        }

        // ========== EFECTOS ORIGINALES DE LA CORONA ==========

        // Efecto de brillo (Glow) para la corona
        Glow glowCorona = new Glow(0);
        imgCorona.setEffect(glowCorona);

        Timeline timelineBrillo = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glowCorona.levelProperty(), 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(glowCorona.levelProperty(), 0.8)),
                new KeyFrame(Duration.millis(2000), new KeyValue(glowCorona.levelProperty(), 0))
        );
        timelineBrillo.setCycleCount(Timeline.INDEFINITE);
        timelineBrillo.play();

        // Rotación suave de la corona
        RotateTransition rotacion = new RotateTransition(Duration.seconds(3), imgCorona);
        rotacion.setByAngle(10);
        rotacion.setAutoReverse(true);
        rotacion.setCycleCount(RotateTransition.INDEFINITE);
        rotacion.play();

        // Efecto de pulso (escala) para la corona
        ScaleTransition pulso = new ScaleTransition(Duration.millis(1500), imgCorona);
        pulso.setToX(1.1);
        pulso.setToY(1.1);
        pulso.setAutoReverse(true);
        pulso.setCycleCount(ScaleTransition.INDEFINITE);
        pulso.play();

        // Caída suave de la corona desde arriba
        imgCorona.setTranslateY(-50);
        TranslateTransition caida = new TranslateTransition(Duration.millis(800), imgCorona);
        caida.setToY(0);
        caida.setInterpolator(Interpolator.EASE_BOTH);
        caida.play();

        // Efecto de sombra en la corona
        DropShadow sombra = new DropShadow(10, Color.web("#4A7FA9", 0.5));
        imgCorona.setEffect(sombra);

        Timeline sombraAnimada = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sombra.radiusProperty(), 10)),
                new KeyFrame(Duration.millis(500), new KeyValue(sombra.radiusProperty(), 20)),
                new KeyFrame(Duration.millis(1000), new KeyValue(sombra.radiusProperty(), 10))
        );
        sombraAnimada.setCycleCount(Timeline.INDEFINITE);
        sombraAnimada.play();

        // ========== EFECTO PULSE GLOW PARA EL TEXTO "Premier Services" ==========
        Label lblTitulo = buscarLabelPremierServices();
        if (lblTitulo != null) {
            aplicarPulseGlowATexto(lblTitulo);
        }

        // Fade in del fondo
        if (rootPane != null) {
            rootPane.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), rootPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }

        // Fade in del ProgressIndicator
        progressIndicator.setOpacity(0);
        FadeTransition fadeProgress = new FadeTransition(Duration.millis(500), progressIndicator);
        fadeProgress.setToValue(1);
        fadeProgress.setDelay(Duration.millis(300));
        fadeProgress.play();

        // Fade in del texto de estado
        lblEstado.setOpacity(0);
        FadeTransition fadeTexto = new FadeTransition(Duration.millis(500), lblEstado);
        fadeTexto.setToValue(1);
        fadeTexto.setDelay(Duration.millis(400));
        fadeTexto.play();
    }

    private Label buscarLabelPremierServices() {
        // Buscar el Label con texto "Premier Services" dentro del rootPane
        if (rootPane == null) return null;

        for (javafx.scene.Node node : rootPane.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) node;
                if (borderPane.getCenter() instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.VBox vbox = (javafx.scene.layout.VBox) borderPane.getCenter();
                    for (javafx.scene.Node child : vbox.getChildren()) {
                        if (child instanceof Label && "Premier Services".equals(((Label) child).getText())) {
                            return (Label) child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void aplicarPulseGlowATexto(Label labelTexto) {
        // Efecto Glow inicial
        Glow glow = new Glow(0.3);
        labelTexto.setEffect(glow);

        // Animación de brillo pulsante (Pulse Glow)
        Timeline pulseGlow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.3)),
                new KeyFrame(Duration.millis(800), new KeyValue(glow.levelProperty(), 1.0)),
                new KeyFrame(Duration.millis(1600), new KeyValue(glow.levelProperty(), 0.3))
        );
        pulseGlow.setCycleCount(Timeline.INDEFINITE);
        pulseGlow.play();
    }

    public void init(Stage stage) {
        this.splashStage = stage;

        Task<Void> tareaCarga = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Conectando a la base de datos...");
                updateProgress(0.1, 1.0);
                Thread.sleep(800);

                for (int i = 0; i < 3; i++) {
                    updateMessage("Conectando a la base de datos" + ".".repeat(i + 1));
                    Thread.sleep(300);
                }

                updateMessage("Cargando perfiles de proveedores...");
                updateProgress(0.3, 1.0);
                Thread.sleep(800);

                updateMessage("Cargando servicios disponibles...");
                updateProgress(0.6, 1.0);
                Thread.sleep(800);

                updateMessage("Preparando interfaz...");
                updateProgress(0.9, 1.0);
                Thread.sleep(500);

                updateMessage("¡Listo!");
                updateProgress(1.0, 1.0);
                Thread.sleep(500);
                return null;
            }
        };

        lblEstado.textProperty().bind(tareaCarga.messageProperty());
        progressIndicator.progressProperty().bind(tareaCarga.progressProperty());

        tareaCarga.setOnSucceeded(event -> {
            try {
                cargarPantallaPrincipal();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error al cargar la aplicación: " + e.getMessage());
            }
        });

        new Thread(tareaCarga).start();
    }

    private void cargarPantallaPrincipal() throws IOException {
        String[] rutas = {
                "/PaginaPrincipal(Sin_Sesion).fxml",
                "/com/example/premierservices/PaginaPrincipal(Sin_Sesion).fxml"
        };
        URL fxmlUrl = null;
        for (String ruta : rutas) {
            fxmlUrl = getClass().getResource(ruta);
            if (fxmlUrl != null) break;
        }
        if (fxmlUrl == null) {
            throw new IOException("No se encontró la pantalla principal");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        splashStage.close();

        Stage mainStage = new Stage();
        mainStage.setTitle("Premier Services - Eventos");

        root.setOpacity(0);
        Scene scene = new Scene(root);
        mainStage.setScene(scene);

        FadeTransition fadeMain = new FadeTransition(Duration.millis(500), root);
        fadeMain.setToValue(1);
        fadeMain.play();

        mainStage.centerOnScreen();
        mainStage.show();
    }
}