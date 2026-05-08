package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class RegistrateProveedorController {

    @FXML private TextField nombreField, emailField, telefonoField, nombreEmpresaField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> ubicacionComboBox;
    @FXML private TextArea descripcionArea;
    @FXML private CheckBox terminosCheckBox;
    @FXML private Label lblMensajePersonal;
    @FXML private Button registrarButton;

    private final List<String> ubicaciones = Arrays.asList(
            "Santo Domingo", "Santiago", "La Vega", "Puerto Plata",
            "San Pedro de Macorís", "La Romana", "Higüey"
    );

    @FXML
    public void initialize() {
        ubicacionComboBox.getItems().addAll(ubicaciones);
        lblMensajePersonal.setText("");
        nombreField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        telefonoField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        nombreEmpresaField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        ubicacionComboBox.valueProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onRegistrarClick() {
        mostrarMensaje("Registro de proveedor aún no implementado", "info");
    }

    @FXML
    private void irALogin(ActionEvent event) {
        navegarA(event, "LoginGeneralV2.fxml", "Iniciar Sesión");
    }

    @FXML
    private void volverAOpcionRegister(ActionEvent event) {
        navegarA(event, "OpcionRegister.fxml", "Elegir tipo de registro");
    }

    private void navegarA(ActionEvent event, String fxmlNombre, String titulo) {
        try {
            URL url = getClass().getResource("/" + fxmlNombre);
            if (url == null) {
                System.err.println("No se encontró: " + fxmlNombre);
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

    private void limpiarMensaje() {
        lblMensajePersonal.setText("");
        lblMensajePersonal.setVisible(false);
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensajePersonal.setText(mensaje);
        lblMensajePersonal.setVisible(true);
        if (tipo.equals("error"))
            lblMensajePersonal.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else if (tipo.equals("exito"))
            lblMensajePersonal.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        else
            lblMensajePersonal.setStyle("-fx-text-fill: #34495e;");
    }
}