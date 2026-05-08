package com.example.premierservices.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginGeneralControllerV2 {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label lblMensaje;
    @FXML private Button AtrasLogin;

    @FXML
    public void initialize() {
        lblMensaje.setText("");
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            mostrarMensaje("Ingrese su correo electrónico", "error");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            mostrarMensaje("Ingrese su contraseña", "error");
            passwordField.requestFocus();
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarMensaje("Correo inválido", "error");
            emailField.requestFocus();
            return;
        }

        // Simulación
        if (email.equals("admin@premierservices.com") && password.equals("admin123")) {
            mostrarMensaje("Bienvenido Administrador", "exito");
        } else if (email.equals("proveedor@premierservices.com") && password.equals("proveedor123")) {
            mostrarMensaje("Bienvenido Proveedor", "exito");
        } else if (email.equals("cliente@premierservices.com") && password.equals("cliente123")) {
            mostrarMensaje("Bienvenido Cliente", "exito");
        } else {
            mostrarMensaje("Credenciales incorrectas", "error");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    @FXML
    private void onRegisterClick() {
        navegarA("OpcionRegister.fxml", "Elegir tipo de registro");
    }

    @FXML
    private void volverAPaginaPrincipal() {
        navegarA("Pagina Principal Beta.fxml", "Premier Services - Eventos");
    }

    private void navegarA(String fxmlNombre, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlNombre));
            Stage stage = (Stage) AtrasLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar pantalla", "error");
        }
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);
        if (tipo.equals("error"))
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
    }
}