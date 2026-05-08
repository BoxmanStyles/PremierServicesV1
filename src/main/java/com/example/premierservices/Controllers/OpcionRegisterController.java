package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class OpcionRegisterController {

    @FXML private Button RegistrarteProveedor;
    @FXML private Button RegistrarteCliente;
    @FXML private Button IrAlLogin;
    @FXML private ImageView imgProveedor;
    @FXML private ImageView imgCliente;

    @FXML
    public void initialize() {
        // Imágenes desde la carpeta IMG en la raíz del proyecto
        File imgProv = new File("IMG/icono_proveedor1-removebg.png");
        if (imgProv.exists()) {
            imgProveedor.setImage(new Image(imgProv.toURI().toString()));
        } else {
            System.err.println("Imagen proveedor no encontrada: " + imgProv.getAbsolutePath());
        }

        File imgCli = new File("IMG/icono_cliente1-removebgpng.png");
        if (imgCli.exists()) {
            imgCliente.setImage(new Image(imgCli.toURI().toString()));
        } else {
            System.err.println("Imagen cliente no encontrada: " + imgCli.getAbsolutePath());
        }
    }

    @FXML
    private void irRegistroProveedor(ActionEvent event) {
        navegarA(event, "RegistrateProveedor.fxml", "Registro de Proveedor");
    }

    @FXML
    private void irRegistroCliente(ActionEvent event) {
        navegarA(event, "RegistrateCliente.fxml", "Registro de Cliente");
    }

    @FXML
    private void volverALogin(ActionEvent event) {
        navegarA(event, "LoginGeneralV2.fxml", "Iniciar Sesión");
    }

    private void navegarA(ActionEvent event, String fxmlNombre, String titulo) {
        try {
            // Buscar directamente en la raíz del classpath
            URL url = getClass().getResource("/" + fxmlNombre);
            if (url == null) {
                System.err.println("No se encontró el FXML: " + fxmlNombre);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}