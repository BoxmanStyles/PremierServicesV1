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

public class RegistrateClienteController {

    @FXML private TextField nombreField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private CheckBox terminosCheckBox;
    @FXML private Label lblMensaje;
    @FXML private Button registrarButton, cancelarButton;

    @FXML
    public void initialize() {
        lblMensaje.setText("");
        nombreField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onRegistrarClick() {
        mostrarMensaje("Registro de cliente aún no implementado", "info");
    }

    @FXML
    private void onCancelarClick() {
        limpiarCampos();
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

    private void limpiarCampos() {
        nombreField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        terminosCheckBox.setSelected(false);
        limpiarMensaje();
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
        if (tipo.equals("error"))
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else if (tipo.equals("exito"))
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        else
            lblMensaje.setStyle("-fx-text-fill: #34495e;");
    }
}