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

    @FXML
    public void initialize() {
        lblMensaje.setText("");

        // Limpiar mensaje cuando el usuario empieza a escribir
        emailField.textProperty().addListener((obs, oldVal, newVal) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> limpiarMensaje());
    }

    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validar campos
        if (email.isEmpty()) {
            mostrarMensaje("Por favor, ingrese su correo electrónico", "error");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mostrarMensaje("Por favor, ingrese su contraseña", "error");
            passwordField.requestFocus();
            return;
        }

        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarMensaje("Formato de correo inválido", "error");
            emailField.requestFocus();
            return;
        }

        // Aquí va tu lógica de autenticación
        // Por ahora, ejemplo simple
        if (email.equals("admin@premierservices.com") && password.equals("admin123")) {
            mostrarMensaje("¡Bienvenido Administrador!", "exito");
            // Aquí abrir dashboard de admin
        } else if (email.equals("proveedor@premierservices.com") && password.equals("proveedor123")) {
            mostrarMensaje("¡Bienvenido Proveedor!", "exito");
            // Aquí abrir dashboard de proveedor
        } else if (email.equals("cliente@premierservices.com") && password.equals("cliente123")) {
            mostrarMensaje("¡Bienvenido Cliente!", "exito");
            // Aquí abrir dashboard de cliente
        } else {
            mostrarMensaje("Correo o contraseña incorrectos", "error");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    @FXML
    private void onRegisterClick() {
        try {
            // Cargar pantalla de registro
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/premierservices/Register/register-proveedor-view.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Premier Services - Registro de Proveedor");
            stage.setWidth(900);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            mostrarMensaje("Error al cargar la pantalla de registro", "error");
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        lblMensaje.setVisible(true);

        switch (tipo) {
            case "error":
                lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 13px;");
                break;
            case "exito":
                lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;");
                break;
            default:
                lblMensaje.setStyle("-fx-text-fill: #34495e; -fx-font-size: 12px;");
        }
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
    }
}