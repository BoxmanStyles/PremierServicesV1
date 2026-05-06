package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;  // ← Importación correcta
import javafx.scene.layout.Pane;
import java.io.File;

public class controllerPaginaPrincipal {

    @FXML private Pane PanelBuscador;
    @FXML private Pane PanelBandeja;
    @FXML private ImageView imagenPerfil;

    @FXML
    protected void Buscador(ActionEvent actionEvent) {
        PanelBuscador.setVisible(true);
    }

    @FXML
    protected void SalirBuscar(ActionEvent actionEvent) {
        PanelBuscador.setVisible(false);
    }

    @FXML
    protected void BandejaAbrir(ActionEvent actionEvent) {
        PanelBandeja.setVisible(true);
    }

    @FXML
    public void SalirBandeja(ActionEvent actionEvent) {
        PanelBandeja.setVisible(false);
    }

    @FXML
    public void initialize() {
        // Cargar la imagen desde la ruta absoluta (o la que funcione)
        File file = new File("C:/Users/jeanm/IdeaProjects/PremierServicesV1/IMG/Perfil 1 sin fondo.png");
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            imagenPerfil.setImage(image);
        } else {
            System.err.println("Imagen no encontrada: " + file.getAbsolutePath());
        }
    }
}